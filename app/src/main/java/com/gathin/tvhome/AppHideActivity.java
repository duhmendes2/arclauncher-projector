package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manage visible and hidden apps on Home:
 * Lets users hide factory bloatware or unwanted pre-installed projector apps
 * without uninstalling them.
 */
public final class AppHideActivity extends Activity {

    private static class AppItem {
        final String pkg;
        final String label;
        final Drawable icon;
        boolean isHidden;

        AppItem(String pkg, String label, Drawable icon, boolean isHidden) {
            this.pkg = pkg;
            this.label = label;
            this.icon = icon;
            this.isHidden = isHidden;
        }
    }

    private final List<AppItem> appList = new ArrayList<>();
    private AppHideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_hide);

        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restore_all).setOnClickListener(v -> restoreAll());

        loadApps();

        adapter = new AppHideAdapter(appList, (item, hidden) -> {
            item.isHidden = hidden;
            // hidden means removedFromHome = true
            LibraryApps.save(AppHideActivity.this, item.pkg, !hidden);
            setResult(RESULT_OK);
        });
        rv.setAdapter(adapter);
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        List<ResolveInfo> resolved = new ArrayList<>(pm.queryIntentActivities(mainIntent, 0));
        resolved.addAll(pm.queryIntentActivities(leanbackIntent, 0));

        Set<String> seen = new HashSet<>();
        Set<String> removed = LibraryApps.loadRemoved(this);

        for (ResolveInfo ri : resolved) {
            if (ri.activityInfo == null) continue;
            String pkg = ri.activityInfo.packageName;
            if (getPackageName().equals(pkg)) continue; // Don't hide the launcher itself
            if (!seen.add(pkg)) continue;

            String label = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            boolean isHidden = removed.contains(pkg);
            appList.add(new AppItem(pkg, label, icon, isHidden));
        }

        Collections.sort(appList, Comparator.comparing(a -> a.label.toLowerCase()));
    }

    private void restoreAll() {
        for (AppItem item : appList) {
            if (item.isHidden) {
                item.isHidden = false;
                LibraryApps.save(this, item.pkg, true);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        setResult(RESULT_OK);
        Toast.makeText(this, "Todos os aplicativos foram restaurados!", Toast.LENGTH_SHORT).show();
    }

    private static class AppHideAdapter extends RecyclerView.Adapter<AppHideAdapter.Holder> {
        interface ToggleListener {
            void onToggle(AppItem item, boolean hidden);
        }

        private final List<AppItem> items;
        private final ToggleListener listener;

        AppHideAdapter(List<AppItem> items, ToggleListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_hide, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            AppItem item = items.get(position);
            holder.icon.setImageDrawable(item.icon);
            holder.title.setText(item.label);
            holder.pkg.setText(item.pkg);
            holder.checkbox.setChecked(!item.isHidden); // Checked = Visible

            holder.itemView.setOnClickListener(v -> {
                boolean newChecked = !holder.checkbox.isChecked();
                holder.checkbox.setChecked(newChecked);
                listener.onToggle(item, !newChecked);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title, pkg;
            final CheckBox checkbox;

            Holder(View v) {
                super(v);
                icon = v.findViewById(R.id.app_icon);
                title = v.findViewById(R.id.app_title);
                pkg = v.findViewById(R.id.app_package);
                checkbox = v.findViewById(R.id.app_checkbox);
            }
        }
    }
}
