package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Screen-off / Audio-only mode for projectors:
 * Turns the projected screen pitch black and lowers window brightness to near-zero
 * while allowing background audio (Spotify, Stremio, Bluetooth, etc.) to keep playing.
 * Pressing any remote key immediately restores the display.
 */
public final class ScreenOffActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Lower brightness to absolute minimum
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = 0.001f;
        window.setAttributes(lp);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);

        TextView hint = new TextView(this);
        hint.setText("📻 Modo Apenas Áudio\nLâmpada / Imagem Apagada\n\nPressione qualquer botão para ligar a tela");
        hint.setTextColor(0x80FFFFFF);
        hint.setTextSize(20f);
        hint.setGravity(android.view.Gravity.CENTER);
        hint.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER);
        root.addView(hint, params);

        setContentView(root);

        // Fade out hint after 3 seconds so the screen becomes 100% black
        handler.postDelayed(() -> {
            if (!isFinishing()) {
                hint.animate().alpha(0f).setDuration(1200).start();
            }
        }, 3000L);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
        return true;
    }
}
