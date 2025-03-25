package ru.miacomsoft.huawei_meta;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class AppRestartUtil {

    /**
     * Перезапуск приложения
     * @param context
     */
   public static void restartApp(Context context) {
        // Получаем Intent для запуска главного Activity
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());

        if (intent != null) {
            // Устанавливаем флаги для очистки стека активностей
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Используем Handler для задержки перед перезапуском
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Запускаем главное Activity
                    context.startActivity(intent);

                    // Завершаем текущий процесс
                    Runtime.getRuntime().exit(0);
                }
            }, 300); // Задержка 300 миллисекунд
        }
    }
}