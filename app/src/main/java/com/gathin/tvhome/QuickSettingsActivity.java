package com.gathin.tvhome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The panel the remote's Settings button opens when {@code settings_key_panel} is on (HomeKeyService):
 * a few glass rows under the Settings pill, over Home or whatever app is playing. Translucent and in a
 * task of its own, so the app behind keeps running and Back returns to it. Pressing Settings again
 * closes the panel.
 */
public final class QuickSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_quick_settings);
        findViewById(R.id.scrim).setOnClickListener(v -> finish());
        LinearLayout rows = findViewById(R.id.rows);

        // Projector dedicated features
        addRowAction(rows, R.drawable.ic_picture, "🎯 Calibrar Foco & Keystone (Padrão)", 0, () -> {
            startActivity(new Intent(this, CalibrationActivity.class));
        });

        String sleepStatus = SleepTimer.getStatusText(this);
        addRowAction(rows, R.drawable.ic_home_settings, "⏱️ Sleep Timer: " + sleepStatus, 0, () -> {
            SleepTimer.showDialog(this, () -> finish());
        });

        addRowAction(rows, R.drawable.ic_home_settings, "📻 Apagar Tela (Apenas Áudio)", 0, () -> {
            startActivity(new Intent(this, ScreenOffActivity.class));
        });

        addRowAction(rows, R.drawable.ic_picture, "🌙 Descanso de Tela (Cinema Screensaver)", 0, () -> {
            startActivity(new Intent(this, ScreensaverActivity.class));
        });

        addRowAction(rows, R.drawable.ic_picture, "📽️ Ajustes do Projetor (Foco & Keystone)", 0, () -> ProjectorActions.openProjectorSettings(this));
        addRowAction(rows, R.drawable.ic_input, "Entradas de Vídeo (HDMI)", 0, () -> ProjectorActions.openTvInputs(this));
        addRowAction(rows, R.drawable.ic_settings, "Bluetooth", 0, () -> ProjectorActions.openBluetoothSettings(this));
        addRow(rows, R.drawable.ic_wifi, R.string.quick_network, 0, new Intent(Settings.ACTION_WIFI_SETTINGS));
        addRow(rows, R.drawable.ic_apps, R.string.quick_apps, R.string.quick_apps_hint,
                new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        addRowAction(rows, R.drawable.ic_picture, "🎬 Papéis de Parede TMDB", 0, () -> CinemaWallpapers.showCinemaPicker(this, null));
        
        // Through Home, so it rebuilds with the new choices when Launcher settings closes.
        addRow(rows, R.drawable.ic_home_settings, R.string.launcher_settings, 0,
                new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_LAUNCHER_SETTINGS));

        addRow(rows, R.drawable.ic_settings, R.string.tv_settings, 0, new Intent(Settings.ACTION_SETTINGS));

        rows.getChildAt(0).requestFocus();
    }

    /** Settings pressed again while the panel is up. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }

    /** Anything else taking the screen (Home, an app, a screensaver) closes the panel. */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
        overridePendingTransition(0, 0);
    }

    private void addRow(LinearLayout rows, int icon, int title, int status, Intent target) {
        View row = getLayoutInflater().inflate(R.layout.item_input, rows, false);
        ((ImageView) row.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.title)).setText(title);
        row.findViewById(R.id.dot).setVisibility(View.GONE);
        TextView hint = row.findViewById(R.id.status);
        if (status != 0) hint.setText(status);
        else hint.setVisibility(View.GONE);
        row.setOnClickListener(v -> {
            try {
                startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
        rows.addView(row);
    }

    private void addRowAction(LinearLayout rows, int icon, String title, int status, Runnable action) {
        View row = getLayoutInflater().inflate(R.layout.item_input, rows, false);
        ((ImageView) row.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.title)).setText(title);
        row.findViewById(R.id.dot).setVisibility(View.GONE);
        TextView hint = row.findViewById(R.id.status);
        if (status != 0) hint.setText(status);
        else hint.setVisibility(View.GONE);
        row.setOnClickListener(v -> {
            try {
                action.run();
            } catch (Exception e) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
        rows.addView(row);
    }
}
