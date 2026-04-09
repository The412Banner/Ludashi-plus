package com.winlator.cmod.store;

import android.content.Context;
import android.content.SharedPreferences;
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
 * Written in Java (not Kotlin) to avoid Kotlin metadata version
 * incompatibilities: JavaSteam is compiled with Kotlin 2.2.0 while
 * the base APK's Kotlin runtime is 1.9.24.  Java bytecode interop
 * bypasses all metadata version checks.
 *
 * Self-contained: uses SharedPreferences directly (no dependency on
 * SteamPrefs.kt which is compiled in a later Kotlin step).
 *
 * Lifecycle:
 *   SteamForegroundService.onStartCommand()
 *     → SteamRepository.getInstance().initialize(ctx)
 *     → SteamRepository.getInstance().connect()
 *   SteamForegroundService.onDestroy()
 *     → SteamRepository.getInstance().disconnect()
 */
public final class SteamRepository {

    private static final String TAG        = "SteamRepo";
    private static final String PREFS_NAME = "steam_prefs";

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
        void onEvent(String event);
    }

    private final CopyOnWriteArrayList<SteamEventListener> listeners =
            new CopyOnWriteArrayList<>();

    public void addListener(SteamEventListener l)    { listeners.add(l); }
    public void removeListener(SteamEventListener l) { listeners.remove(l); }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private volatile boolean connected = false;
    private volatile boolean loggedIn  = false;

    public boolean isConnected() { return connected; }
    public boolean isLoggedIn()  { return loggedIn; }

    // -------------------------------------------------------------------------
    // SharedPreferences (set on initialize)
    // -------------------------------------------------------------------------

    private Context appContext = null;
    private SharedPreferences prefs = null;

    private String  pGet(String key, String  def) { return prefs != null ? prefs.getString(key, def)  : def; }
    private long    pGet(String key, long    def) { return prefs != null ? prefs.getLong(key, def)    : def; }
    private int     pGet(String key, int     def) { return prefs != null ? prefs.getInt(key, def)     : def; }

    private void    pPut(String key, String v)  { if (prefs != null) prefs.edit().putString(key, v).apply(); }
    private void    pPut(String key, long v)    { if (prefs != null) prefs.edit().putLong(key, v).apply(); }
    private void    pPut(String key, int v)     { if (prefs != null) prefs.edit().putInt(key, v).apply(); }

    private boolean isLoggedInPrefs() {
        return !pGet("refresh_token", "").isEmpty() && !pGet("username", "").isEmpty();
    }

    // -------------------------------------------------------------------------
    // JavaSteam instances
    // -------------------------------------------------------------------------

    private SteamClient     steamClient  = null;
    private CallbackManager manager      = null;
    private SteamUser       steamUser    = null;
    private SteamApps       steamApps    = null;

    private HandlerThread     pumpThread  = null;
    private Handler           pumpHandler = null;
    private final AtomicBoolean pumping   = new AtomicBoolean(false);

    // raw licenses for DepotDownloader (Phase 5)
    private final List<Object> licenses = new ArrayList<>();
    public List<Object> getLicenses() {
        synchronized (licenses) { return new ArrayList<>(licenses); }
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /** Build SteamClient and register callbacks. Idempotent. */
    public synchronized void initialize(Context ctx) {
        if (appContext == null) {
            appContext = ctx.getApplicationContext();
        }
        if (prefs == null) {
            prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        SteamDatabase.getInstance(appContext);
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
        if (!pumping.get() || pumpHandler == null) return;
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

        if (isLoggedInPrefs()) {
            Log.i(TAG, "Auto-login as " + pGet("username", ""));
            loginWithToken(pGet("username", ""), pGet("refresh_token", ""));
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

        pPut("cell_id", cb.getCellID());
        long sid64 = cb.getClientSteamID().convertToUInt64();
        pPut("steam_id_64", sid64);
        pPut("account_id", (int)(sid64 & 0xFFFFFFFFL));

        loggedIn = true;
        emit("LoggedIn:" + sid64);
        Log.i(TAG, "Logged in as " + pGet("username", ""));
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
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /** Auto-login using a stored refresh token. */
    public void loginWithToken(String username, String refreshToken) {
        if (steamUser == null) return;
        LogOnDetails details = new LogOnDetails();
        details.setUsername(username);
        details.setAccessToken(refreshToken);  // refreshToken goes in accessToken field
        details.setShouldRememberPassword(true);
        steamUser.logOn(details);
    }

    /**
     * Persist credentials returned from the Steam auth session
     * (called from Phase 2 auth flow after pollingWaitForResult).
     */
    public void saveSession(String username, String refreshToken) {
        pPut("username", username);
        pPut("refresh_token", refreshToken);
    }

    /**
     * First-time credential login — stub for Phase 1.
     * Phase 2 will implement the full SteamAuthentication API flow.
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
        if (prefs != null) {
            prefs.edit()
                .remove("username").remove("refresh_token")
                .remove("steam_id_64").remove("account_id")
                .remove("display_name").remove("last_pics_change")
                .apply();
        }
        synchronized (licenses) { licenses.clear(); }
        Log.i(TAG, "Logged out");
        emit("LoggedOut");
    }

    // -------------------------------------------------------------------------
    // Accessors for downstream phases
    // -------------------------------------------------------------------------

    public SteamClient   getSteamClient() { return steamClient; }
    public SteamApps     getSteamApps()   { return steamApps; }
    public SteamDatabase getDatabase()    { return SteamDatabase.getInstance(); }

    public String getUsername()     { return pGet("username", ""); }
    public String getRefreshToken() { return pGet("refresh_token", ""); }
    public long   getSteamId64()    { return pGet("steam_id_64", 0L); }
    public int    getAccountId()    { return pGet("account_id", 0); }
    public String getDisplayName()  { return pGet("display_name", ""); }
    public void   setDisplayName(String name) { pPut("display_name", name); }

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
