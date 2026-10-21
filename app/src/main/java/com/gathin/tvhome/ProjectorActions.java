package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        // 1. Direct hardware apps found on Bettdow AC1066 / Amlogic
        PackageManager pm = context.getPackageManager();
        String[] directApps = {
            "com.droidlogic.keystone",
            "com.tpjz.vafocus",
            "com.zeasn.whaleos.settings",
            "com.whaletv.settings"
        };
        for (String pkg : directApps) {
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null && tryStart(context, launch)) {
                return;
            }
        }

        // 2. Candidate components
        String[][] candidates = new String[][]{
            {"com.droidlogic.keystone", "com.droidlogic.keystone.MainActivity"},
            {"com.tpjz.vafocus", "com.tpjz.vafocus.MainActivity"},
            {"com.zeasn.whaleos.settings", "com.zeasn.whaleos.settings.MainActivity"},
            {"com.android.tv.settings", "com.android.tv.settings.display.DisplayActivity"},
            {"com.whaletv.settings", "com.whaletv.settings.MainActivity"},
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

    public static void openFocusControl(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage("com.tpjz.vafocus");
        if (launch != null && tryStart(context, launch)) {
            return;
        }
        openProjectorSettings(context);
    }

    public static void openKeystoneControl(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage("com.droidlogic.keystone");
        if (launch != null && tryStart(context, launch)) {
            return;
        }
        openProjectorSettings(context);
    }

    public static void openBluetoothSpeakerMode(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage("com.stellamore.bluetoothspeaker");
        if (launch != null && tryStart(context, launch)) {
            return;
        }
        openBluetoothSettings(context);
    }

    public static void openTvInputs(Context context) {
        PackageManager pm = context.getPackageManager();
        String[] candidates = {"com.droidlogic.tvinput", "com.mstar.tv.tvapp", "com.google.android.tv", "com.android.tv", "com.whaletv.tv"};
        for (String pkg : candidates) {
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null && tryStart(context, launch)) {
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
