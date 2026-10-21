package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Fullscreen calibration pattern for projectors:
 * - 16:9 frame and corner markers for keystone correction.
 * - Millimetric grid for geometry and distortion checking.
 * - Central and corner focus stars / concentric rings for optical lens focusing.
 * - 10-step grayscale ramp for contrast/brightness adjustment.
 * Pressing any remote button dismisses the pattern.
 */
public final class CalibrationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(new CalibrationView(this));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
        return true;
    }

    private static class CalibrationView extends View {
        private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint majorGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect textBounds = new Rect();

        public CalibrationView(Context context) {
            super(context);
            setBackgroundColor(0xFF000000);

            gridPaint.setColor(0x28FFFFFF);
            gridPaint.setStrokeWidth(1.5f);

            majorGridPaint.setColor(0x60FFFFFF);
            majorGridPaint.setStrokeWidth(2.5f);

            framePaint.setColor(0xFF00E5FF); // Neon cyan
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setStrokeWidth(4f);

            focusPaint.setColor(0xFFFFFFFF);
            focusPaint.setStyle(Paint.Style.STROKE);
            focusPaint.setStrokeWidth(2f);

            textPaint.setColor(0xB3FFFFFF);
            textPaint.setTextSize(24f);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            // 1. Outer 16:9 Safety Frame
            float margin = 20f;
            canvas.drawRect(margin, margin, w - margin, h - margin, framePaint);

            // 2. Corner Alignment Marks (L-shapes)
            float lSize = 80f;
            Paint cornerPaint = new Paint(framePaint);
            cornerPaint.setColor(0xFFFFD600); // Bright Yellow
            cornerPaint.setStrokeWidth(6f);

            // Top-Left
            canvas.drawLine(margin, margin, margin + lSize, margin, cornerPaint);
            canvas.drawLine(margin, margin, margin, margin + lSize, cornerPaint);
            // Top-Right
            canvas.drawLine(w - margin, margin, w - margin - lSize, margin, cornerPaint);
            canvas.drawLine(w - margin, margin, w - margin, margin + lSize, cornerPaint);
            // Bottom-Left
            canvas.drawLine(margin, h - margin, margin + lSize, h - margin, cornerPaint);
            canvas.drawLine(margin, h - margin, margin, h - margin - lSize, cornerPaint);
            // Bottom-Right
            canvas.drawLine(w - margin, h - margin, w - margin - lSize, h - margin, cornerPaint);
            canvas.drawLine(w - margin, h - margin, w - margin, h - margin - lSize, cornerPaint);

            // 3. Grid Lines (16 horizontal divisions, 9 vertical divisions)
            int cols = 16;
            int rows = 9;
            float stepX = (float) w / cols;
            float stepY = (float) h / rows;

            for (int i = 1; i < cols; i++) {
                float x = i * stepX;
                Paint p = (i == cols / 2) ? majorGridPaint : gridPaint;
                canvas.drawLine(x, margin, x, h - margin, p);
            }
            for (int j = 1; j < rows; j++) {
                float y = j * stepY;
                Paint p = (j == rows / 2) ? majorGridPaint : gridPaint;
                canvas.drawLine(margin, y, w - margin, y, p);
            }

            // 4. Center & Corner Focus Targets
            drawFocusTarget(canvas, w / 2f, h / 2f, 90f);
            drawFocusTarget(canvas, w * 0.18f, h * 0.22f, 50f);
            drawFocusTarget(canvas, w * 0.82f, h * 0.22f, 50f);
            drawFocusTarget(canvas, w * 0.18f, h * 0.78f, 50f);
            drawFocusTarget(canvas, w * 0.82f, h * 0.78f, 50f);

            // 5. Grayscale Ramp (10 steps) near bottom
            int steps = 10;
            float rampW = w * 0.5f;
            float rampH = 26f;
            float rampLeft = (w - rampW) / 2f;
            float rampTop = h - margin - 80f;
            float blockW = rampW / steps;

            for (int k = 0; k < steps; k++) {
                int gray = (int) (k * (255f / (steps - 1)));
                stepPaint.setColor(Color.rgb(gray, gray, gray));
                canvas.drawRect(rampLeft + k * blockW, rampTop, rampLeft + (k + 1) * blockW, rampTop + rampH, stepPaint);
            }

            // 6. Hint text
            String hint = "Padrão de Calibração de Foco & Keystone • Pressione qualquer botão do controle para sair";
            canvas.drawText(hint, w / 2f, h - margin - 25f, textPaint);
        }

        private void drawFocusTarget(Canvas canvas, float cx, float cy, float radius) {
            // Concentric circles
            for (float r = radius; r > 10; r -= 15f) {
                canvas.drawCircle(cx, cy, r, focusPaint);
            }
            // Crosshairs
            canvas.drawLine(cx - radius * 1.3f, cy, cx + radius * 1.3f, cy, focusPaint);
            canvas.drawLine(cx, cy - radius * 1.3f, cx, cy + radius * 1.3f, focusPaint);
            // Diagonal star lines for focus astigmatism test
            float diag = radius * 0.7f;
            canvas.drawLine(cx - diag, cy - diag, cx + diag, cy + diag, gridPaint);
            canvas.drawLine(cx - diag, cy + diag, cx + diag, cy - diag, gridPaint);
        }
    }
}
