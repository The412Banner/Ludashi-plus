package com.winlator.cmod.store

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.*

/**
 * Steam library screen — lists owned games fetched via PICS sync.
 *
 * Phase 4: shows games from SteamDatabase, refreshes on LibrarySynced event.
 * Pull-to-refresh triggers a manual re-sync.
 */
class SteamGamesActivity : Activity(), SteamRepository.SteamEventListener {

    private val ui = Handler(Looper.getMainLooper())
    private lateinit var statusText: TextView
    private lateinit var listView: ListView
    private lateinit var emptyText: TextView
    private var games: List<SteamGame> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = buildUI()
        setContentView(root)

        SteamRepository.getInstance().addListener(this)
        loadGames()
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
                val count = event.substringAfter("LibrarySynced:").toIntOrNull() ?: 0
                ui.post {
                    statusText.text = "$count games in library"
                    loadGames()
                }
            }
            event == "LoggedOut" || event == "Disconnected" -> {
                ui.post { finish() }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private fun loadGames() {
        val repo = SteamRepository.getInstance()
        val rows = repo.database.allGames
        games = rows.map { SteamGame.fromGameRow(it) }
        refreshList()
    }

    private fun refreshList() {
        val adapter = object : ArrayAdapter<SteamGame>(this, 0, games) {
            override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
                val game = getItem(pos)!!
                val row = convertView as? LinearLayout ?: buildRow()
                val nameView  = row.getChildAt(0) as TextView
                val typeView  = row.getChildAt(1) as TextView
                nameView.text = game.name.ifEmpty { "App ${game.appId}" }
                typeView.text = game.type.uppercase()
                typeView.setTextColor(if (game.type == "game") Color.parseColor("#4CAF50") else Color.parseColor("#FF9800"))
                return row
            }
        }
        listView.adapter = adapter
        emptyText.visibility = if (games.isEmpty()) View.VISIBLE else View.GONE
        listView.visibility  = if (games.isEmpty()) View.GONE   else View.VISIBLE
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private fun buildUI(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1B1B1B"))
        }

        // Header bar
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.parseColor("#212121"))
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
            text = "↻"
            setTextColor(Color.parseColor("#4FC3F7"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { SteamRepository.getInstance().syncLibrary() }
        }
        header.addView(backBtn)
        header.addView(title)
        header.addView(refreshBtn)
        root.addView(header)

        // Status bar
        statusText = TextView(this).apply {
            text = "Loading library…"
            textSize = 12f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setBackgroundColor(Color.parseColor("#1A1A2E"))
        }
        root.addView(statusText)

        // Empty state
        emptyText = TextView(this).apply {
            text = "No games found.\nIf sync just finished, tap ↻ to refresh.\nMake sure you are logged in to Steam."
            textSize = 14f
            setTextColor(Color.parseColor("#888888"))
            gravity = android.view.Gravity.CENTER
            setPadding(dp(24), dp(48), dp(24), dp(24))
            visibility = View.GONE
        }
        root.addView(emptyText, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // Game list
        listView = ListView(this).apply {
            setBackgroundColor(Color.parseColor("#1B1B1B"))
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

    private fun buildRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(dp(16), dp(12), dp(16), dp(12))
        val nameView = TextView(this@SteamGamesActivity).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val typeView = TextView(this@SteamGamesActivity).apply {
            textSize = 10f
            setPadding(dp(4), dp(2), dp(4), dp(2))
        }
        addView(nameView)
        addView(typeView)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
