package com.winlator.cmod.store

import android.content.Context
import android.util.Log
import `in`.dragonbra.javasteam.depotdownloader.DepotDownloader
import `in`.dragonbra.javasteam.depotdownloader.IDownloadListener
import `in`.dragonbra.javasteam.depotdownloader.data.AppItem
import `in`.dragonbra.javasteam.depotdownloader.data.DownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicLong

/**
 * Steam depot download engine — uses JavaSteam's built-in DepotDownloader.
 *
 * Replaces the hand-rolled HTTP approach. DepotDownloader handles:
 *   - manifest request codes (CM connection)
 *   - CDN auth tokens (CM connection)
 *   - depot key requests (CM connection)
 *   - chunk downloading via Ktor CIO HTTP
 *   - AES-ECB decryption + VZip/LZMA decompression
 */
object SteamDepotDownloader {

    private const val TAG = "SteamDepot"

    // -------------------------------------------------------------------------
    // Debug log — written to getExternalFilesDir/steam_debug.txt
    // -------------------------------------------------------------------------

    private var debugLogFile: File? = null
    val debugLogPath: String get() = debugLogFile?.absolutePath ?: "(not initialized)"

    private fun initDebugLog(ctx: Context) {
        try {
            val dir = ctx.getExternalFilesDir(null)
            if (dir != null) {
                debugLogFile = File(dir, "steam_debug.txt")
                BufferedWriter(FileWriter(debugLogFile!!, false)).use { w ->
                    w.write("=== Steam DepotDownloader Debug Log (JavaSteam native) ===\n")
                    w.write("Engine: JavaSteam DepotDownloader (Ktor CIO)\n")
                    w.write("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n\n")
                }
                dlog("Debug log: ${debugLogFile!!.absolutePath}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not create debug log: ${e.message}")
        }
    }

    private fun dlog(msg: String) {
        Log.i(TAG, msg)
        debugLogFile ?: return
        try {
            BufferedWriter(FileWriter(debugLogFile!!, true)).use { w ->
                val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
                w.write("[$ts] $msg\n")
            }
        } catch (_: Exception) {}
    }

    private fun dlogError(msg: String, t: Throwable) {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        dlog("$msg: ${t.message}")
        dlog("Stack: $sw")
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Java-compatible singleton accessor. */
    @JvmStatic fun getInstance(): SteamDepotDownloader = this

    fun installApp(appId: Int, ctx: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            runInstall(appId, ctx)
        }
    }

    // -------------------------------------------------------------------------
    // Core install logic
    // -------------------------------------------------------------------------

    private fun runInstall(appId: Int, ctx: Context) {
        initDebugLog(ctx)
        dlog("=== Starting install: appId=$appId ===")

        val repo = SteamRepository.getInstance()
        val steamClient = repo.steamClient
        if (steamClient == null) {
            dlog("FAIL: SteamClient is null — not connected to Steam")
            emitFailed(appId, "Not connected to Steam")
            return
        }
        dlog("SteamClient: connected=${repo.isConnected}, loggedIn=${repo.isLoggedIn}")

        val licenses = repo.getLicenses()
        dlog("Licenses: ${licenses.size} entries")
        if (licenses.isEmpty()) {
            dlog("WARNING: license list is empty — DepotDownloader may not find any depots")
        }

        val db = repo.database
        val row = db.getGame(appId)
        if (row == null) {
            dlog("FAIL: appId=$appId not found in database")
            emitFailed(appId, "Game not found in database")
            return
        }
        dlog("Game: name='${row.name}' type=${row.type} sizeBytes=${row.sizeBytes}")

        // Sanitise game name for directory usage
        val safeName = row.name.replace(Regex("[/\\\\:*?\"<>|]"), "_").trim()
        val installDir = File(ctx.getExternalFilesDir(null), "steam_games/$safeName")
        dlog("Install dir: ${installDir.absolutePath}")

        // total bytes from PICS size data (falls back to depot manifest sum)
        val totalExpected: Long = if (row.sizeBytes > 0L) {
            row.sizeBytes
        } else {
            db.getDepotManifests(appId).sumOf { it.sizeBytes }.let { if (it > 0L) it else 1L }
        }
        dlog("Expected total: ${fmtSize(totalExpected)}")

        // Queue in DB so UI shows progress
        db.queueDownload(appId, totalExpected, installDir.absolutePath)

        // Track bytes across all depots (DepotDownloader reports per-depot %)
        val bytesDownloaded = AtomicLong(0L)

        dlog("Constructing DepotDownloader(androidEmulation=true, maxDownloads=4, maxDecompress=4, debug=true)")
        val downloader = try {
            DepotDownloader(
                steamClient = steamClient,
                licenses = licenses,
                debug = true,
                androidEmulation = true,   // forces Windows OS filter — essential for games
                maxDownloads = 4,
                maxDecompress = 4,
                autoStartDownload = false,
            )
        } catch (e: Exception) {
            dlog("FAIL: DepotDownloader constructor threw")
            dlogError("DepotDownloader()", e)
            emitFailed(appId, "DepotDownloader init failed: ${e.message}")
            return
        }
        dlog("DepotDownloader constructed OK")

        downloader.addListener(object : IDownloadListener {
            override fun onDownloadStarted(item: DownloadItem) {
                dlog("onDownloadStarted: appId=${item.appId}")
                repo.emit("DownloadProgress:$appId:0:$totalExpected")
            }

            override fun onStatusUpdate(message: String) {
                dlog("Status: $message")
            }

            override fun onFileCompleted(depotId: Int, fileName: String, depotPercentComplete: Float) {
                val pct = (depotPercentComplete * 100).toInt()
                dlog("File done: depot=$depotId pct=$pct% file=$fileName")
            }

            override fun onChunkCompleted(
                depotId: Int,
                depotPercentComplete: Float,
                compressedBytes: Long,
                uncompressedBytes: Long,
            ) {
                // uncompressedBytes is cumulative per-depot; update if it grew
                val prev = bytesDownloaded.get()
                if (uncompressedBytes > prev) bytesDownloaded.set(uncompressedBytes)
                val done = bytesDownloaded.get()
                val pct = (depotPercentComplete * 100).toInt()
                dlog("Chunk: depot=$depotId pct=$pct% cumulative=${fmtSize(done)}/${fmtSize(totalExpected)}")
                repo.emit("DownloadProgress:$appId:$done:$totalExpected")
                db.updateDownloadProgress(appId, done)
            }

            override fun onDepotCompleted(depotId: Int, compressedBytes: Long, uncompressedBytes: Long) {
                dlog("Depot $depotId complete: ${fmtSize(uncompressedBytes)} uncompressed / ${fmtSize(compressedBytes)} compressed")
            }

            override fun onDownloadCompleted(item: DownloadItem) {
                dlog("=== Download complete: appId=${item.appId} ===")
                val finalBytes = bytesDownloaded.get()
                db.markInstalled(appId, installDir.absolutePath, finalBytes)
                repo.emit("DownloadComplete:$appId")
            }

            override fun onDownloadFailed(item: DownloadItem, error: Throwable) {
                dlog("=== Download FAILED: appId=${item.appId} ===")
                dlogError("onDownloadFailed", error)
                emitFailed(appId, error.message ?: "Unknown error")
            }
        })

        val item = AppItem(
            appId = appId,
            installDirectory = installDir.absolutePath,
            branch = "public",
            // Depot list intentionally empty: DepotDownloader selects
            // Windows-compatible depots automatically via license data.
        )
        dlog("Adding AppItem: appId=${item.appId} branch=${item.branch} dir=${item.installDirectory}")
        downloader.add(item)
        downloader.finishAdding()

        dlog("Calling startDownloading()...")
        try {
            downloader.startDownloading()
            dlog("startDownloading() returned (download loop running in background)")
        } catch (e: Exception) {
            dlog("FAIL: startDownloading() threw")
            dlogError("startDownloading", e)
            emitFailed(appId, "startDownloading failed: ${e.message}")
            try { downloader.close() } catch (_: Exception) {}
            return
        }

        dlog("Blocking on getCompletion().get()...")
        try {
            downloader.getCompletion().get()
            dlog("getCompletion() returned — download finished")
        } catch (e: ExecutionException) {
            // onDownloadFailed callback should have fired already
            dlog("getCompletion() ExecutionException (onDownloadFailed already called)")
            dlogError("ExecutionException.cause", e.cause ?: e)
        } catch (e: InterruptedException) {
            dlog("getCompletion() interrupted: ${e.message}")
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            dlog("getCompletion() unexpected exception")
            dlogError("getCompletion unexpected", e)
        } finally {
            dlog("Closing DepotDownloader")
            try { downloader.close() } catch (_: Exception) {}
            dlog("=== runInstall() finished ===")
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun emitFailed(appId: Int, reason: String) {
        SteamRepository.getInstance().database.markDownloadFailed(appId, reason)
        SteamRepository.getInstance().emit("DownloadFailed:$appId:$reason")
        Log.e(TAG, "DownloadFailed $appId: $reason")
    }

    private fun fmtSize(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
        else                    -> "%.0f KB".format(bytes / 1024.0)
    }
}
