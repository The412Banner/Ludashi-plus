package com.winlator.cmod.store;

import android.app.Activity;

/**
 * Launch bridge for GOG games in Ludashi-plus.
 * Delegates to LudashiLaunchBridge.
 */
public final class GogLaunchHelper {

    private GogLaunchHelper() {}

    public static void triggerLaunch(Activity activity, String exePath) {
        LudashiLaunchBridge.triggerLaunch(activity, exePath);
    }
}
