package com.winlator.cmod.store

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.LruCache
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.FrameLayout
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Steam library screen — collapsible cards matching Epic/GOG/Amazon style.
 *
 * View modes: LIST (collapsible cards) · GRID · POSTER
 * Install controls are inline in each card (no separate detail activity for install).
 * SteamGameDetailActivity is still accessible via the "Details" button in the expanded section.
 */
class SteamGamesActivity : Activity(), SteamRepository.SteamEventListener {

    private val ui = Handler(Looper.getMainLooper())
    private lateinit var syncText: TextView
    private lateinit var gameListLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var refreshBtn: Button
    private lateinit var viewToggleBtn: Button
    private lateinit var searchBar: EditText

    private var allGames: List<SteamGame> = emptyList()
    private var expandedCard: LinearLayout? = null
    private var expandedArrow: TextView? = null
    private var viewMode: String = "list"

    // Per-card progress/complete callbacks keyed by appId
    private val progressUpdaters = mutableMapOf<Int, (done: Long, total: Long) -> Unit>()
    private val completeUpdaters = mutableMapOf<Int, () -> Unit>()
    // Active cancel handles — used to restore Cancel state when cards are rebuilt mid-download
    private val cancelRefs = mutableMapOf<Int, Runnable?>()

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewMode = getSharedPreferences(PREFS_NAME, 0).getString(VIEW_MODE_KEY, "list") ?: "list"
        buildUi()
        SteamRepository.getInstance().addListener(this)

        val cached = loadGames()
        if (cached.isNotEmpty()) {
            applyFilter("")
            val n = cached.size
            syncText.text = "$n game${if (n == 1) "" else "s"} — cached  •  tap ↺ to refresh"
        } else {
            val repo = SteamRepository.getInstance()
            if (repo.isLoggedIn) {
                syncText.text = "Syncing library…"
                repo.syncLibrary()
            } else {
                syncText.text = "Not logged in"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case install state changed in SteamGameDetailActivity
        val fresh = loadGames()
        applyFilter(searchBar.text?.toString() ?: "")
        if (fresh.isNotEmpty() && syncText.text.startsWith("Loading")) {
            val n = fresh.size
            syncText.text = "$n game${if (n == 1) "" else "s"} in library"
        }
    }

    override fun onDestroy() {
        SteamRepository.getInstance().removeListener(this)
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SteamRepository.SteamEventListener
    // ─────────────────────────────────────────────────────────────────────────

    override fun onEvent(event: String) {
        when {
            event.startsWith("LibraryProgress:") -> {
                val parts = event.split(":")
                val phase = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val count = parts.getOrNull(2)?.toIntOrNull() ?: 0
                ui.post {
                    syncText.text = if (phase == 0) "Syncing packages ($count)…"
                                    else "Fetching $count app records…"
                }
            }
            event.startsWith("LibrarySynced:") -> {
                ui.post {
                    val games = loadGames()
                    applyFilter(searchBar.text?.toString() ?: "")
                    syncText.text = "${games.size} games in library"
                    refreshBtn.isEnabled = true
                }
            }
            event.startsWith("DownloadProgress:") -> {
                val parts = event.split(":")
                val appId = parts.getOrNull(1)?.toIntOrNull() ?: return
                val done  = parts.getOrNull(2)?.toLongOrNull() ?: 0L
                val total = parts.getOrNull(3)?.toLongOrNull() ?: 1L
                ui.post { progressUpdaters[appId]?.invoke(done, total) }
            }
            event.startsWith("DownloadComplete:") -> {
                val appId = event.substringAfter("DownloadComplete:").toIntOrNull() ?: return
                ui.post {
                    completeUpdaters[appId]?.invoke()
                    cancelRefs.remove(appId)
                    val games = loadGames()
                    val n = games.size
                    syncText.text = "$n game${if (n == 1) "" else "s"} in library"
                }
            }
            event.startsWith("DownloadFailed:") -> {
                val parts = event.split(":", limit = 3)
                val appId = parts.getOrNull(1)?.toIntOrNull() ?: return
                val reason = parts.getOrNull(2) ?: "Unknown error"
                ui.post {
                    progressUpdaters[appId]?.invoke(-1L, 0L) // signal error
                    cancelRefs.remove(appId)
                    Toast.makeText(this, "Download failed: $reason", Toast.LENGTH_LONG).show()
                }
            }
            event.startsWith("DownloadCancelled:") -> {
                val appId = event.substringAfter("DownloadCancelled:").toIntOrNull() ?: return
                ui.post {
                    cancelRefs.remove(appId)
                    loadGames()
                    applyFilter(searchBar.text?.toString() ?: "")
                }
            }
            event == "LoggedOut" -> ui.post { finish() }
            event == "Disconnected" -> ui.post { syncText.text = "Disconnected — reconnecting…" }
            event == "Connected" -> {
                val repo = SteamRepository.getInstance()
                if (allGames.isEmpty() && repo.isLoggedIn) {
                    ui.post { syncText.text = "Reconnected — syncing library…" }
                    repo.syncLibrary()
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadGames(): List<SteamGame> {
        return try {
            val rows = SteamRepository.getInstance().getCachedGameRows()
            val games = rows
                .filter { it.type == "game" }
                .map { SteamGame.fromGameRow(it) }
                .sortedBy { it.name.lowercase() }
            allGames = games
            games
        } catch (e: IllegalStateException) {
            startActivity(Intent(this, SteamMainActivity::class.java))
            finish()
            emptyList()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter + render
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyFilter(query: String) {
        val q = query.trim()
        val filtered = if (q.isEmpty()) allGames
                       else allGames.filter { it.name.contains(q, ignoreCase = true) }
        ui.post {
            gameListLayout.removeAllViews()
            progressUpdaters.clear()
            completeUpdaters.clear()
            expandedCard  = null
            expandedArrow = null

            if (filtered.isEmpty()) {
                val emptyTV = TextView(this).apply {
                    text = if (q.isEmpty()) "No games found.\nTap ↺ to sync your library."
                           else "No results for \"$q\""
                    setTextColor(0xFF666666.toInt())
                    textSize = 14f
                    gravity = Gravity.CENTER
                }
                gameListLayout.addView(emptyTV,
                    LinearLayout.LayoutParams(-1, -2).also { it.topMargin = dp(32) })
            } else when (viewMode) {
                "grid"   -> { gameListLayout.setPadding(dp(4), dp(4), dp(4), dp(4)); addGamesAsGrid(filtered, 105, dp(3), dp(6)) }
                "poster" -> { gameListLayout.setPadding(dp(4), dp(4), dp(4), dp(4)); addGamesAsGrid(filtered, 176, dp(10), dp(10)) }
                else     -> { gameListLayout.setPadding(dp(8), dp(8), dp(8), dp(8)); filtered.forEach { addGameCard(it) } }
            }
            scrollView.visibility = View.VISIBLE
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIST card
    // ─────────────────────────────────────────────────────────────────────────

    private fun addGameCard(game: SteamGame) {
        val isInstalled  = game.isInstalled
        val isDownloading = cancelRefs.containsKey(game.appId)

        val cardBg = GradientDrawable().apply {
            setColor(COLOR_CARD_BG); cornerRadius = dp(6).toFloat()
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = cardBg
            isFocusable = true
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            setOnFocusChangeListener { _, f ->
                cardBg.setColor(if (f) 0xFF1A1F2A.toInt() else COLOR_CARD_BG)
                cardBg.setStroke(if (f) dp(3) else 0, if (f) 0xFFFFD700.toInt() else 0)
            }
        }
        val cardLp = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }

        // ── Collapsed header ──────────────────────────────────────────────────
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val coverIV = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                setColor(0xFF141820.toInt()); cornerRadius = dp(4).toFloat()
            }
        }
        topRow.addView(coverIV,
            LinearLayout.LayoutParams(dp(60), dp(60)).also { it.rightMargin = dp(10) })
        loadCoverArt(coverIV, game.appId)

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleTV = TextView(this).apply {
            text = game.name.ifEmpty { "App ${game.appId}" }
            setTextColor(0xFFFFFFFF.toInt()); textSize = 15f
            setTypeface(null, Typeface.BOLD)
            maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END
        }
        titleRow.addView(titleTV, LinearLayout.LayoutParams(-2, -2))

        val collapsedCheckTV = TextView(this).apply {
            text = " ✓"; setTextColor(0xFF4CAF50.toInt()); textSize = 14f
            setTypeface(null, Typeface.BOLD)
            visibility = if (isInstalled) View.VISIBLE else View.GONE
        }
        titleRow.addView(collapsedCheckTV, LinearLayout.LayoutParams(-2, -2))
        titleRow.addView(View(this), LinearLayout.LayoutParams(0, 0, 1f))

        val infoCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            addView(titleRow, LinearLayout.LayoutParams(-1, -2))
        }
        if (game.sizeBytes > 0) {
            val sizeTV = TextView(this).apply {
                text = fmtSize(game.sizeBytes)
                setTextColor(0xFF888888.toInt()); textSize = 11f
                maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END
            }
            infoCol.addView(sizeTV, LinearLayout.LayoutParams(-1, -2))
        }
        topRow.addView(infoCol, LinearLayout.LayoutParams(0, -2, 1f))

        val arrowTV = TextView(this).apply {
            text = "▼"; setTextColor(0xFF888888.toInt()); textSize = 14f
            setPadding(dp(8), 0, 0, 0)
        }
        topRow.addView(arrowTV, LinearLayout.LayoutParams(-2, -2))
        card.addView(topRow, LinearLayout.LayoutParams(-1, -2))

        // ── Expandable section ────────────────────────────────────────────────
        val expandSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val checkmark = TextView(this).apply {
            text = "✓ Installed"; setTextColor(0xFF4CAF50.toInt()); textSize = 10f
            visibility = if (isInstalled) View.VISIBLE else View.GONE
        }
        expandSection.addView(checkmark,
            LinearLayout.LayoutParams(-1, -2).also { it.topMargin = dp(4) })

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100; progress = 0
            visibility = if (isDownloading) View.VISIBLE else View.GONE
            progressDrawable.setColorFilter(COLOR_STEAM, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        expandSection.addView(progressBar,
            LinearLayout.LayoutParams(-1, dp(6)).also { it.topMargin = dp(6) })

        val pctTV = TextView(this).apply {
            setTextColor(COLOR_STEAM); textSize = 12f
            setTypeface(null, Typeface.BOLD)
            text = if (isDownloading) "…%" else ""
            visibility = if (isDownloading) View.VISIBLE else View.GONE
        }
        expandSection.addView(pctTV, LinearLayout.LayoutParams(-2, -2))

        val statusTV = TextView(this).apply {
            setTextColor(0xFFAAAAAA.toInt()); textSize = 11f
            text = if (isDownloading) "Downloading…" else ""
            visibility = if (isDownloading) View.VISIBLE else View.GONE
        }
        expandSection.addView(statusTV,
            LinearLayout.LayoutParams(-1, -2).also { it.topMargin = dp(2) })

        val actionBtn = Button(this).apply {
            text = when {
                isDownloading -> "Cancel"
                isInstalled   -> "Add to Launcher"
                else          -> "Install"
            }
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(when {
                isDownloading -> COLOR_CANCEL
                isInstalled   -> COLOR_LAUNCH
                else          -> COLOR_INSTALL
            })
            textSize = 13f
        }
        expandSection.addView(actionBtn,
            LinearLayout.LayoutParams(-1, dp(40)).also { it.topMargin = dp(8) })

        val detailBtn = Button(this).apply {
            text = "Details"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF333333.toInt())
            textSize = 12f
            setOnClickListener {
                startActivity(
                    Intent(this@SteamGamesActivity, SteamGameDetailActivity::class.java)
                        .putExtra(SteamGameDetailActivity.EXTRA_APP_ID, game.appId))
            }
        }
        expandSection.addView(detailBtn,
            LinearLayout.LayoutParams(-1, dp(36)).also { it.topMargin = dp(4) })

        card.addView(expandSection, LinearLayout.LayoutParams(-1, -2))

        // ── Progress callbacks ────────────────────────────────────────────────
        progressUpdaters[game.appId] = { done, total ->
            if (done < 0L) {
                // error
                progressBar.visibility = View.GONE
                pctTV.visibility = View.GONE
                statusTV.text = "Download failed"
                actionBtn.text = "Install"
                actionBtn.setBackgroundColor(COLOR_INSTALL)
                actionBtn.isEnabled = true
            } else {
                val pct = if (total > 0) ((done * 100) / total).toInt().coerceIn(0, 99) else 0
                progressBar.progress = pct
                pctTV.text = "$pct%"
                statusTV.text = "${fmtSize(done)} / ${fmtSize(total)}"
            }
        }
        completeUpdaters[game.appId] = {
            progressBar.progress = 100
            pctTV.visibility = View.GONE
            statusTV.visibility = View.GONE
            checkmark.visibility = View.VISIBLE
            collapsedCheckTV.visibility = View.VISIBLE
            actionBtn.text = "Add to Launcher"
            actionBtn.setBackgroundColor(COLOR_LAUNCH)
            actionBtn.isEnabled = true
        }

        // ── Button logic ──────────────────────────────────────────────────────
        actionBtn.setOnClickListener {
            when (actionBtn.text.toString()) {
                "Cancel" -> {
                    cancelRefs[game.appId]?.run()
                    cancelRefs.remove(game.appId)
                }
                "Add to Launcher", "Add Game" -> launchGame(game)
                else -> startInstall(game, actionBtn, progressBar, pctTV, statusTV)
            }
        }

        // ── Tap to expand / collapse ──────────────────────────────────────────
        card.setOnClickListener {
            if (expandSection.visibility == View.VISIBLE) {
                expandSection.visibility = View.GONE
                arrowTV.text = "▼"
                expandedCard  = null
                expandedArrow = null
            } else {
                // Collapse previous card
                expandedCard?.let { prev ->
                    (prev.getChildAt(1) as? LinearLayout)?.visibility = View.GONE
                }
                expandedArrow?.text = "▼"
                expandSection.visibility = View.VISIBLE
                arrowTV.text = "▲"
                expandedCard  = card
                expandedArrow = arrowTV
            }
        }

        gameListLayout.addView(card, cardLp)
    }

    private fun startInstall(
        game: SteamGame,
        actionBtn: Button,
        progressBar: ProgressBar,
        pctTV: TextView,
        statusTV: TextView,
    ) {
        actionBtn.text = "Cancel"
        actionBtn.setBackgroundColor(COLOR_CANCEL)
        progressBar.visibility = View.VISIBLE
        pctTV.text = "0%"; pctTV.visibility = View.VISIBLE
        statusTV.text = "Starting…"; statusTV.visibility = View.VISIBLE
        val cancel = SteamDepotDownloader.installApp(game.appId, this)
        cancelRefs[game.appId] = cancel
    }

    private fun launchGame(game: SteamGame) {
        val installDir = game.installDir.ifEmpty {
            SteamRepository.getInstance().database.getGame(game.appId)?.installDir ?: ""
        }
        if (installDir.isEmpty()) {
            Toast.makeText(this, "Install directory not found", Toast.LENGTH_SHORT).show()
            return
        }
        val exes = AmazonLaunchHelper.collectExe(java.io.File(installDir))
        if (exes.isEmpty()) {
            Toast.makeText(this, "No executable found in install directory", Toast.LENGTH_SHORT).show()
            return
        }
        val scored = AmazonLaunchHelper.scoreExe(exes, java.io.File(installDir))
        if (scored.size == 1) {
            LudashiLaunchBridge.addToLauncher(this, game.name, scored[0].absolutePath)
        } else {
            val names = scored.map { it.name }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Choose executable")
                .setItems(names) { _, i ->
                    LudashiLaunchBridge.addToLauncher(this, game.name, scored[i].absolutePath)
                }.show()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GRID / POSTER view
    // ─────────────────────────────────────────────────────────────────────────

    private fun addGamesAsGrid(games: List<SteamGame>, itemWidthDp: Int, hGap: Int, vGap: Int) {
        val screenW = resources.displayMetrics.widthPixels
        val itemW   = dp(itemWidthDp)
        val cols    = ((screenW + hGap) / (itemW + hGap)).coerceAtLeast(2)
        val artH    = (itemW * 3) / 2   // 2:3 portrait ratio

        var row: LinearLayout? = null
        games.forEachIndexed { i, game ->
            if (i % cols == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                gameListLayout.addView(row,
                    LinearLayout.LayoutParams(-1, -2).also { it.bottomMargin = vGap })
            }
            val tileBg = GradientDrawable().apply {
                setColor(COLOR_CARD_BG); cornerRadius = dp(4).toFloat()
            }
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = tileBg
                setOnClickListener {
                    startActivity(
                        Intent(this@SteamGamesActivity, SteamGameDetailActivity::class.java)
                            .putExtra(SteamGameDetailActivity.EXTRA_APP_ID, game.appId))
                }
            }
            val artIV = ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(0xFF2A2A2A.toInt())
            }
            tile.addView(artIV, LinearLayout.LayoutParams(itemW, artH))
            loadCoverArt(artIV, game.appId)

            val nameTV = TextView(this).apply {
                text = game.name.ifEmpty { "App ${game.appId}" }
                setTextColor(0xFFFFFFFF.toInt()); textSize = 11f
                maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(dp(4), dp(4), dp(4), dp(4))
            }
            tile.addView(nameTV, LinearLayout.LayoutParams(itemW, -2))

            row!!.addView(tile,
                LinearLayout.LayoutParams(itemW, -2).also {
                    it.rightMargin = if ((i + 1) % cols != 0) hGap else 0
                })
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cover art
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadCoverArt(view: ImageView, appId: Int) {
        imageCache.get(appId)?.let { view.setImageBitmap(it); return }
        imageExecutor.submit {
            val bmp =
                tryBitmap("https://shared.steamstatic.com/store_item_assets/steam/apps/$appId/library_600x900.jpg")
                ?: tryBitmap("https://shared.steamstatic.com/store_item_assets/steam/apps/$appId/header.jpg")
            if (bmp != null) {
                imageCache.put(appId, bmp)
                ui.post { view.setImageBitmap(bmp) }
            }
        }
    }

    private fun tryBitmap(url: String): Bitmap? = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 6_000; conn.readTimeout = 10_000
        conn.connect()
        if (conn.responseCode == 200) BitmapFactory.decodeStream(conn.inputStream) else null
    } catch (_: Exception) { null }

    // ─────────────────────────────────────────────────────────────────────────
    // UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D0D0D.toInt())
        }

        // ── Header ────────────────────────────────────────────────────────────
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF0D0D0D.toInt())
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        fun headerBtnBg() = GradientDrawable().apply {
            setColor(0xFF333333.toInt()); cornerRadius = dp(4).toFloat()
        }
        fun focusHighlight(bg: GradientDrawable): (View, Boolean) -> Unit = { _, f ->
            bg.setColor(if (f) 0xFF555555.toInt() else 0xFF333333.toInt())
            bg.setStroke(if (f) dp(2) else 0, if (f) 0xFFFFD700.toInt() else 0)
        }

        val backBg = headerBtnBg()
        val backBtn = Button(this).apply {
            text = "←"; setTextColor(0xFFFFFFFF.toInt()); background = backBg
            textSize = 16f; setPadding(dp(12), 0, dp(12), 0)
            setOnFocusChangeListener(focusHighlight(backBg))
            setOnClickListener { finish() }
        }
        header.addView(backBtn, LinearLayout.LayoutParams(-2, dp(40)))

        val titleTV = TextView(this).apply {
            text = "Steam Library"; setTextColor(COLOR_STEAM)
            textSize = 18f; setTypeface(null, Typeface.BOLD)
            setPadding(dp(12), 0, 0, 0)
        }
        header.addView(titleTV, LinearLayout.LayoutParams(0, -2, 1f))

        val toggleBg = headerBtnBg()
        viewToggleBtn = Button(this).apply {
            text = viewModeIcon(viewMode); setTextColor(0xFFFFFFFF.toInt()); background = toggleBg
            textSize = 16f; setPadding(dp(12), 0, dp(12), 0)
            setOnFocusChangeListener(focusHighlight(toggleBg))
            setOnClickListener {
                viewMode = when (viewMode) { "list" -> "grid"; "grid" -> "poster"; else -> "list" }
                getSharedPreferences(PREFS_NAME, 0).edit().putString(VIEW_MODE_KEY, viewMode).apply()
                text = viewModeIcon(viewMode)
                expandedCard = null; expandedArrow = null
                applyFilter(searchBar.text?.toString() ?: "")
            }
        }
        header.addView(viewToggleBtn, LinearLayout.LayoutParams(-2, dp(40)))

        val refreshBg = headerBtnBg()
        refreshBtn = Button(this).apply {
            text = "↺"; setTextColor(0xFFFFFFFF.toInt()); background = refreshBg
            textSize = 16f; setPadding(dp(12), 0, dp(12), 0)
            setOnFocusChangeListener(focusHighlight(refreshBg))
            setOnClickListener {
                isEnabled = false; syncText.text = "Syncing…"
                SteamRepository.getInstance().also {
                    it.invalidateGameCache()
                    it.syncLibrary()
                }
            }
        }
        header.addView(refreshBtn, LinearLayout.LayoutParams(-2, dp(40)))
        root.addView(header, LinearLayout.LayoutParams(-1, -2))

        // ── Search bar ────────────────────────────────────────────────────────
        searchBar = EditText(this).apply {
            hint = "Search games…"
            setHintTextColor(0xFF666666.toInt()); setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f; setBackgroundColor(0xFF141820.toInt())
            setPadding(dp(12), dp(8), dp(12), dp(8)); isSingleLine = true
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                    applyFilter(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        root.addView(searchBar, LinearLayout.LayoutParams(-1, -2))

        // ── Sync status ───────────────────────────────────────────────────────
        syncText = TextView(this).apply {
            text = "Loading Steam library…"
            setTextColor(0xFFCCCCCC.toInt()); textSize = 13f
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setBackgroundColor(0xFF111111.toInt())
        }
        root.addView(syncText, LinearLayout.LayoutParams(-1, -2))

        // ── Scrollable game list ──────────────────────────────────────────────
        scrollView = ScrollView(this).apply {
            setBackgroundColor(0xFF0D0D0D.toInt())
            visibility = View.GONE
        }
        gameListLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        scrollView.addView(gameListLayout, FrameLayout.LayoutParams(-1, -2))
        root.addView(scrollView, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun viewModeIcon(mode: String) = when (mode) {
        "grid"   -> "⊞"
        "poster" -> "☰"
        else     -> "▦"
    }

    private fun fmtSize(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
        else                    -> "%.0f KB".format(bytes / 1024.0)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private const val PREFS_NAME    = "steam_prefs"
        private const val VIEW_MODE_KEY = "steam_view_mode"

        private val COLOR_STEAM   = 0xFF4FC3F7.toInt()  // Steam light blue
        private val COLOR_CARD_BG = 0xFF0F1117.toInt()
        private val COLOR_INSTALL = 0xFF1565C0.toInt()  // blue
        private val COLOR_CANCEL  = 0xFFCC3333.toInt()  // red
        private val COLOR_LAUNCH  = 0xFF2E7D32.toInt()  // green

        private val imageCache    = LruCache<Int, Bitmap>(4 * 1024 * 1024)
        private val imageExecutor = Executors.newFixedThreadPool(4)
    }
}
