package com.gathin.tvhome;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Device Administrator receiver to grant hardware power-management and screen-locking privileges.
 * Allows the sleep timer to legitimately suspend/lock the projector hardware without root.
 */
public class TvAdminReceiver extends DeviceAdminReceiver {

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), TvAdminReceiver.class);
    }

    public static boolean isAdminActive(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm != null && dpm.isAdminActive(getComponentName(context));
    }

    public static void requestAdmin(Activity activity) {
        if (isAdminActive(activity)) {
            Toast.makeText(activity, "⚡ Permissão de hardware já está ATIVA!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(activity));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Permite que o Arc Launcher desligue e suspenda o hardware do projetor quando o Sleep Timer expirar.");
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Não foi possível abrir o diálogo de administrador.", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean lockNow(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null && dpm.isAdminActive(getComponentName(context))) {
            try {
                dpm.lockNow();
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "⚡ Arc Launcher ativado como Administrador de Hardware!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, "Permissão de hardware desativada.", Toast.LENGTH_SHORT).show();
    }
}
