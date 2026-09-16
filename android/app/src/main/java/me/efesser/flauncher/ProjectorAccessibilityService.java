package me.efesser.flauncher;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class ProjectorAccessibilityService extends AccessibilityService {

    private static final String MY_PACKAGE = "com.omeda.arc";
    private long lastLaunchTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageNameChar = event.getPackageName();
            if (packageNameChar != null) {
                String pkg = packageNameChar.toString();
                if (isStockLauncherPackage(pkg)) {
                    launchArcLauncher();
                }
            }
        }
    }

    private boolean isStockLauncherPackage(String pkg) {
        if (pkg.equals(MY_PACKAGE)) return false;
        String lower = pkg.toLowerCase();
        return lower.contains("whaletv")
                || lower.contains("whale.tv")
                || lower.contains("zeasn")
                || lower.contains("tvlauncher")
                || lower.contains("leanbacklauncher")
                || lower.contains("googlequicksearchbox")
                || lower.contains("mstar.tv.home");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME && event.getAction() == KeyEvent.ACTION_UP) {
            launchArcLauncher();
            return true;
        }
        return super.onKeyEvent(event);
    }

    private void launchArcLauncher() {
        long now = System.currentTimeMillis();
        if (now - lastLaunchTime < 500) {
            return;
        }
        lastLaunchTime = now;

        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    @Override
    public void onInterrupt() {}
}
