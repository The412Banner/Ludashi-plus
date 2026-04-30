package com.winlator.cmod.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StoreDownloadQueue {

    public interface DownloadListener {
        void onProgress(String msg, int pct);
        void onComplete(String installDir);
        void onError(String msg);
        void onCancelled();
    }

    public static class DownloadEntry {
        public final String dlKey;
        public final String store;
        public final String title;
        public volatile int     percent;
        public volatile String  status;
        public volatile boolean active;

        DownloadEntry(String dlKey, String store, String title) {
            this.dlKey   = dlKey;
            this.store   = store;
            this.title   = title;
            this.percent = 0;
            this.status  = "Starting…";
            this.active  = true;
        }
    }

    private static final ConcurrentHashMap<String, DownloadEntry>  entries     = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, DownloadListener> listeners  = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicBoolean>  cancelFlags = new ConcurrentHashMap<>();

    public static boolean isActive(String dlKey) {
        DownloadEntry e = entries.get(dlKey);
        return e != null && e.active;
    }

    public static void cancel(Context ctx, String dlKey) {
        AtomicBoolean flag = cancelFlags.get(dlKey);
        if (flag != null) flag.set(true);
    }

    public static void addListener(String dlKey, DownloadListener l) {
        listeners.put(dlKey, l);
    }

    public static void removeListener(String dlKey) {
        listeners.remove(dlKey);
    }

    public static List<DownloadEntry> getAll() {
        return new ArrayList<>(entries.values());
    }

    // ── GOG ──────────────────────────────────────────────────────────────────

    public static void startGog(Context ctx, GogGame game, String dlKey) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        cancelFlags.put(dlKey, cancelled);
        DownloadEntry entry = new DownloadEntry(dlKey, "GOG", game.title);
        entries.put(dlKey, entry);

        GogDownloadManager.startDownload(ctx, game, new GogDownloadManager.Callback() {
            @Override public void onProgress(String msg, int pct) {
                entry.status = msg; entry.percent = pct;
                notifyProgress(dlKey, msg, pct);
            }
            @Override public void onComplete(String exePath) {
                ctx.getSharedPreferences("bh_gog_prefs", 0).edit()
                        .putString("gog_exe_" + game.gameId, exePath)
                        .putString("gog_dir_" + game.gameId, new File(exePath).getParent())
                        .apply();
                finish(dlKey, entry, false, false, null, exePath);
            }
            @Override public void onError(String msg) {
                finish(dlKey, entry, false, true, msg, null);
            }
            @Override public void onCancelled() {
                finish(dlKey, entry, true, false, null, null);
            }
        });
    }

    // ── Epic ─────────────────────────────────────────────────────────────────

    public static void startEpic(Context ctx, EpicGame game, String dlKey) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        cancelFlags.put(dlKey, cancelled);
        DownloadEntry entry = new DownloadEntry(dlKey, "EPIC", game.title);
        entries.put(dlKey, entry);

        new Thread(() -> {
            try {
                String token = EpicCredentialStore.getValidAccessToken(ctx);
                if (token == null) { finish(dlKey, entry, cancelled.get(), true, "Login required", null); return; }
                if (cancelled.get()) { finish(dlKey, entry, true, false, null, null); return; }

                notifyProgress(dlKey, "Fetching manifest…", 0);
                entry.status = "Fetching manifest…";

                String manifestJson = EpicApiClient.getManifestApiJson(
                        token, game.namespace, game.catalogItemId, game.appName);
                if (manifestJson == null) { finish(dlKey, entry, cancelled.get(), true, "Failed to fetch manifest", null); return; }
                if (cancelled.get()) { finish(dlKey, entry, true, false, null, null); return; }

                String sanitized = game.title.replaceAll("[^a-zA-Z0-9 \\-_]", "").trim();
                if (sanitized.isEmpty()) sanitized = "epic_" + game.appName.hashCode();
                File installDir = new File(new File(ctx.getFilesDir(), "imagefs/epic_games"), sanitized);
                ctx.getSharedPreferences("bh_epic_prefs", 0).edit()
                        .putString("epic_dir_" + game.appName, installDir.getAbsolutePath())
                        .apply();

                final String finalToken = token;
                boolean ok = EpicDownloadManager.install(ctx, manifestJson, finalToken,
                        installDir.getAbsolutePath(), (msg, pct) -> {
                            if (cancelled.get()) return;
                            entry.status = msg; entry.percent = pct;
                            notifyProgress(dlKey, msg, pct);
                        });

                if (cancelled.get()) { finish(dlKey, entry, true, false, null, null); return; }
                if (!ok) { finish(dlKey, entry, false, true, "Download failed", null); return; }

                List<File> exeFiles = new ArrayList<>();
                AmazonLaunchHelper.collectExe(installDir, exeFiles);
                if (exeFiles.isEmpty()) { finish(dlKey, entry, false, true, "No executable found", null); return; }

                String lower = game.title.toLowerCase();
                Collections.sort(exeFiles, (a, b) ->
                        AmazonLaunchHelper.scoreExe(b, lower) - AmazonLaunchHelper.scoreExe(a, lower));

                String exePath = exeFiles.get(0).getAbsolutePath();
                ctx.getSharedPreferences("bh_epic_prefs", 0).edit()
                        .putString("epic_exe_" + game.appName, exePath)
                        .apply();
                finish(dlKey, entry, false, false, null, exePath);
            } catch (Exception e) {
                finish(dlKey, entry, false, true, e.getMessage(), null);
            }
        }, "epic-dl-" + game.appName).start();
    }

    // ── Amazon ───────────────────────────────────────────────────────────────

    public static void startAmazon(Context ctx, AmazonGame game, String dlKey) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        cancelFlags.put(dlKey, cancelled);
        DownloadEntry entry = new DownloadEntry(dlKey, "AMAZON", game.title);
        entries.put(dlKey, entry);

        new Thread(() -> {
            try {
                String token = AmazonCredentialStore.getValidAccessToken(ctx);
                if (token == null) { finish(dlKey, entry, cancelled.get(), true, "Login required", null); return; }
                if (cancelled.get()) { finish(dlKey, entry, true, false, null, null); return; }

                String sanitized = game.title.replaceAll("[^a-zA-Z0-9 \\-_]", "").trim();
                if (sanitized.isEmpty()) sanitized = "game_" + game.productId.hashCode();
                File installDir = new File(new File(ctx.getFilesDir(), "imagefs/Amazon"), sanitized);
                ctx.getSharedPreferences("bh_amazon_prefs", 0).edit()
                        .putString("amazon_dir_" + game.productId, installDir.getAbsolutePath())
                        .apply();

                boolean ok = AmazonDownloadManager.install(ctx, game, token, installDir,
                        (dl, total, file) -> {
                            if (cancelled.get()) return;
                            int pct = (total > 0) ? (int) (dl * 100L / total) : 0;
                            String name = (file != null && !file.isEmpty()) ? file : "Downloading…";
                            entry.status = name; entry.percent = pct;
                            notifyProgress(dlKey, name, pct);
                        },
                        cancelled::get);

                if (cancelled.get()) { finish(dlKey, entry, true, false, null, null); return; }
                if (!ok) { finish(dlKey, entry, false, true, "Download failed", null); return; }

                List<File> exeFiles = new ArrayList<>();
                AmazonLaunchHelper.collectExe(installDir, exeFiles);
                if (exeFiles.isEmpty()) { finish(dlKey, entry, false, true, "No executable found", null); return; }

                String lower = game.title.toLowerCase();
                Collections.sort(exeFiles, (a, b) ->
                        AmazonLaunchHelper.scoreExe(b, lower) - AmazonLaunchHelper.scoreExe(a, lower));

                String exePath = exeFiles.get(0).getAbsolutePath();
                ctx.getSharedPreferences("bh_amazon_prefs", 0).edit()
                        .putString("amazon_exe_" + game.productId, exePath)
                        .apply();
                finish(dlKey, entry, false, false, null, exePath);
            } catch (Exception e) {
                finish(dlKey, entry, false, true, e.getMessage(), null);
            }
        }, "amazon-dl-" + game.productId).start();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static void notifyProgress(String dlKey, String msg, int pct) {
        DownloadListener l = listeners.get(dlKey);
        if (l != null) l.onProgress(msg, pct);
    }

    private static void finish(String dlKey, DownloadEntry entry,
                                boolean wasCancelled, boolean wasError,
                                String errorMsg, String completePath) {
        entry.active = false;
        cancelFlags.remove(dlKey);
        DownloadListener l = listeners.get(dlKey);
        if (wasCancelled) {
            entry.status = "Cancelled";
            if (l != null) l.onCancelled();
        } else if (wasError) {
            entry.status = "Error: " + errorMsg;
            if (l != null) l.onError(errorMsg);
        } else {
            entry.status = "Complete";
            entry.percent = 100;
            if (l != null) l.onComplete(completePath);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> entries.remove(dlKey), 60_000L);
    }
}
