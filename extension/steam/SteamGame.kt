package com.winlator.cmod.store

/**
 * Lightweight in-memory model for a Steam app entry.
 *
 * Populated from PICS data during library sync (Phase 4).
 * Persisted to SQLite in Phase 3 for offline use.
 *
 * Install state is derived at runtime from the presence of
 * SteamGame.installMarkerFile(ctx) on disk.
 */
data class SteamGame(
    val appId: Int,
    val name: String,

    /** Folder name under steamapps/common/ — from PICS config.installdir. */
    val installDir: String,

    /** Icon hash hex string — used to build steamcdn URL. */
    val iconHash: String = "",

    /** Estimated total install size in bytes (sum of depot manifest sizes). */
    val sizeBytes: Long = 0L,

    /** Depots to download for this app (resolved during library sync). */
    val depotIds: List<Int> = emptyList(),

    /** App type string from PICS common.type — "game", "dlc", "tool", etc. */
    val type: String = "game",

    /** True if the game has been fully downloaded (marker file present). */
    val isInstalled: Boolean = false,
) {
    /** Store header image URL — available for all apps, no hash needed. */
    val headerUrl: String
        get() = "https://shared.steamstatic.com/store_item_assets/steam/apps/$appId/header.jpg"

    /** Old-style CDN icon URL — requires iconHash from PICS. */
    val iconUrl: String?
        get() = if (iconHash.isNotEmpty())
            "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$iconHash.jpg"
        else null
}
