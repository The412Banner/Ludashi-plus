package com.winlator.cmod.store

import android.app.Activity
import android.graphics.Bitmap
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.net.URL

/**
 * Game detail screen — shows header art, metadata, Install/Launch buttons.
 *
 * Phase 5: display-only with stubbed Install action (download engine Phase 6).
 * Launch button enabled only when isInstalled=true (Phase 7).
 */
class SteamGameDetailActivity : Activity(), SteamRepository.SteamEventListener {

    companion object {
        const val EXTRA_APP_ID = "steam_app_id"
    }

    private val ui = Handler(Looper.getMainLooper())
    private var appId: Int = 0
    private var game: SteamGame? = null

    // views updated after load
    private lateinit var headerImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var typeText: TextView
    private lateinit var sizeText: TextView
    private lateinit var statusText: TextView
    private lateinit var installBtn: Button
    private lateinit var launchBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appId = intent.getIntExtra(EXTRA_APP_ID, 0)
        if (appId == 0) { finish(); return }

        setContentView(buildUI())
        SteamRepository.getInstance().addListener(this)
        loadGame()
    }

    override fun onDestroy() {
        SteamRepository.getInstance().removeListener(this)
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // SteamRepository.SteamEventListener
    // -------------------------------------------------------------------------

    override fun onEvent(event: String) {
        when {
            event.startsWith("DownloadProgress:") -> {
                val parts = event.split(":")
                val id    = parts.getOrNull(1)?.toIntOrNull() ?: return
                if (id != appId) return
                val done  = parts.getOrNull(2)?.toLongOrNull() ?: 0L
                val total = parts.getOrNull(3)?.toLongOrNull() ?: 1L
                val pct   = if (total > 0) (done * 100 / total).toInt() else 0
                ui.post {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress   = pct
                    progressText.visibility = View.VISIBLE
                    progressText.text = "Downloading… $pct%  (${fmtSize(done)} / ${fmtSize(total)})"
                    installBtn.isEnabled = false
                    installBtn.text = "Downloading…"
                }
            }
            event.startsWith("DownloadComplete:") -> {
                val id = event.substringAfter("DownloadComplete:").toIntOrNull() ?: return
                if (id != appId) return
                ui.post { loadGame() }
            }
            event.startsWith("DownloadFailed:") -> {
                val parts = event.split(":")
                val id = parts.getOrNull(1)?.toIntOrNull() ?: return
                if (id != appId) return
                val reason = parts.drop(2).joinToString(":")
                ui.post {
                    progressBar.visibility  = View.GONE
                    progressText.visibility = View.GONE
                    statusText.text = "Download failed: $reason"
                    statusText.setTextColor(Color.parseColor("#FF5555"))
                    installBtn.isEnabled = true
                    installBtn.text = "Retry"
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private fun loadGame() {
        val row = SteamRepository.getInstance().database.getGame(appId)
        if (row == null) { finish(); return }
        game = SteamGame.fromGameRow(row)
        refreshUI()
        loadHeaderImage()

        // Check for an active download
        val dlRow = SteamRepository.getInstance().database.getDownload(appId)
        if (dlRow != null && dlRow.status == SteamDatabase.DL_DOWNLOADING) {
            val pct = if (dlRow.bytesTotal > 0) (dlRow.bytesDownloaded * 100 / dlRow.bytesTotal).toInt() else 0
            progressBar.visibility  = View.VISIBLE
            progressBar.progress    = pct
            progressText.visibility = View.VISIBLE
            progressText.text = "Downloading… $pct%"
            installBtn.isEnabled = false
            installBtn.text = "Downloading…"
        }
    }

    private fun refreshUI() {
        val g = game ?: return
        nameText.text = g.name.ifEmpty { "App ${g.appId}" }
        typeText.text = g.type.uppercase()
        typeText.setTextColor(if (g.type == "game") Color.parseColor("#4CAF50") else Color.parseColor("#FF9800"))
        sizeText.text = if (g.sizeBytes > 0) "~${fmtSize(g.sizeBytes)}" else "Size unknown"

        if (g.isInstalled) {
            statusText.text = "Installed"
            statusText.setTextColor(Color.parseColor("#4CAF50"))
            installBtn.text = "Uninstall"
            installBtn.setBackgroundColor(Color.parseColor("#B71C1C"))
            launchBtn.isEnabled = true
            launchBtn.alpha = 1f
        } else {
            statusText.text = "Not installed"
            statusText.setTextColor(Color.parseColor("#AAAAAA"))
            installBtn.text = "Install"
            installBtn.setBackgroundColor(Color.parseColor("#1565C0"))
            launchBtn.isEnabled = false
            launchBtn.alpha = 0.4f
        }
    }

    private fun loadHeaderImage() {
        val url = game?.headerUrl ?: return
        Thread {
            try {
                val bmp: Bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                ui.post { headerImage.setImageBitmap(bmp) }
            } catch (_: Exception) {}
        }.start()
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    private fun onInstallClicked() {
        val g = game ?: return
        if (g.isInstalled) {
            // Uninstall — remove from DB and delete files
            SteamRepository.getInstance().database.markUninstalled(appId)
            if (g.installDir.isNotEmpty()) {
                Thread { File(g.installDir).deleteRecursively() }.start()
            }
            loadGame()
        } else {
            installBtn.isEnabled = false
            installBtn.text = "Starting…"
            SteamDepotDownloader.getInstance().installApp(appId, applicationContext)
        }
    }

    private fun onLaunchClicked() {
        val g = game ?: return
        if (!g.isInstalled || g.installDir.isEmpty()) {
            Toast.makeText(this, "Game not installed", Toast.LENGTH_SHORT).show()
            return
        }
        val exePath = findExe(File(g.installDir))
        if (exePath == null) {
            Toast.makeText(this, "No executable found in install directory", Toast.LENGTH_LONG).show()
            return
        }
        com.winlator.cmod.store.LudashiLaunchBridge.addToLauncher(this, g.name, exePath)
    }

    /**
     * Scan for the best .exe in the install directory tree.
     * Skips redistributable paths. Returns the largest .exe found,
     * or null if none exist.
     */
    private fun findExe(root: File): String? {
        if (!root.exists() || !root.isDirectory) return null
        val skip = setOf("redist", "redistribut", "_commonredist", "directx", "vcredist", "__installer")
        return root.walkTopDown()
            .filter { f ->
                f.isFile &&
                f.name.endsWith(".exe", ignoreCase = true) &&
                skip.none { f.absolutePath.lowercase().contains(it) }
            }
            .maxByOrNull { it.length() }
            ?.absolutePath
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private fun buildUI(): View {
        val scroll = android.widget.ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#1B1B1B"))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(root)

        // Back button header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.parseColor("#212121"))
        }
        val backBtn = Button(this).apply {
            text = "← Back"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }
        header.addView(backBtn)
        root.addView(header)

        // Header image (16:7 aspect ratio approximation)
        headerImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#0D47A1"))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(180))
        }
        root.addView(headerImage)

        // Info section
        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }

        nameText = TextView(this).apply {
            text = "Loading…"
            textSize = 22f
            setTextColor(Color.WHITE)
        }
        info.addView(nameText)

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
        }
        typeText = TextView(this).apply {
            text = "GAME"
            textSize = 11f
            setPadding(dp(6), dp(2), dp(6), dp(2))
            setBackgroundColor(Color.parseColor("#263238"))
        }
        sizeText = TextView(this).apply {
            text = "Size unknown"
            textSize = 12f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(dp(12), 0, 0, 0)
        }
        row1.addView(typeText)
        row1.addView(sizeText)
        info.addView(row1)

        statusText = TextView(this).apply {
            text = "Not installed"
            textSize = 12f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(0, dp(4), 0, dp(12))
        }
        info.addView(statusText)
        root.addView(info)

        // Progress bar (hidden until download starts)
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
            setPadding(dp(16), 0, dp(16), 0)
        }
        root.addView(progressBar, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)))

        progressText = TextView(this).apply {
            text = ""
            textSize = 11f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(dp(16), dp(2), dp(16), dp(4))
            visibility = View.GONE
        }
        root.addView(progressText)

        // Buttons
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }

        installBtn = Button(this).apply {
            text = "Install"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1565C0"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            }
            setOnClickListener { onInstallClicked() }
        }

        launchBtn = Button(this).apply {
            text = "Launch"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2E7D32"))
            isEnabled = false
            alpha = 0.4f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { onLaunchClicked() }
        }

        btnRow.addView(installBtn)
        btnRow.addView(launchBtn)
        root.addView(btnRow)

        return scroll
    }

    private fun fmtSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
            else                    -> "%.0f KB".format(bytes / 1024.0)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
