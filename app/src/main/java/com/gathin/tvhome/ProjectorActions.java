package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

public final class ProjectorActions {
    private ProjectorActions() {}

    public static boolean tryStart(Context context, Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void openProjectorSettings(Context context) {
        String[][] candidates = new String[][]{
            {"com.android.tv.settings", "com.android.tv.settings.display.DisplayActivity"},
            {"com.android.tv.settings", "com.android.tv.settings.device.display.DisplayActivity"},
            {"com.whaletv.settings", "com.whaletv.settings.MainActivity"},
            {"com.zeasn.whale.settings", "com.zeasn.whale.settings.MainActivity"},
            {"com.softwinner.tv.settings", "com.softwinner.tv.settings.DisplaySettingsActivity"},
            {"com.mstar.tv.setting", "com.mstar.tv.setting.MainActivity"},
            {"com.android.settings", "com.android.settings.Settings$DisplaySettingsActivity"}
        };
        for (String[] candidate : candidates) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(candidate[0], candidate[1]));
            if (tryStart(context, intent)) {
                return;
            }
        }
        if (!tryStart(context, new Intent(Settings.ACTION_DISPLAY_SETTINGS))) {
            tryStart(context, new Intent(Settings.ACTION_SETTINGS));
        }
    }

    public static void openTvInputs(Context context) {
        String[][] candidates = new String[][]{
            {"com.mstar.tv.tvapp", "com.mstar.tv.tvapp.ui.MainActivity"},
            {"com.google.android.tv", "com.google.android.tv.MainActivity"},
            {"com.android.tv", "com.android.tv.MainActivity"},
            {"com.whaletv.tv", "com.whaletv.tv.MainActivity"}
        };
        for (String[] candidate : candidates) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(candidate[0], candidate[1]));
            if (tryStart(context, intent)) {
                return;
            }
        }
        try {
            Intent inputIntent = new Intent("android.intent.action.VIEW");
            inputIntent.setData(Uri.parse("content://android.media.tv/passthrough"));
            if (tryStart(context, inputIntent)) {
                return;
            }
        } catch (Exception ignored) {}
        Toast.makeText(context, "Abrindo Ajustes de Entrada...", Toast.LENGTH_SHORT).show();
        tryStart(context, new Intent(Settings.ACTION_SETTINGS));
    }

    public static void openBluetoothSettings(Context context) {
        if (!tryStart(context, new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))) {
            tryStart(context, new Intent(Settings.ACTION_SETTINGS));
        }
    }

    public static void openAccessibilitySettings(Context context) {
        if (!tryStart(context, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) {
            tryStart(context, new Intent(Settings.ACTION_SETTINGS));
        }
    }
}
