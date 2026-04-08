package com.winlator.cmod.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Launch bridge for Ludashi-plus store integrations.
 *
 * Uses reflection to access ContainerManager and XServerDisplayActivity
 * so this class compiles against android.jar alone — no Ludashi stubs needed.
 *
 * After a game installs, call triggerLaunch(activity, exePath).
 * Picks the first available Wine container and starts XServerDisplayActivity.
 * The user then navigates to the exe inside the Wine session.
 */
public final class LudashiLaunchBridge {

    private LudashiLaunchBridge() {}

    public static void triggerLaunch(Activity activity, String exePath) {
        new Thread(() -> {
            Handler h = new Handler(Looper.getMainLooper());
            try {
                Class<?> cmClass = Class.forName("com.winlator.cmod.container.ContainerManager");
                Object manager = cmClass.getConstructor(Context.class).newInstance(activity);
                Method getContainers = cmClass.getMethod("getContainers");
                List<?> containers = (List<?>) getContainers.invoke(manager);

                if (containers == null || containers.isEmpty()) {
                    h.post(() -> Toast.makeText(activity,
                            "No Wine container found. Create one first.",
                            Toast.LENGTH_LONG).show());
                    return;
                }

                Object first = containers.get(0);
                Field idField = first.getClass().getField("id");
                int containerId = (int) idField.get(first);

                h.post(() -> {
                    Toast.makeText(activity,
                            "Launching Wine. Game at:\n" + exePath,
                            Toast.LENGTH_LONG).show();
                    try {
                        Class<?> xsClass = Class.forName(
                                "com.winlator.cmod.XServerDisplayActivity");
                        Intent intent = new Intent(activity, xsClass);
                        intent.putExtra("container_id", containerId);
                        activity.startActivity(intent);
                    } catch (Exception e2) {
                        Toast.makeText(activity,
                                "Failed to open container: " + e2.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                h.post(() -> Toast.makeText(activity,
                        "Launch error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
