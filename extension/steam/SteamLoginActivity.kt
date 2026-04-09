package com.winlator.cmod.store

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Steam login screen — username/password + Steam Guard entry.
 *
 * Phase 1 stub. Full login UI (EditTexts, buttons, QR link) implemented in Phase 7.
 */
class SteamLoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Steam login coming soon (Phase 7)", Toast.LENGTH_LONG).show()
        finish()
    }
}
