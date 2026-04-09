package com.winlator.cmod.store;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;

/**
 * Singleton managing the JavaSteam SteamClient lifecycle.
 *
 * Written in Java to avoid Kotlin metadata version incompatibilities
 * (JavaSteam is compiled with Kotlin 2.2.0; the base APK Kotlin compiler is 1.9.x).
 * Java bytecode interop works regardless of Kotlin metadata version.
 *
 * Lifecycle:
 *   SteamForegroundService.onStartCommand() → SteamRepository.initialize() → .connect()
 *   SteamForegroundService.onDestroy()     → SteamRepository.disconnect()
 *
 * Events: addListener() / removeListener(). Listeners called on pump HandlerThread.
 */
public final class SteamRepository {

    private static final String TAG = "SteamRepo";

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static final SteamRepository INSTANCE = new SteamRepository();
    public static SteamRepository getInstance() { return INSTANCE; }
    private SteamRepository() {}

    // -------------------------------------------------------------------------
    // Event listener
    // -------------------------------------------------------------------------

    public interface SteamEventListener {
        void onEvent(Object event);
    }

    private final CopyOnWriteArrayList<SteamEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(SteamEventListener l)    { listeners.add(l); }
    public void removeListener(SteamEventListener l) { listeners.remove(l); }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private volatile boolean connected = false;
    private volatile boolean loggedIn  = false;
    private volatile List<Object> library = new ArrayList<>();

    public boolean isConnected() { return connected; }
    public boolean isLoggedIn()  { return loggedIn;  }
    public List<Object> getLibrary() { return library; }

    // -------------------------------------------------------------------------
    // JavaSteam instances
    // -------------------------------------------------------------------------

    private SteamClient     steamClient  = null;
    private CallbackManager manager      = null;
    private SteamUser       steamUser    = null;
    private SteamApps       steamApps    = null;

    // callback pump
    private HandlerThread pumpThread = null;
    private Handler       pumpHandler = null;
    private final AtomicBoolean pumping = new AtomicBoolean(false);

    // raw license list (for DepotDownloader Phase 5)
    private final List<Object> licenses = new ArrayList<>();
    public List<Object> getLicenses() {
        synchronized (licenses) { return new ArrayList<>(licenses); }
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /** Build SteamClient and register callbacks. Idempotent. */
    public synchronized void initialize() {
        if (steamClient != null) return;

        SteamConfiguration config = SteamConfiguration.create(b -> {
            b.withProtocolTypes(EnumSet.of(ProtocolTypes.WEB_SOCKET));
            b.withConnectionTimeout(30_000L);
        });

        steamClient = new SteamClient(config);
        manager     = new CallbackManager(steamClient);
        steamUser   = steamClient.getHandler(SteamUser.class);
        steamApps   = steamClient.getHandler(SteamApps.class);

        registerCallbacks();
        Log.i(TAG, "SteamRepository initialised");
    }

    private void registerCallbacks() {
        manager.subscribe(ConnectedCallback.class,    cb -> onConnected());
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);
        manager.subscribe(LoggedOnCallback.class,     this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class,    this::onLoggedOff);
        manager.subscribe(LicenseListCallback.class,  this::onLicenseList);
    }

    // -------------------------------------------------------------------------
    // Connection
    // -------------------------------------------------------------------------

    public void connect() {
        if (steamClient == null) { Log.e(TAG, "connect() before initialize()"); return; }
        startPump();
        steamClient.connect();
    }

    public void disconnect() {
        if (steamClient != null) steamClient.disconnect();
        stopPump();
        connected = false;
        loggedIn  = false;
    }

    private void startPump() {
        if (pumping.getAndSet(true)) return;
        pumpThread  = new HandlerThread("SteamPump");
        pumpThread.start();
        pumpHandler = new Handler(pumpThread.getLooper());
        schedulePump();
    }

    private void stopPump() {
        pumping.set(false);
        if (pumpThread != null) { pumpThread.quitSafely(); pumpThread = null; }
        pumpHandler = null;
    }

    private void schedulePump() {
        if (!pumping.get()) return;
        if (pumpHandler == null) return;
        pumpHandler.post(() -> {
            try { if (manager != null) manager.runWaitCallbacks(500L); }
            catch (Exception e) { Log.e(TAG, "Pump error", e); }
            schedulePump();
        });
    }

    // -------------------------------------------------------------------------
    // Callback handlers
    // -------------------------------------------------------------------------

    private void onConnected() {
        Log.i(TAG, "Connected to Steam CM");
        connected = true;
        emit("Connected");

        // Auto-login with stored refresh token
        if (SteamPrefs.INSTANCE.isLoggedIn()) {
            Log.i(TAG, "Auto-login as " + SteamPrefs.INSTANCE.getUsername());
            loginWithToken(SteamPrefs.INSTANCE.getUsername(), SteamPrefs.INSTANCE.getRefreshToken());
        }
    }

    private void onDisconnected(DisconnectedCallback cb) {
        Log.i(TAG, "Disconnected (userInitiated=" + cb.isUserInitiated() + ")");
        connected = false;
        loggedIn  = false;
        emit("Disconnected");
    }

    private void onLoggedOn(LoggedOnCallback cb) {
        if (cb.getResult() != EResult.OK) {
            Log.w(TAG, "Login failed: " + cb.getResult());
            emit("LoginFailed:" + cb.getResult().name());
            return;
        }

        SteamPrefs.INSTANCE.setCellId(cb.getCellID());
        long sid64 = cb.getClientSteamID().convertToUInt64().toLong();
        SteamPrefs.INSTANCE.setSteamId64(sid64);
        SteamPrefs.INSTANCE.setAccountId((int)(sid64 & 0xFFFFFFFFL));

        loggedIn = true;
        emit("LoggedIn:" + sid64 + ":" + SteamPrefs.INSTANCE.getDisplayName());
        Log.i(TAG, "Logged in as " + SteamPrefs.INSTANCE.getUsername());
    }

    private void onLoggedOff(LoggedOffCallback cb) {
        Log.i(TAG, "Logged off: " + cb.getResult());
        loggedIn = false;
        emit("LoggedOut");
    }

    private void onLicenseList(LicenseListCallback cb) {
        Log.i(TAG, cb.getLicenseList().size() + " licenses received");
        synchronized (licenses) {
            licenses.clear();
            licenses.addAll(cb.getLicenseList());
        }
        emit("LibraryProgress:0:" + cb.getLicenseList().size());
        // Full PICS sync added in Phase 4
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /** Auto-login using a stored refresh token. */
    public void loginWithToken(String username, String refreshToken) {
        if (steamUser == null) return;
        LogOnDetails details = new LogOnDetails();
        details.setUsername(username);
        details.setAccessToken(refreshToken);  // refreshToken goes in accessToken per JavaSteam
        details.setShouldRememberPassword(true);
        steamUser.logOn(details);
    }

    /**
     * First-time credential login — Phase 2 will implement full Steam auth API.
     * Stubbed for Phase 1; emits "LoginFailed:NotImplemented".
     */
    public void loginWithCredentials(String username, String password) {
        Log.w(TAG, "loginWithCredentials: not yet implemented (Phase 2)");
        emit("LoginFailed:Phase2NotImplemented");
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    public void logout() {
        if (steamUser != null) steamUser.logOff();
        SteamPrefs.INSTANCE.clear();
        library = new ArrayList<>();
        synchronized (licenses) { licenses.clear(); }
        Log.i(TAG, "Logged out");
    }

    // -------------------------------------------------------------------------
    // Library (Phase 4 will populate via PICS)
    // -------------------------------------------------------------------------

    public void updateLibrary(List<Object> games) {
        library = games;
        emit("LibrarySynced");
    }

    // -------------------------------------------------------------------------
    // Accessors for downstream phases
    // -------------------------------------------------------------------------

    public SteamClient getSteamClient() { return steamClient; }
    public SteamApps   getSteamApps()   { return steamApps;   }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void emit(String event) {
        for (SteamEventListener l : listeners) {
            try { l.onEvent(event); }
            catch (Exception e) { Log.e(TAG, "Listener error for event " + event, e); }
        }
    }
}
