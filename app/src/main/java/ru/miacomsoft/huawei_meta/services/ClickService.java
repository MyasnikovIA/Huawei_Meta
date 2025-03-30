package ru.miacomsoft.huawei_meta.services;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

public class ClickService extends Service {
    private static final String TAG = "ClickService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Получаем координаты из Intent
        int x = intent.getIntExtra("x", 0);
        int y = intent.getIntExtra("y", 0);
        // Эмулируем нажатие
        emulateClick(x, y);
        return START_NOT_STICKY;
    }

    private void emulateClick(int x, int y) {
        try {
            // Создаем событие нажатия (ACTION_DOWN)
            long downTime = System.currentTimeMillis();
            MotionEvent downEvent = MotionEvent.obtain(
                    downTime,
                    downTime,
                    MotionEvent.ACTION_DOWN,
                    x,
                    y,
                    0
            );

            // Создаем событие отпускания (ACTION_UP)
            long upTime = System.currentTimeMillis() + ViewConfiguration.getTapTimeout();
            MotionEvent upEvent = MotionEvent.obtain(
                    upTime,
                    upTime,
                    MotionEvent.ACTION_UP,
                    x,
                    y,
                    0
            );
            // Отправляем события
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendPointerSync(downEvent);
            instrumentation.sendPointerSync(upEvent);

            Log.d(TAG, "Click emulated at (" + x + ", " + y + ")");
        } catch (Exception e) {
            Log.e(TAG, "Failed to emulate click: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}