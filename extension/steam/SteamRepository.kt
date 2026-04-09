package com.winlator.cmod.store

import android.content.Context
import android.util.Log
import in.dragonbra.javasteam.enums.EResult
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes
import in.dragonbra.javasteam.steam.authentication.AuthSessionDetails
import in.dragonbra.javasteam.steam.authentication.IAuthenticator
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback
import in.dragonbra.javasteam.steam.steamclient.SteamClient
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.EnumSet

/**
 * Singleton managing the JavaSteam SteamClient lifecycle.
 *
 * Lifecycle:
 *   SteamMainActivity.onCreate() → SteamPrefs.init() → SteamRepository.initialize()
 *   SteamForegroundService.onStartCommand() → SteamRepository.connect()
 *   SteamForegroundService.onDestroy() → SteamRepository.disconnect()
 *
 * Events flow through [events] SharedFlow; UI collects these to drive state.
 */
object SteamRepository {

    private const val TAG = "SteamRepo"

    // --- JavaSteam instances (null until initialize() is called) ---
    private var steamClient: SteamClient? = null
    private var manager: CallbackManager? = null
    private var steamUser: SteamUser? = null
    private var steamApps: SteamApps? = null
    private var steamFriends: SteamFriends? = null

    // --- Coroutine infrastructure ---
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pumpJob: Job? = null

    // --- Public state ---
    private val _events = MutableSharedFlow<SteamEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SteamEvent> = _events

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _library = MutableStateFlow<List<SteamGame>>(emptyList())
    val library: StateFlow<List<SteamGame>> = _library

    // --- Raw license list (needed by DepotDownloader in Phase 5) ---
    private val _licenses = mutableListOf<Any>()   // typed as Any — downcast in DepotDownloader
    fun getLicenses(): List<Any> = _licenses.toList()

    // --- Pending Steam Guard code (UI completes this deferred) ---
    @Volatile private var pendingGuardCode: CompletableDeferred<String>? = null

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Build the SteamClient and register all callbacks.
     * Must be called once before connect().
     */
    fun initialize() {
        if (steamClient != null) return   // idempotent

        val config = SteamConfiguration.create { b ->
            b.withProtocolTypes(EnumSet.of(ProtocolTypes.WEB_SOCKET))
            b.withConnectionTimeout(30_000L)
        }

        steamClient = SteamClient(config)
        manager = CallbackManager(steamClient!!)

        steamUser    = steamClient!!.getHandler(SteamUser::class.java)
        steamApps    = steamClient!!.getHandler(SteamApps::class.java)
        steamFriends = steamClient!!.getHandler(SteamFriends::class.java)

        registerCallbacks()
        Log.i(TAG, "SteamRepository initialised")
    }

    private fun registerCallbacks() {
        val m = manager!!
        m.subscribe(ConnectedCallback::class.java) { onConnected() }
        m.subscribe(DisconnectedCallback::class.java) { cb: DisconnectedCallback ->
            onDisconnected(cb.isUserInitiated)
        }
        m.subscribe(LoggedOnCallback::class.java) { cb: LoggedOnCallback ->
            onLoggedOn(cb)
        }
        m.subscribe(LoggedOffCallback::class.java) { cb: LoggedOffCallback ->
            onLoggedOff(cb)
        }
        m.subscribe(LicenseListCallback::class.java) { cb: LicenseListCallback ->
            onLicenseList(cb)
        }
    }

    // -------------------------------------------------------------------------
    // Connection
    // -------------------------------------------------------------------------

    /** Connect to Steam CM. Triggers auto-login if a refresh token is stored. */
    fun connect() {
        val client = steamClient ?: run {
            Log.e(TAG, "connect() called before initialize()")
            return
        }
        client.connect()
        startPump()
    }

    /** Disconnect from Steam CM and stop the callback pump. */
    fun disconnect() {
        steamClient?.disconnect()
        pumpJob?.cancel()
        pumpJob = null
        _isConnected.value = false
        _isLoggedIn.value = false
    }

    private fun startPump() {
        pumpJob?.cancel()
        pumpJob = scope.launch {
            Log.d(TAG, "Callback pump started")
            while (isActive) {
                try {
                    manager?.runWaitCallbacks(500L)
                } catch (e: Exception) {
                    Log.e(TAG, "Pump error", e)
                }
            }
            Log.d(TAG, "Callback pump stopped")
        }
    }

    // -------------------------------------------------------------------------
    // Callback handlers (all called on the pump coroutine thread)
    // -------------------------------------------------------------------------

    private fun onConnected() {
        Log.i(TAG, "Connected to Steam CM")
        _isConnected.value = true
        emit(SteamEvent.Connected)

        // Auto-login with stored refresh token
        if (SteamPrefs.isLoggedIn) {
            Log.i(TAG, "Auto-login as ${SteamPrefs.username}")
            loginWithToken(SteamPrefs.username, SteamPrefs.refreshToken)
        }
    }

    private fun onDisconnected(userInitiated: Boolean) {
        Log.i(TAG, "Disconnected (userInitiated=$userInitiated)")
        _isConnected.value = false
        _isLoggedIn.value = false
        emit(SteamEvent.Disconnected(userInitiated))
    }

    private fun onLoggedOn(cb: LoggedOnCallback) {
        if (cb.result != EResult.OK) {
            Log.w(TAG, "LoggedOn failed: ${cb.result}")
            emit(SteamEvent.LoginFailed(cb.result.name))
            return
        }

        SteamPrefs.cellId    = cb.cellID
        val sid64 = cb.clientSteamID.convertToUInt64().toLong()
        SteamPrefs.steamId64 = sid64
        SteamPrefs.accountId = (sid64 and 0xFFFFFFFFL).toInt()   // lower 32 bits = account ID

        _isLoggedIn.value = true
        emit(SteamEvent.LoggedIn(SteamPrefs.steamId64, SteamPrefs.displayName))
        Log.i(TAG, "Logged in as ${SteamPrefs.username} (${SteamPrefs.displayName})")
    }

    private fun onLoggedOff(cb: LoggedOffCallback) {
        Log.i(TAG, "Logged off: ${cb.result}")
        _isLoggedIn.value = false
        emit(SteamEvent.LoggedOut)
    }

    private fun onLicenseList(cb: LicenseListCallback) {
        Log.i(TAG, "Received ${cb.licenseList.size} licenses")
        synchronized(_licenses) {
            _licenses.clear()
            _licenses.addAll(cb.licenseList)
        }
        // Full PICS sync in Phase 4 — placeholder here
        emit(SteamEvent.LibraryProgress(0, cb.licenseList.size))
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /**
     * Auto-login using a stored refresh token.
     * Called automatically on [onConnected]; also callable directly after a token is known.
     */
    fun loginWithToken(username: String, refreshToken: String) {
        steamUser?.logOn(LogOnDetails().apply {
            this.username = username
            this.accessToken = refreshToken          // refreshToken goes here per JavaSteam API
            shouldRememberPassword = true
        })
    }

    /**
     * First-time credential login using username + password.
     *
     * [onGuardCode] is invoked when Steam Guard is required.
     *   - type = "email" or "2fa"
     *   - hint = partial email domain (for email type) or null
     *   Returns the code entered by the user (suspends until UI provides it).
     *
     * On success, the refresh token is persisted in SteamPrefs and
     * [loginWithToken] is called to complete the CM login.
     *
     * On failure, [SteamEvent.LoginFailed] is emitted.
     */
    suspend fun loginWithCredentials(
        username: String,
        password: String,
        onGuardCode: suspend (type: String, hint: String?) -> String,
    ) = withContext(Dispatchers.IO) {
        val client = steamClient ?: run {
            emit(SteamEvent.LoginFailed("Not initialised"))
            return@withContext
        }

        try {
            val authenticator = object : IAuthenticator {
                override fun getDeviceCode(previousCodeWasIncorrect: Boolean): String {
                    emit(SteamEvent.SteamGuardTwoFactorRequired)
                    return runBlocking { onGuardCode("2fa", null) }
                }

                override fun getEmailCode(
                    emailDomain: String?,
                    previousCodeWasIncorrect: Boolean,
                ): String {
                    emit(SteamEvent.SteamGuardEmailRequired(emailDomain))
                    return runBlocking { onGuardCode("email", emailDomain) }
                }

                override fun acceptDeviceConfirmation(): Boolean = false
            }

            val authDetails = AuthSessionDetails().apply {
                this.username = username
                this.password = password
                persistentSession = true
                this.authenticator = authenticator
            }

            val auth = client.authentication
            val session = auth.beginAuthSessionViaCredentials(authDetails).await()
            val result  = session.pollingWaitForResult().await()

            val resolvedName = result.accountName?.takeIf { it.isNotEmpty() } ?: username
            SteamPrefs.username     = resolvedName
            SteamPrefs.refreshToken = result.refreshToken ?: ""

            Log.i(TAG, "Credentials auth succeeded for $resolvedName, token stored")
            loginWithToken(resolvedName, SteamPrefs.refreshToken)

        } catch (e: Exception) {
            Log.e(TAG, "Credential login failed", e)
            emit(SteamEvent.LoginFailed(e.message ?: "Unknown error"))
        }
    }

    /**
     * Start a QR login session.
     * Emits [SteamEvent.QrChallengeReceived] with the first challenge URL.
     * The URL is refreshed automatically (further events emitted) if Steam rotates it.
     * Completes (suspends until) either login succeeds or [SteamEvent.QrExpired].
     */
    suspend fun loginWithQr() = withContext(Dispatchers.IO) {
        val client = steamClient ?: run {
            emit(SteamEvent.LoginFailed("Not initialised"))
            return@withContext
        }

        try {
            val authDetails = AuthSessionDetails().apply {
                persistentSession = true
            }

            val auth    = client.authentication
            val session = auth.beginAuthSessionViaQR(authDetails).await()

            // Emit the first QR URL
            emit(SteamEvent.QrChallengeReceived(session.challengeUrl))

            // Wire up URL refresh callback (interface may vary by JavaSteam version)
            try {
                @Suppress("UNCHECKED_CAST")
                val iface = Class.forName(
                    "in.dragonbra.javasteam.steam.authentication.IChallengeUrlChanged"
                )
                val proxy = java.lang.reflect.Proxy.newProxyInstance(
                    iface.classLoader, arrayOf(iface)
                ) { _, _, _ -> emit(SteamEvent.QrChallengeReceived(session.challengeUrl)); null }
                session.javaClass.getField("challengeUrlChanged").set(session, proxy)
            } catch (_: Exception) {
                // Field/interface not present in this JavaSteam build — URL won't auto-refresh
            }

            // Poll until the user scans the QR code
            val result = session.pollingWaitForResult().await()
            if (result == null) {
                emit(SteamEvent.QrExpired)
                return@withContext
            }

            val resolvedName = result.accountName?.takeIf { it.isNotEmpty() } ?: ""
            SteamPrefs.username     = resolvedName
            SteamPrefs.refreshToken = result.refreshToken ?: ""

            Log.i(TAG, "QR auth succeeded for $resolvedName")
            loginWithToken(resolvedName, SteamPrefs.refreshToken)

        } catch (e: Exception) {
            Log.e(TAG, "QR login failed", e)
            emit(SteamEvent.LoginFailed(e.message ?: "QR error"))
        }
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    fun logout() {
        steamUser?.logOff()
        SteamPrefs.clear()
        _library.value = emptyList()
        synchronized(_licenses) { _licenses.clear() }
        Log.i(TAG, "Logged out")
    }

    // -------------------------------------------------------------------------
    // Library (stubs — full PICS sync in Phase 4)
    // -------------------------------------------------------------------------

    /** Replace the in-memory library (called from PICS sync in Phase 4). */
    fun updateLibrary(games: List<SteamGame>) {
        _library.value = games
        emit(SteamEvent.LibrarySynced)
    }

    // -------------------------------------------------------------------------
    // Accessors for downstream phases
    // -------------------------------------------------------------------------

    /** Raw SteamClient — needed by DepotDownloader in Phase 5. */
    fun getSteamClient(): SteamClient? = steamClient

    /** Raw SteamApps handler — needed by PICS sync in Phase 4. */
    fun getSteamApps(): SteamApps? = steamApps

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun emit(event: SteamEvent) {
        scope.launch { _events.emit(event) }
    }
}
