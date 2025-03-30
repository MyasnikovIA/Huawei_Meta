package ru.miacomsoft.huawei_meta.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class AppRestartUtil {

    private static Thread.UncaughtExceptionHandler defaultHandler;

    public static void onError(AppCompatActivity appCompatActivity) {
        // Для перезапуска после исключения (в классе Application):
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            // Логирование ошибки
            Log.e("AppCrash", "Crash detected", ex);
            // Перезапуск приложения
            AppRestartUtil.restartApp(appCompatActivity.getApplicationContext());
            // Вызов стандартного обработчика (необязательно)
            defaultHandler.uncaughtException(thread, ex);
        });
    }

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
