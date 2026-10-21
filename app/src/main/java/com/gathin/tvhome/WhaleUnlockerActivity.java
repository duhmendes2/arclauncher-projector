package com.gathin.tvhome;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * System Unlocker for Whale OS / Zeasn and locked Android TV projectors:
 * Provides direct D-pad access to launch hidden AOSP settings, factory/engineering menus,
 * and activate hardware device-administrator privileges without needing numeric keypad remotes.
 */
public final class WhaleUnlockerActivity extends Activity {

    private static class UnlockAction {
        final int icon;
        final String title;
        final String description;
        final Runnable action;

        UnlockAction(int icon, String title, String description, Runnable action) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.action = action;
        }
    }

    private final List<UnlockAction> actions = new ArrayList<>();
    private UnlockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whale_unlocker);

        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        buildActions();

        adapter = new UnlockAdapter(actions);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            buildActions();
            adapter.notifyDataSetChanged();
        }
    }

    private void buildActions() {
        actions.clear();

        // 1. Device Administrator (Real Hardware Shutdown)
        boolean adminActive = TvAdminReceiver.isAdminActive(this);
        actions.add(new UnlockAction(
                R.drawable.ic_home_settings,
                "⚡ Ativar Desligamento Real de Hardware " + (adminActive ? "✅ [ATIVADO]" : "⚠️ [CLIQUE PARA ATIVAR]"),
                adminActive ? "Permissão de hardware ativa! O Sleep Timer já desliga o projetor de verdade no kernel."
                            : "Concede permissão ao Arc Launcher para desligar e suspender a placa do projetor pelo Sleep Timer.",
                () -> TvAdminReceiver.requestAdmin(this)
        ));

        // 2. Accessibility Service (Power Dialog & Shortcuts)
        boolean accessActive = HomeKeyService.isServiceRunning();
        actions.add(new UnlockAction(
                R.drawable.ic_settings,
                "🔑 Serviço de Acessibilidade do Controle " + (accessActive ? "✅ [ATIVADO]" : "⚠️ [RECOMENDADO]"),
                accessActive ? "Serviço ativo para captura do controle e envio de sinais de energia."
                             : "Abre as opções de acessibilidade para ligar o 'Arc Launcher Home Service'.",
                () -> tryStart(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        ));

        // 3. Force Open AOSP Developer Options
        actions.add(new UnlockAction(
                R.drawable.ic_settings,
                "📱 Forçar Abertura das 'Opções do Desenvolvedor'",
                "Burlar o Whale OS e tentar abrir direto a tela oculta para ativar Depuração USB / ADB.",
                this::openDeveloperSettings
        ));

        // 4. Force Open Pure AOSP Device Info (7 clicks)
        actions.add(new UnlockAction(
                R.drawable.ic_settings,
                "ℹ️ Abrir 'Sobre o Aparelho' (Android AOSP Puro)",
                "Abre a tela original do Android onde o clique de 7 vezes no Build Number não está desativado pelo Whale OS.",
                this::openDeviceInfoSettings
        ));

        // 5. Open Factory / Engineering Menus
        actions.add(new UnlockAction(
                R.drawable.ic_settings,
                "🛠️ Abrir Menus de Engenharia / Fábrica da Placa",
                "Tenta invocar telas de teste de fábrica (MStar, CVTE, Amlogic, WhaleTV) sem precisar de teclado numérico.",
                this::openFactoryMenus
        ));

        // 6. Open Hidden Android Testing Screen (*#*#4636#*#*)
        actions.add(new UnlockAction(
                R.drawable.ic_settings,
                "⚙️ Abrir Tela de Testes do Sistema (*#*#4636#*#*)",
                "Abre a tela secreta de diagnósticos de rádio, rede e estatísticas de uso do Android.",
                () -> {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.settings", "com.android.settings.TestingSettings");
                    if (!tryStart(intent)) {
                        Toast.makeText(this, "Não foi possível abrir a tela de testes.", Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    private void openDeveloperSettings() {
        String[][] targets = {
            {"com.android.settings", "com.android.settings.DevelopmentSettings"},
            {"com.android.settings", "com.android.settings.Settings$DevelopmentSettingsDashboardActivity"},
            {"com.android.settings", "com.android.settings.DevelopmentSettingsActivity"},
            {"com.whaletv.settings", "com.whaletv.settings.DeveloperActivity"},
            {"com.zeasn.whale.settings", "com.zeasn.whale.settings.DeveloperActivity"}
        };
        for (String[] t : targets) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(t[0], t[1]));
            if (tryStart(intent)) return;
        }
        if (!tryStart(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))) {
            Toast.makeText(this, "Whale OS bloqueou o atalho direto. Tente abrir pelo 'Sobre o Aparelho' abaixo.", Toast.LENGTH_LONG).show();
        }
    }

    private void openDeviceInfoSettings() {
        String[][] targets = {
            {"com.android.settings", "com.android.settings.DeviceInfoSettings"},
            {"com.android.settings", "com.android.settings.Settings$MyDeviceInfoActivity"},
            {"com.android.settings", "com.android.settings.Settings$DeviceInfoSettingsActivity"}
        };
        for (String[] t : targets) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(t[0], t[1]));
            if (tryStart(intent)) return;
        }
        if (!tryStart(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))) {
            Toast.makeText(this, "Não foi possível abrir a tela sobre o aparelho.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFactoryMenus() {
        String[][] targets = {
            {"com.mstar.tv.setting", "com.mstar.tv.setting.MainActivity"},
            {"com.cvte.tv.factory", "com.cvte.tv.factory.MainActivity"},
            {"com.droidlogic.factorymenu", "com.droidlogic.factorymenu.MainActivity"},
            {"com.whaletv.settings", "com.whaletv.settings.FactoryActivity"},
            {"com.softwinner.tv.settings", "com.softwinner.tv.settings.DisplaySettingsActivity"}
        };
        for (String[] t : targets) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(t[0], t[1]));
            if (tryStart(intent)) return;
        }
        Toast.makeText(this, "Nenhum menu de fábrica respondeu ao sinal direto.", Toast.LENGTH_SHORT).show();
    }

    private boolean tryStart(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static class UnlockAdapter extends RecyclerView.Adapter<UnlockAdapter.Holder> {
        private final List<UnlockAction> items;

        UnlockAdapter(List<UnlockAction> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_whale_unlocker, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            UnlockAction item = items.get(position);
            holder.icon.setImageResource(item.icon);
            holder.title.setText(item.title);
            holder.desc.setText(item.description);
            holder.itemView.setOnClickListener(v -> item.action.run());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title, desc;

            Holder(View v) {
                super(v);
                icon = v.findViewById(R.id.icon);
                title = v.findViewById(R.id.title);
                desc = v.findViewById(R.id.desc);
            }
        }
    }
}
