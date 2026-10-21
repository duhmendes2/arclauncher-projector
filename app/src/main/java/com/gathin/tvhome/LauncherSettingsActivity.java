package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Remote-friendly settings owned by the launcher; system settings stay one row away. */
public final class LauncherSettingsActivity extends Activity {
    private static final int REQUEST_PICK_IMAGE = 1010;
    static final String KEY_SCREENSAVER_TIMEOUT = "screensaver_timeout_minutes";
    static final String KEY_NIGHT_DIMMER = "night_dimmer_percent";

    private SharedPreferences choices;
    private final TextView[] columnChoices = new TextView[4];
    private final TextView[] rowChoices = new TextView[4];
    private TextView wallpaperChoice;
    private TextView animationsChoice;
    private TextView advancedChoice;
    private TextView followThemeChoice;
    private TextView screensaverChoice;
    private TextView nightDimmerChoice;
    private TextView sleepTimerChoice;

    private int columns;
    private int rowsBelowDock;
    private boolean animations;
    private boolean advancedEffects;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_launcher_settings);
        choices = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        Config config = Config.load(this);
        columns = config.columns;
        rowsBelowDock = config.rowsBelowDock;
        animations = config.animations;
        advancedEffects = config.animations && config.advancedEffects;

        int[] ids = {R.id.columns4, R.id.columns5, R.id.columns6, R.id.columns7};
        for (int i = 0; i < ids.length; i++) {
            final int value = i + 4;
            columnChoices[i] = findViewById(ids[i]);
            columnChoices[i].setOnClickListener(v -> {
                if (columns == value) return;
                columns = value;
                choices.edit().putInt("columns", value).apply();
                setResult(RESULT_OK);
                refresh();
            });
        }
        int[] rowIds = {R.id.rows0, R.id.rows1, R.id.rows2, R.id.rows3};
        for (int i = 0; i < rowIds.length; i++) {
            final int value = i;
            rowChoices[i] = findViewById(rowIds[i]);
            rowChoices[i].setOnClickListener(v -> {
                if (rowsBelowDock == value) return;
                rowsBelowDock = value;
                choices.edit().putInt(Config.ROWS_BELOW_DOCK_KEY, value).apply();
                setResult(RESULT_OK);
                refresh();
            });
        }

        if (getResources().getBoolean(R.bool.rtl_app_grid)) {
            reverseRow(columnChoices);
            reverseRow(rowChoices);
        }

        // Recursos de Projetor & Cinema
        findViewById(R.id.calibrationChoice).setOnClickListener(v ->
                startActivity(new Intent(this, CalibrationActivity.class)));

        sleepTimerChoice = findViewById(R.id.sleepTimerChoice);
        sleepTimerChoice.setOnClickListener(v ->
                SleepTimer.showDialog(this, this::refresh));

        findViewById(R.id.screenOffChoice).setOnClickListener(v ->
                startActivity(new Intent(this, ScreenOffActivity.class)));

        screensaverChoice = findViewById(R.id.screensaverChoice);
        screensaverChoice.setOnClickListener(v -> showScreensaverTimeoutDialog());

        nightDimmerChoice = findViewById(R.id.nightDimmerChoice);
        nightDimmerChoice.setOnClickListener(v -> showNightDimmerDialog());

        findViewById(R.id.appHideChoice).setOnClickListener(v -> {
            startActivity(new Intent(this, AppHideActivity.class));
            setResult(RESULT_OK);
        });

        findViewById(R.id.projectorSettingsChoice).setOnClickListener(v ->
                ProjectorActions.openProjectorSettings(this));

        // Papéis de Parede
        wallpaperChoice = findViewById(R.id.wallpaperChoice);
        wallpaperChoice.setOnClickListener(v -> showWallpaperPicker());

        findViewById(R.id.pickLocalWallpaperChoice).setOnClickListener(v -> showLocalImagePicker());

        findViewById(R.id.cinemaWallpaperChoice).setOnClickListener(v ->
                CinemaWallpapers.showCinemaPicker(this, () -> {
                    setResult(RESULT_OK);
                    refresh();
                }));

        // Efeitos & Desempenho
        animationsChoice = findViewById(R.id.animationsChoice);
        animationsChoice.setOnClickListener(v -> {
            animations = !animations;
            if (!animations) advancedEffects = false;
            choices.edit().putBoolean("animations", animations)
                    .putBoolean("advanced_effects", advancedEffects).apply();
            setResult(RESULT_OK);
            refresh();
        });

        advancedChoice = findViewById(R.id.advancedChoice);
        advancedChoice.setOnClickListener(v -> {
            if (advancedEffects) {
                setAdvanced(false);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.advanced_effects)
                        .setMessage(R.string.advanced_effects_warning)
                        .setPositiveButton(R.string.enable, (dialog, which) -> setAdvanced(true))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        followThemeChoice = findViewById(R.id.followThemeChoice);
        followThemeChoice.setOnClickListener(v -> {
            if (!hasOnTvChoices()) return;
            choices.edit().remove("columns").remove(Config.ROWS_BELOW_DOCK_KEY).remove("animations")
                    .remove("advanced_effects").remove(Config.WALLPAPER_CHOICE_KEY).commit();
            Config current = Config.load(this);
            columns = current.columns;
            rowsBelowDock = current.rowsBelowDock;
            animations = current.animations;
            advancedEffects = current.animations && current.advancedEffects;
            setResult(RESULT_OK);
            refresh();
        });

        findViewById(R.id.defaultHomeChoice).setOnClickListener(v -> {
            Toast.makeText(this, R.string.home_settings_help, Toast.LENGTH_LONG).show();
            open(new Intent(Settings.ACTION_HOME_SETTINGS));
        });

        findViewById(R.id.tvSettingsChoice).setOnClickListener(v ->
                open(new Intent(Settings.ACTION_SETTINGS)));

        refresh();
        columnChoices[Math.max(0, Math.min(columns - 4, 3))].requestFocus();
    }

    private void reverseRow(TextView[] views) {
        LinearLayout row = (LinearLayout) views[0].getParent();
        row.removeAllViews();
        for (int i = views.length - 1; i >= 0; i--) row.addView(views[i]);
    }

    private void showScreensaverTimeoutDialog() {
        int[] timeouts = {0, 2, 5, 10, 15};
        String[] labels = {
            "Desativado",
            "Após 2 minutos",
            "Após 5 minutos",
            "Após 10 minutos",
            "Após 15 minutos"
        };
        int current = choices.getInt(KEY_SCREENSAVER_TIMEOUT, 5);
        int selected = 2;
        for (int i = 0; i < timeouts.length; i++) {
            if (timeouts[i] == current) {
                selected = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("🌙 Protetor de Tela Inativo")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putInt(KEY_SCREENSAVER_TIMEOUT, timeouts[which]).apply();
                    setResult(RESULT_OK);
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showNightDimmerDialog() {
        int[] levels = {0, 20, 40, 60};
        String[] labels = {
            "Desativado (Brilho Normal)",
            "Escurecimento 20% (Suave)",
            "Escurecimento 40% (Médio)",
            "Escurecimento 60% (Profundo - Noite Total)"
        };
        int current = choices.getInt(KEY_NIGHT_DIMMER, 0);
        int selected = 0;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] == current) {
                selected = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("🕶️ Dimmer de Cinema (Filtro Escurecedor)")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putInt(KEY_NIGHT_DIMMER, levels[which]).apply();
                    setResult(RESULT_OK);
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showWallpaperPicker() {
        File dir = Config.dir(this);
        List<File> images = new ArrayList<>(WallpaperRotation.images(
                dir == null ? null : new File(dir, "wallpapers")));
        File single = dir == null ? null : new File(dir, Config.WALLPAPER_NAME);
        if (single != null && single.isFile()) images.add(0, single);
        String[] labels = new String[images.size() + 2];
        labels[0] = "📂 Escolher da Memória / Pendrive...";
        for (int i = 0; i < images.size(); i++) labels[i + 1] = images.get(i).getName();
        labels[images.size() + 1] = getString(R.string.wallpaper_none);
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        int checked = "none".equals(current) ? images.size() + 1
                : (indexOf(images, current) >= 0 ? indexOf(images, current) + 1 : -1);
        new AlertDialog.Builder(this)
                .setTitle(R.string.wallpaper_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) {
                        showLocalImagePicker();
                    } else if (which == images.size() + 1) {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, "none").apply();
                        setResult(RESULT_OK);
                        Toast.makeText(this, R.string.wallpaper_applied, Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY,
                                images.get(which - 1).getName()).apply();
                        setResult(RESULT_OK);
                        Toast.makeText(this, R.string.wallpaper_applied, Toast.LENGTH_SHORT).show();
                        refresh();
                    }
                })
                .setNeutralButton(R.string.wallpaper_theme_default, (dialog, which) -> {
                    choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();
                    setResult(RESULT_OK);
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showLocalImagePicker() {
        List<File> localImages = scanLocalImages();
        String[] options = new String[localImages.size() + 1];
        options[0] = "📂 Abrir Gerenciador de Arquivos do Sistema...";
        for (int i = 0; i < localImages.size(); i++) {
            File f = localImages.get(i);
            String parent = f.getParentFile() != null ? f.getParentFile().getName() : "";
            options[i + 1] = "[" + parent + "] " + f.getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Imagens na Memória do Projetor")
                .setItems(options, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) {
                        launchSystemFilePicker();
                    } else {
                        File selected = localImages.get(which - 1);
                        applyLocalFileWallpaper(selected);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void launchSystemFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Selecione a Imagem"), REQUEST_PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(this, "Gerenciador de arquivos não encontrado no sistema.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            copyUriToWallpaper(imageUri);
        }
    }

    private void copyUriToWallpaper(Uri uri) {
        new Thread(() -> {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) return;
                File dir = Config.dir(this);
                if (dir == null) return;
                File target = new File(dir, Config.WALLPAPER_NAME);
                try (FileOutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    out.flush();
                }
                runOnUiThread(() -> {
                    choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, Config.WALLPAPER_NAME).apply();
                    setResult(RESULT_OK);
                    Toast.makeText(this, "🎬 Papel de parede aplicado!", Toast.LENGTH_SHORT).show();
                    refresh();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao carregar imagem selecionada.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private List<File> scanLocalImages() {
        List<File> list = new ArrayList<>();
        String[] paths = {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(),
            "/sdcard/Projetor",
            "/storage/emulated/0/Projetor",
            "/storage/emulated/0/Pictures",
            "/storage/emulated/0/Download",
            "/sdcard/Download"
        };
        for (String p : paths) {
            scanDir(new File(p), list);
        }

        File storage = new File("/storage");
        if (storage.exists() && storage.isDirectory()) {
            File[] roots = storage.listFiles();
            if (roots != null) {
                for (File root : roots) {
                    if (root.isDirectory() && !root.getName().equals("emulated") && !root.getName().equals("self")) {
                        scanDir(root, list);
                    }
                }
            }
        }
        return list;
    }

    private void scanDir(File dir, List<File> out) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile()) {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) {
                    out.add(f);
                }
            }
        }
    }

    private void applyLocalFileWallpaper(File source) {
        new Thread(() -> {
            try (InputStream in = new FileInputStream(source)) {
                File dir = Config.dir(this);
                if (dir == null) return;
                File target = new File(dir, Config.WALLPAPER_NAME);
                try (FileOutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    out.flush();
                }
                runOnUiThread(() -> {
                    choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, Config.WALLPAPER_NAME).apply();
                    setResult(RESULT_OK);
                    Toast.makeText(this, "🎬 Papel de parede aplicado!", Toast.LENGTH_SHORT).show();
                    refresh();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao copiar imagem.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private static int indexOf(List<File> images, String name) {
        if (name == null) return -1;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    private void setAdvanced(boolean enabled) {
        advancedEffects = enabled;
        choices.edit().putBoolean("advanced_effects", enabled).apply();
        setResult(RESULT_OK);
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < columnChoices.length; i++) {
            columnChoices[i].setSelected(columns == i + 4);
        }
        for (int i = 0; i < rowChoices.length; i++) {
            rowChoices[i].setSelected(rowsBelowDock == i);
        }

        // Sleep status
        if (sleepTimerChoice != null) {
            sleepTimerChoice.setText("⏱️ Temporizador de Desligamento: " + SleepTimer.getStatusText(this));
        }

        // Screensaver status
        if (screensaverChoice != null) {
            int to = choices.getInt(KEY_SCREENSAVER_TIMEOUT, 5);
            screensaverChoice.setText("🌙 Protetor de Tela Inativo: " + (to == 0 ? "Desativado" : to + " minutos"));
        }

        // Night dimmer status
        if (nightDimmerChoice != null) {
            int dim = choices.getInt(KEY_NIGHT_DIMMER, 0);
            nightDimmerChoice.setText("🕶️ Dimmer de Cinema: " + (dim == 0 ? "Desativado" : dim + "%"));
        }

        animationsChoice.setText(getString(R.string.animations_state,
                getString(animations ? R.string.on : R.string.off)));
        advancedChoice.setText(getString(R.string.advanced_effects_state,
                getString(advancedEffects ? R.string.on : R.string.off)));
        advancedChoice.setEnabled(animations);
        advancedChoice.setAlpha(animations ? 1f : 0.5f);
        String wallpaperCurrent = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        wallpaperChoice.setText(getString(R.string.wallpaper_state,
                "none".equals(wallpaperCurrent) ? getString(R.string.wallpaper_none)
                        : wallpaperCurrent != null ? wallpaperCurrent
                        : getString(R.string.wallpaper_theme_default)));
        followThemeChoice.setSelected(!hasOnTvChoices());
    }

    private boolean hasOnTvChoices() {
        return choices.contains("columns") || choices.contains(Config.ROWS_BELOW_DOCK_KEY)
                || choices.contains("animations") || choices.contains("advanced_effects")
                || choices.contains(Config.WALLPAPER_CHOICE_KEY)
                || choices.contains(KEY_SCREENSAVER_TIMEOUT)
                || choices.contains(KEY_NIGHT_DIMMER);
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        }
    }
}
