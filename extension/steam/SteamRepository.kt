package com.winlator.cmod.store

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.steam.authentication.AuthSessionDetails
import `in`.dragonbra.javasteam.steam.authentication.IAuthenticator
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import java.util.EnumSet
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton managing the JavaSteam SteamClient lifecycle.
 *
 * Does NOT use kotlinx.coroutines — uses HandlerThread + CountDownLatch
 * so the compile classpath stays minimal (android.jar + javasteam.jar + stdlib).
 *
 * Lifecycle:
 *   SteamForegroundService.onStartCommand() → SteamRepository.initialize() → .connect()
 *   SteamForegroundService.onDestroy()     → SteamRepository.disconnect()
 *
 * Events: register with addListener() / removeListener(). Listeners are called
 * on an internal background thread — post to main Handler if UI updates needed.
 */
object SteamRepository {

    private const val TAG = "SteamRepo"

    // -------------------------------------------------------------------------
    // Event listener interface
    // -------------------------------------------------------------------------

    interface SteamEventListener {
        fun onEvent(event: SteamEvent)
    }

    private val listeners = CopyOnWriteArrayList<SteamEventListener>()

    fun addListener(l: SteamEventListener) { listeners.add(l) }
    fun removeListener(l: SteamEventListener) { listeners.remove(l) }

    // -------------------------------------------------------------------------
    // State (safe to read from any thread)
    // -------------------------------------------------------------------------

    @Volatile var isConnected: Boolean = false
        private set

    @Volatile var isLoggedIn: Boolean = false
        private set

    @Volatile var library: List<SteamGame> = emptyList()
        private set

    // -------------------------------------------------------------------------
    // JavaSteam instances (null until initialize())
    // -------------------------------------------------------------------------

    private var steamClient: SteamClient? = null
    private var manager: CallbackManager? = null
    private var steamUser: SteamUser? = null
    private var steamApps: SteamApps? = null

    // callback pump thread
    private var pumpThread: HandlerThread? = null
    private var pumpHandler: Handler? = null
    private val pumping = AtomicBoolean(false)

    // raw licenses — needed by DepotDownloader (Phase 5)
    private val _licenses = ArrayList<Any>()
    fun getLicenses(): List<Any> = synchronized(_licenses) { _licenses.toList() }

    // pending Steam Guard code (set by UI, consumed by auth thread)
    @Volatile private var pendingGuardLatch: CountDownLatch? = null
    @Volatile private var pendingGuardCode: String = ""

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Build SteamClient and register callbacks.
     * Idempotent — safe to call multiple times.
     */
    fun initialize() {
        if (steamClient != null) return

        val config = SteamConfiguration.create { b ->
            b.withProtocolTypes(EnumSet.of(ProtocolTypes.WEB_SOCKET))
            b.withConnectionTimeout(30_000L)
        }

        steamClient = SteamClient(config)
        manager     = CallbackManager(steamClient!!)
        steamUser   = steamClient!!.getHandler(SteamUser::class.java)
        steamApps   = steamClient!!.getHandler(SteamApps::class.java)

        registerCallbacks()
        Log.i(TAG, "SteamRepository initialised")
    }

    private fun registerCallbacks() {
        val m = manager!!
        m.subscribe(ConnectedCallback::class.java)     { _: ConnectedCallback     -> onConnected() }
        m.subscribe(DisconnectedCallback::class.java)  { cb: DisconnectedCallback -> onDisconnected(cb.isUserInitiated) }
        m.subscribe(LoggedOnCallback::class.java)      { cb: LoggedOnCallback     -> onLoggedOn(cb) }
        m.subscribe(LoggedOffCallback::class.java)     { cb: LoggedOffCallback    -> onLoggedOff(cb) }
        m.subscribe(LicenseListCallback::class.java)   { cb: LicenseListCallback  -> onLicenseList(cb) }
    }

    // -------------------------------------------------------------------------
    // Connection
    // -------------------------------------------------------------------------

    fun connect() {
        val client = steamClient ?: run { Log.e(TAG, "connect() before initialize()"); return }
        startPump()
        client.connect()
    }

    fun disconnect() {
        steamClient?.disconnect()
        stopPump()
        isConnected = false
        isLoggedIn  = false
    }

    private fun startPump() {
        if (pumping.getAndSet(true)) return
        val ht = HandlerThread("SteamPump").also { it.start() }
        pumpThread  = ht
        pumpHandler = Handler(ht.looper)
        schedulePump()
    }

    private fun stopPump() {
        pumping.set(false)
        pumpThread?.quitSafely()
        pumpThread  = null
        pumpHandler = null
    }

    private fun schedulePump() {
        if (!pumping.get()) return
        pumpHandler?.post {
            try {
                manager?.runWaitCallbacks(500L)
            } catch (e: Exception) {
                Log.e(TAG, "Pump error", e)
            }
            schedulePump()
        }
    }

    // -------------------------------------------------------------------------
    // Callback handlers (called on the pump thread)
    // -------------------------------------------------------------------------

    private fun onConnected() {
        Log.i(TAG, "Connected to Steam CM")
        isConnected = true
        emit(SteamEvent.Connected)

        if (SteamPrefs.isLoggedIn) {
            Log.i(TAG, "Auto-login as ${SteamPrefs.username}")
            loginWithToken(SteamPrefs.username, SteamPrefs.refreshToken)
        }
    }

    private fun onDisconnected(userInitiated: Boolean) {
        Log.i(TAG, "Disconnected (userInitiated=$userInitiated)")
        isConnected = false
        isLoggedIn  = false
        emit(SteamEvent.Disconnected(userInitiated))
    }

    private fun onLoggedOn(cb: LoggedOnCallback) {
        if (cb.result != EResult.OK) {
            Log.w(TAG, "Login failed: ${cb.result}")
            emit(SteamEvent.LoginFailed(cb.result.name))
            return
        }

        SteamPrefs.cellId    = cb.cellID
        val sid64 = cb.clientSteamID.convertToUInt64().toLong()
        SteamPrefs.steamId64 = sid64
        SteamPrefs.accountId = (sid64 and 0xFFFFFFFFL).toInt()

        isLoggedIn = true
        emit(SteamEvent.LoggedIn(sid64, SteamPrefs.displayName))
        Log.i(TAG, "Logged in as ${SteamPrefs.username}")
    }

    private fun onLoggedOff(cb: LoggedOffCallback) {
        Log.i(TAG, "Logged off: ${cb.result}")
        isLoggedIn = false
        emit(SteamEvent.LoggedOut)
    }

    private fun onLicenseList(cb: LicenseListCallback) {
        Log.i(TAG, "${cb.licenseList.size} licenses received")
        synchronized(_licenses) {
            _licenses.clear()
            _licenses.addAll(cb.licenseList)
        }
        emit(SteamEvent.LibraryProgress(0, cb.licenseList.size))
        // Full PICS sync added in Phase 4
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /** Auto-login with a stored refresh token. */
    fun loginWithToken(username: String, refreshToken: String) {
        steamUser?.logOn(LogOnDetails().apply {
            this.username     = username
            this.accessToken  = refreshToken   // refreshToken goes in accessToken per JavaSteam API
            shouldRememberPassword = true
        })
    }

    /**
     * First-time credential login (blocking — run on a background thread).
     *
     * [guardCodeCallback] is invoked on the calling thread when Steam Guard is needed.
     *   type = "email" | "2fa", hint = partial email domain or null.
     *   Return the code the user entered.
     *
     * On success, refresh token stored and [loginWithToken] called.
     * On failure, [SteamEvent.LoginFailed] emitted.
     */
    fun loginWithCredentials(
        username: String,
        password: String,
        guardCodeCallback: (type: String, hint: String?) -> String,
    ) {
        val client = steamClient ?: run { emit(SteamEvent.LoginFailed("Not initialised")); return }

        Thread {
            try {
                val authenticator = object : IAuthenticator {
                    override fun getDeviceCode(previousCodeWasIncorrect: Boolean): String {
                        emit(SteamEvent.SteamGuardTwoFactorRequired)
                        return guardCodeCallback("2fa", null)
                    }
                    override fun getEmailCode(emailDomain: String?, previousCodeWasIncorrect: Boolean): String {
                        emit(SteamEvent.SteamGuardEmailRequired(emailDomain))
                        return guardCodeCallback("email", emailDomain)
                    }
                    override fun acceptDeviceConfirmation(): Boolean = false
                }

                val authDetails = AuthSessionDetails().apply {
                    this.username      = username
                    this.password      = password
                    persistentSession  = true
                    this.authenticator = authenticator
                }

                val auth    = client.authentication
                val session = auth.beginAuthSessionViaCredentials(authDetails).await()
                val result  = session.pollingWaitForResult().await()

                val resolvedName = result.accountName?.takeIf { it.isNotEmpty() } ?: username
                SteamPrefs.username     = resolvedName
                SteamPrefs.refreshToken = result.refreshToken ?: ""

                Log.i(TAG, "Credentials auth succeeded for $resolvedName")
                loginWithToken(resolvedName, SteamPrefs.refreshToken)

            } catch (e: Exception) {
                Log.e(TAG, "Credential login failed", e)
                emit(SteamEvent.LoginFailed(e.message ?: "Unknown error"))
            }
        }.start()
    }

    /**
     * QR login (blocking — run on a background thread).
     * Emits [SteamEvent.QrChallengeReceived] with the challenge URL.
     */
    fun loginWithQr() {
        val client = steamClient ?: run { emit(SteamEvent.LoginFailed("Not initialised")); return }

        Thread {
            try {
                val authDetails = AuthSessionDetails().apply { persistentSession = true }

                val auth    = client.authentication
                val session = auth.beginAuthSessionViaQR(authDetails).await()

                emit(SteamEvent.QrChallengeReceived(session.challengeUrl))

                // Wire URL refresh callback via reflection (API varies by JavaSteam version)
                try {
                    val iface = Class.forName(
                        "in.dragonbra.javasteam.steam.authentication.IChallengeUrlChanged"
                    )
                    val proxy = java.lang.reflect.Proxy.newProxyInstance(
                        iface.classLoader, arrayOf(iface)
                    ) { _, _, _ ->
                        emit(SteamEvent.QrChallengeReceived(session.challengeUrl))
                        null
                    }
                    session.javaClass.getField("challengeUrlChanged").set(session, proxy)
                } catch (_: Exception) { /* not supported in this build */ }

                val result = session.pollingWaitForResult().await()
                if (result == null) {
                    emit(SteamEvent.QrExpired)
                    return@Thread
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
        }.start()
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    fun logout() {
        steamUser?.logOff()
        SteamPrefs.clear()
        library = emptyList()
        synchronized(_licenses) { _licenses.clear() }
        Log.i(TAG, "Logged out")
    }

    // -------------------------------------------------------------------------
    // Library (Phase 4 will populate via PICS)
    // -------------------------------------------------------------------------

    fun updateLibrary(games: List<SteamGame>) {
        library = games
        emit(SteamEvent.LibrarySynced)
    }

    // -------------------------------------------------------------------------
    // Accessors for downstream phases
    // -------------------------------------------------------------------------

    fun getSteamClient(): SteamClient? = steamClient
    fun getSteamApps(): SteamApps?     = steamApps

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun emit(event: SteamEvent) {
        for (l in listeners) {
            try { l.onEvent(event) } catch (e: Exception) { Log.e(TAG, "Listener error", e) }
        }
    }
}
