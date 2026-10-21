package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

public final class SleepTimer {
    private static final String PREF_NAME = "sleep_timer_prefs";
    private static final String KEY_TARGET_MILLIS = "target_millis";
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable pendingCheck;

    private SleepTimer() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static long getTargetMillis(Context context) {
        return prefs(context).getLong(KEY_TARGET_MILLIS, 0L);
    }

    public static boolean isRunning(Context context) {
        long target = getTargetMillis(context);
        return target > System.currentTimeMillis();
    }

    public static int getRemainingMinutes(Context context) {
        long target = getTargetMillis(context);
        long diff = target - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) Math.ceil(diff / 60000.0);
    }

    public static String getStatusText(Context context) {
        if (!isRunning(context)) {
            return "Desativado";
        }
        int min = getRemainingMinutes(context);
        return min + " min restantes";
    }

    public static void setTimer(Context context, int minutes) {
        if (minutes <= 0) {
            cancelTimer(context);
            Toast.makeText(context, "⏱️ Temporizador cancelado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if admin is active; if not, suggest activating it for real hardware shutdown
        if (!TvAdminReceiver.isAdminActive(context) && !HomeKeyService.isServiceRunning()) {
            Toast.makeText(context, "💡 Dica: Ative o 'Administrador de Dispositivo' no Destravador para desligamento 100% de hardware.", Toast.LENGTH_LONG).show();
        }

        long target = System.currentTimeMillis() + (minutes * 60L * 1000L);
        prefs(context).edit().putLong(KEY_TARGET_MILLIS, target).apply();
        scheduleCheck(context.getApplicationContext());
        Toast.makeText(context, "⏱️ Desligamento programado para " + minutes + " min", Toast.LENGTH_SHORT).show();
    }

    public static void cancelTimer(Context context) {
        prefs(context).edit().remove(KEY_TARGET_MILLIS).apply();
        if (pendingCheck != null) {
            handler.removeCallbacks(pendingCheck);
            pendingCheck = null;
        }
    }

    public static void scheduleCheck(Context context) {
        if (pendingCheck != null) {
            handler.removeCallbacks(pendingCheck);
        }
        if (!isRunning(context)) return;

        long diff = getTargetMillis(context) - System.currentTimeMillis();
        if (diff <= 0) {
            executeSleep(context);
            return;
        }

        pendingCheck = () -> {
            if (isRunning(context)) {
                if (System.currentTimeMillis() >= getTargetMillis(context)) {
                    executeSleep(context);
                } else {
                    scheduleCheck(context);
                }
            }
        };
        long delay = Math.min(diff, 15_000L);
        handler.postDelayed(pendingCheck, Math.max(1000L, delay));
    }

    public static void showDialog(Activity activity, Runnable onUpdated) {
        final int[] minutesOptions = {0, 15, 30, 45, 60, 90, 120};
        String[] labels = {
            "🚫 Desativar Temporizador",
            "⏱️ 15 Minutos",
            "⏱️ 30 Minutos",
            "⏱️ 45 Minutos",
            "⏱️ 60 Minutos (1 Hora)",
            "⏱️ 90 Minutos (1h30)",
            "⏱️ 120 Minutos (2 Horas)"
        };

        int remaining = getRemainingMinutes(activity);
        if (remaining > 0) {
            labels[0] = "🚫 Desativar (restam " + remaining + " min)";
        }

        new AlertDialog.Builder(activity)
                .setTitle("⏱️ Temporizador de Desligamento (Sleep)")
                .setItems(labels, (dialog, which) -> {
                    dialog.dismiss();
                    setTimer(activity, minutesOptions[which]);
                    if (onUpdated != null) onUpdated.run();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void executeSleep(Context context) {
        cancelTimer(context);

        // 1. Hardware Force-Lock via Device Administrator (Kernel level screen off and sleep)
        if (TvAdminReceiver.lockNow(context)) {
            return;
        }

        // 2. Hardware Force-Lock via Accessibility Service (Global Action Lock Screen)
        if (HomeKeyService.lockHardwareScreen()) {
            return;
        }

        // 3. Fallback: PowerManager goToSleep
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                try {
                    pm.getClass().getMethod("goToSleep", long.class).invoke(pm, SystemClock.uptimeMillis());
                    return;
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // 4. Fallback: Request Shutdown Intent
        try {
            Intent shutdown = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
            shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
            shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(shutdown);
            return;
        } catch (Exception ignored) {}

        // 5. Final Graceful Fallback: Deep Screen Off
        try {
            Intent screenOff = new Intent(context, ScreenOffActivity.class);
            screenOff.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(screenOff);
        } catch (Exception ignored) {}
    }
}
