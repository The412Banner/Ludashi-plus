package com.winlator.cmod.store

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * QR code login screen — displays a QR code that the user scans with the Steam mobile app.
 *
 * Phase 1 stub. Full QR UI implemented in Phase 7.
 */
class QrLoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "QR login coming soon (Phase 7)", Toast.LENGTH_LONG).show()
        finish()
    }
}
