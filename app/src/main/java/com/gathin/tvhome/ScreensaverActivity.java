package com.gathin.tvhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Integrated cinema screensaver for projectors:
 * - Ambient background image slideshow (from downloaded TMDB wallpapers or custom wallpaper).
 * - Floating clock and localized date that gently moves every 45s to avoid burn-in.
 * - Dismisses instantly on any key press.
 */
public final class ScreensaverActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private ImageView bgA, bgB;
    private TextView timeView, dateView;
    private LinearLayout clockContainer;
    private List<File> wallpaperFiles = new ArrayList<>();
    private int currentWallpaperIdx = 0;
    private boolean showingA = true;

    private final Runnable driftRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            driftClock();
            rotateWallpaper();
            handler.postDelayed(this, 30_000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);

        bgA = new ImageView(this);
        bgA.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bgB = new ImageView(this);
        bgB.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bgB.setAlpha(0f);

        root.addView(bgA, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(bgB, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Dark dim overlay so clock is always readable
        View dimOverlay = new View(this);
        dimOverlay.setBackgroundColor(0x99000000);
        root.addView(dimOverlay, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Clock Container
        clockContainer = new LinearLayout(this);
        clockContainer.setOrientation(LinearLayout.VERTICAL);
        clockContainer.setPadding(30, 20, 30, 20);

        timeView = new TextView(this);
        timeView.setTextSize(72f);
        timeView.setTextColor(0xFFFFFFFF);
        timeView.setTypeface(android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL));

        dateView = new TextView(this);
        dateView.setTextSize(22f);
        dateView.setTextColor(0xB3FFFFFF);
        dateView.setTypeface(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL));

        clockContainer.addView(timeView);
        clockContainer.addView(dateView);

        root.addView(clockContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);

        loadWallpapers();
        updateTime();
    }

    private void loadWallpapers() {
        File dir = Config.dir(this);
        if (dir != null) {
            File wpDir = new File(dir, "wallpapers");
            if (wpDir.exists() && wpDir.isDirectory()) {
                wallpaperFiles.addAll(WallpaperRotation.images(wpDir));
            }
            File single = new File(dir, Config.WALLPAPER_NAME);
            if (single.exists() && !wallpaperFiles.contains(single)) {
                wallpaperFiles.add(0, single);
            }
        }
        if (!wallpaperFiles.isEmpty()) {
            currentWallpaperIdx = random.nextInt(wallpaperFiles.size());
            displayWallpaper(bgA, wallpaperFiles.get(currentWallpaperIdx));
        }
    }

    private void displayWallpaper(ImageView iv, File file) {
        try {
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bmp != null) {
                iv.setImageBitmap(bmp);
            }
        } catch (Throwable ignored) {}
    }

    private void rotateWallpaper() {
        if (wallpaperFiles.size() <= 1) return;
        int nextIdx = currentWallpaperIdx;
        if (wallpaperFiles.size() > 1) {
            while (nextIdx == currentWallpaperIdx) {
                nextIdx = random.nextInt(wallpaperFiles.size());
            }
        }
        currentWallpaperIdx = nextIdx;
        File nextFile = wallpaperFiles.get(currentWallpaperIdx);

        ImageView current = showingA ? bgA : bgB;
        ImageView next = showingA ? bgB : bgA;

        displayWallpaper(next, nextFile);
        next.animate().alpha(1f).setDuration(1500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                current.setAlpha(0f);
                showingA = !showingA;
            }
        }).start();
    }

    private void updateTime() {
        Date now = new Date();
        timeView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(now));
    }

    private void driftClock() {
        clockContainer.animate().alpha(0f).setDuration(800).withEndAction(() -> {
            View parent = (View) clockContainer.getParent();
            if (parent != null && parent.getWidth() > 0 && parent.getHeight() > 0) {
                int maxX = Math.max(0, parent.getWidth() - clockContainer.getWidth() - 100);
                int maxY = Math.max(0, parent.getHeight() - clockContainer.getHeight() - 100);
                float x = 50 + random.nextFloat() * maxX;
                float y = 50 + random.nextFloat() * maxY;
                clockContainer.setX(x);
                clockContainer.setY(y);
            }
            clockContainer.animate().alpha(1f).setDuration(1000).start();
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(driftRunnable);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(driftRunnable);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
        return true;
    }
}
