package ru.miacomsoft.huawei_meta.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 *     <!-- Запуск внешней программы -->
 *     <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" tools:ignore="QueryAllPackagesPermission" />
 */
public class RunExternalApp {

    private Context context;

    public RunExternalApp(Context context) {
        this.context = context;
    }

    /**
     * Запустить приложение по имени пакета
     * @param packageName
     */
    public void run(String packageName ) {
        // String packageName = "com.huawei.cvIntl60";
        if (isPackageInstalled(packageName)) {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                context.startActivity(launchIntent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + packageName));
                context.startActivity(intent);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            context.startActivity(intent);
        }
    }

    /**
     * Проверка наличия установленного приложения (пакета)
     * @param packageName
     * @return
     */
    private boolean isPackageInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
