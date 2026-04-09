package com.winlator.cmod.store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

/**
 * Entry point for the Steam store tab.
 *
 * Phase 1 stub — shows a placeholder toast and starts the foreground service.
 * Full login/library UI is implemented in Phase 7.
 */
class SteamMainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise prefs (idempotent)
        SteamPrefs.init(this)

        // Start foreground service (keeps CM connection alive)
        SteamForegroundService.start(this)

        if (SteamPrefs.isLoggedIn) {
            // Already have a session — go straight to library
            startActivity(Intent(this, SteamGamesActivity::class.java))
        } else {
            // Need to log in first
            startActivity(Intent(this, SteamLoginActivity::class.java))
        }
        finish()
    }
}
