package com.proyectx.focus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Queries only launchable packages visible to Android; no inventory leaves the device. */
public final class InstalledAppRepository {
    private InstalledAppRepository() {}

    public static List<InstalledApp> listLaunchable(Context context) {
        PackageManager packages = context.getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packages.queryIntentActivities(launcher, PackageManager.MATCH_DEFAULT_ONLY);
        Map<String, InstalledApp> byPackage = new LinkedHashMap<>();
        for (ResolveInfo activity : activities) {
            if (activity.activityInfo == null) continue;
            String packageName = activity.activityInfo.packageName;
            if (context.getPackageName().equals(packageName)) continue;
            try {
                CharSequence rawLabel = activity.loadLabel(packages);
                String label = rawLabel == null || rawLabel.toString().isBlank() ? packageName : rawLabel.toString();
                if (!byPackage.containsKey(packageName))
                    byPackage.put(packageName, new InstalledApp(label, packageName));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed package metadata supplied by the platform.
            }
        }
        List<InstalledApp> result = new ArrayList<>(byPackage.values());
        Collections.sort(result, APP_ORDER);
        return result;
    }

    private static final Comparator<InstalledApp> APP_ORDER = new Comparator<InstalledApp>() {
        @Override
        public int compare(InstalledApp first, InstalledApp second) {
            int labels = first.label()
                    .toLowerCase(java.util.Locale.ROOT)
                    .compareTo(second.label().toLowerCase(java.util.Locale.ROOT));
            return labels != 0 ? labels : first.packageName().compareTo(second.packageName());
        }
    };
}
