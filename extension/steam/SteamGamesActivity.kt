package com.winlator.cmod.store

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Steam library/games screen — displays owned games, download progress, launch shortcuts.
 *
 * Phase 1 stub. Full library UI implemented in Phase 8.
 */
class SteamGamesActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this,
            "Steam library coming soon. Logged in as: ${SteamPrefs.displayName.ifEmpty { SteamPrefs.username }}",
            Toast.LENGTH_LONG).show()
        finish()
    }
}
