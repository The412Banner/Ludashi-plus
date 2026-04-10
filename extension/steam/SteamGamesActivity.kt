package com.winlator.cmod.store

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Steam library screen — shows only type="game" entries.
 *
 * Each row shows the Steam library portrait art (600x900) loaded async,
 * falling back to the header image (header.jpg) if portrait isn't available.
 */
class SteamGamesActivity : Activity(), SteamRepository.SteamEventListener {

    private val ui = Handler(Looper.getMainLooper())
    private lateinit var statusText: TextView
    private lateinit var listView: ListView
    private lateinit var emptyText: TextView
    private var games: List<SteamGame> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUI())
        SteamRepository.getInstance().addListener(this)
        loadGames()

        // If already logged in but DB is empty (sync fired before Activity opened),
        // kick off a re-sync immediately.
        val repo = SteamRepository.getInstance()
        if (games.isEmpty() && repo.isLoggedIn) {
            statusText.text = "Syncing library…"
            repo.syncLibrary()
        }
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
            event.startsWith("LibraryProgress:") -> {
                val parts = event.split(":")
                val phase = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val count = parts.getOrNull(2)?.toIntOrNull() ?: 0
                ui.post {
                    statusText.text = if (phase == 0)
                        "Syncing packages ($count)…"
                    else
                        "Fetching $count app records…"
                }
            }
            event.startsWith("LibrarySynced:") -> {
                // Reload from DB and derive count from what's actually showing —
                // the event count can be 0 if Steam returned empty "no change" buffers
                // for apps that haven't changed since last request.
                ui.post {
                    loadGames()
                    statusText.text = "${games.size} games in library"
                }
            }
            event == "LoggedOut" -> {
                ui.post { finish() }
            }
            event == "Disconnected" -> {
                // Transient disconnect — auto-reconnect is in progress.
                // Don't close the activity; just show status.
                ui.post { statusText.text = "Disconnected — reconnecting…" }
            }
            event == "Connected" -> {
                // After reconnect, retry sync if still empty.
                val repo = SteamRepository.getInstance()
                if (games.isEmpty() && repo.isLoggedIn) {
                    ui.post { statusText.text = "Reconnected — syncing library…" }
                    repo.syncLibrary()
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Data — only show type="game" entries
    // -------------------------------------------------------------------------

    private fun loadGames() {
        val db = try {
            SteamRepository.getInstance().database
        } catch (e: IllegalStateException) {
            // Process was restarted (e.g. after a crash) without going through
            // SteamMainActivity — SteamRepository/SteamDatabase not initialised.
            // Redirect to the entry point so the user can log back in.
            startActivity(android.content.Intent(this, SteamMainActivity::class.java))
            finish()
            return
        }
        val rows = db.allGames
        games = rows
            .filter { it.type == "game" }
            .map { SteamGame.fromGameRow(it) }
            .sortedBy { it.name.lowercase() }
        refreshList()
    }

    private fun refreshList() {
        val adapter = object : ArrayAdapter<SteamGame>(this, 0, games) {
            override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
                val game = getItem(pos)!!
                val row = (convertView as? LinearLayout) ?: buildRow()
                // Tag the row with appId so the async image loader can detect recycling
                row.tag = game.appId

                val artView      = row.getChildAt(0) as ImageView
                val infoView     = row.getChildAt(1) as LinearLayout
                val nameView     = infoView.getChildAt(0) as TextView
                val developerView = infoView.getChildAt(1) as TextView
                val genresView   = infoView.getChildAt(2) as TextView
                val sizeView     = infoView.getChildAt(3) as TextView
                val metaView     = infoView.getChildAt(4) as TextView

                nameView.text = game.name.ifEmpty { "App ${game.appId}" }

                developerView.text = game.developer
                developerView.visibility = if (game.developer.isNotEmpty()) View.VISIBLE else View.GONE

                genresView.text = game.genres
                genresView.visibility = if (game.genres.isNotEmpty()) View.VISIBLE else View.GONE

                val sizeLabel = fmtSize(game.sizeBytes)
                sizeView.text = sizeLabel
                sizeView.visibility = if (game.sizeBytes > 0) View.VISIBLE else View.GONE

                if (game.metacriticScore > 0) {
                    metaView.text = "Metacritic: ${game.metacriticScore}"
                    metaView.setTextColor(when {
                        game.metacriticScore >= 75 -> 0xFF4CAF50.toInt()  // green
                        game.metacriticScore >= 50 -> 0xFFFFC107.toInt()  // amber
                        else                       -> 0xFFF44336.toInt()  // red
                    })
                    metaView.visibility = View.VISIBLE
                } else {
                    metaView.visibility = View.GONE
                }

                // Reset art to placeholder then kick off async load
                artView.setImageResource(android.R.color.darker_gray)
                loadCoverArt(artView, game.appId)
                return row
            }
        }
        listView.adapter = adapter
        emptyText.visibility = if (games.isEmpty()) View.VISIBLE else View.GONE
        listView.visibility  = if (games.isEmpty()) View.GONE   else View.VISIBLE
    }

    // -------------------------------------------------------------------------
    // Cover art loading
    // -------------------------------------------------------------------------

    private fun loadCoverArt(view: ImageView, appId: Int) {
        imageCache.get(appId)?.let { cached ->
            view.setImageBitmap(cached)
            return
        }
        imageExecutor.submit {
            // Try portrait art first (600x900), fall back to wide header
            val bmp = tryBitmap("https://shared.steamstatic.com/store_item_assets/steam/apps/$appId/library_600x900.jpg")
                   ?: tryBitmap("https://shared.steamstatic.com/store_item_assets/steam/apps/$appId/header.jpg")
            if (bmp != null) {
                imageCache.put(appId, bmp)
                ui.post {
                    // Only set if this view still shows the same appId (not recycled)
                    val parent = view.parent as? LinearLayout
                    if (parent?.tag == appId) view.setImageBitmap(bmp)
                }
            }
        }
    }

    private fun tryBitmap(url: String): Bitmap? = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 6_000
        conn.readTimeout    = 10_000
        conn.connect()
        if (conn.responseCode == 200)
            BitmapFactory.decodeStream(conn.inputStream)
        else null
    } catch (_: Exception) { null }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private fun buildUI(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        // Header bar
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.parseColor("#212121"))
            gravity = Gravity.CENTER_VERTICAL
        }
        val backBtn = Button(this).apply {
            text = "←"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }
        val title = TextView(this).apply {
            text = "Steam Library"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(dp(8), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val refreshBtn = Button(this).apply {
            text = "Refresh"
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(BLUE)
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener { SteamRepository.getInstance().syncLibrary() }
        }
        header.addView(backBtn)
        header.addView(title)
        header.addView(refreshBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)))
        root.addView(header)

        // Status bar
        statusText = TextView(this).apply {
            text = "Loading library…"
            textSize = 12f
            setTextColor(GRAY)
            setPadding(dp(12), dp(5), dp(12), dp(5))
            setBackgroundColor(Color.parseColor("#1A1A2E"))
        }
        root.addView(statusText)

        // Empty state
        emptyText = TextView(this).apply {
            text = "No games found.\nIf sync just finished, tap Refresh."
            textSize = 14f
            setTextColor(GRAY)
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(48), dp(24), dp(24))
            visibility = View.GONE
        }
        root.addView(emptyText, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // Game list
        listView = ListView(this).apply {
            setBackgroundColor(BG)
            divider = null
            dividerHeight = dp(1)
            setOnItemClickListener { _, _, pos, _ ->
                val game = games.getOrNull(pos) ?: return@setOnItemClickListener
                startActivity(Intent(this@SteamGamesActivity, SteamGameDetailActivity::class.java)
                    .putExtra(SteamGameDetailActivity.EXTRA_APP_ID, game.appId))
            }
        }
        root.addView(listView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        return root
    }

    /** Build a card row: [portrait art | name / developer / genres / size / metacritic] */
    private fun buildRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setBackgroundColor(CARD_BG)
        setPadding(0, 0, 0, 0)

        // Portrait art thumbnail (approx 2:3 ratio)
        val artWidth  = dp(80)
        val artHeight = dp(140)
        val artView = ImageView(this@SteamGamesActivity).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#2A2A2A"))
        }
        addView(artView, LinearLayout.LayoutParams(artWidth, artHeight))

        // Right side: name + metadata stack
        val infoLayout = LinearLayout(this@SteamGamesActivity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(8), dp(8))
        }

        fun smallText() = TextView(this@SteamGamesActivity).apply {
            textSize = 11f
            setTextColor(GRAY)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(2), 0, 0)
        }

        // child 0: game name
        val nameView = TextView(this@SteamGamesActivity).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        // child 1: developer
        val developerView = smallText()
        // child 2: genres
        val genresView = smallText()
        // child 3: install size
        val sizeView = smallText()
        // child 4: metacritic score (color set dynamically)
        val metaView = smallText()

        val wrapLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        infoLayout.addView(nameView,      wrapLp)
        infoLayout.addView(developerView, wrapLp)
        infoLayout.addView(genresView,    wrapLp)
        infoLayout.addView(sizeView,      wrapLp)
        infoLayout.addView(metaView,      wrapLp)

        addView(infoLayout, LinearLayout.LayoutParams(0, artHeight, 1f))

        // Row bottom margin (acts as divider)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).also { it.bottomMargin = dp(2) }
    }

    private fun fmtSize(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
        else                    -> "%.0f KB".format(bytes / 1024.0)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private val BG      = Color.parseColor("#1B1B1B")
        private val CARD_BG = Color.parseColor("#252525")
        private val GRAY    = Color.parseColor("#AAAAAA")
        private val BLUE    = Color.parseColor("#4FC3F7")

        // Shared LRU image cache (4 MB cap) and fixed thread pool across instances
        private val imageCache = LruCache<Int, Bitmap>(4 * 1024 * 1024)
        private val imageExecutor = Executors.newFixedThreadPool(4)
    }
}
