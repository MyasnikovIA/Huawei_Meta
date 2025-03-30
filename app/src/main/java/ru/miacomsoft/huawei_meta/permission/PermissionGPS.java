package ru.miacomsoft.huawei_meta.permission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ru.miacomsoft.huawei_meta.gps_location.GpsManager;

public class PermissionGPS {
    public interface CallbackEmptyReturn {
        void call();
    }

    private AppCompatActivity appCompatActivity; // Активность, которая запрашивает разрешения
    private CallbackEmptyReturn callbackEmptyReturn; // Коллбэк для обработки успешного получения разрешений
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 200;


    public PermissionGPS(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }

    /**
     * Запуск проверки наличия разрешения на доступ к GPS
     * @param callbackEmptyReturn - вложенная функция предназначена для запуска если все разрешения выданны
     */
    public void checkPermissions(CallbackEmptyReturn callbackEmptyReturn) {
        this.callbackEmptyReturn = callbackEmptyReturn;

        GpsManager.isExistGpsDevice = GpsManager.hasGpsModule(appCompatActivity);

        if (ContextCompat.checkSelfPermission(appCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(appCompatActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            // Разрешение уже предоставлено
            if (this.callbackEmptyReturn != null) {
                this.callbackEmptyReturn.call();
            }
        }
    }

    /**
     * Обработка результата запроса разрешений
     *
     * @param requestCode  код запроса
     * @param permissions  запрашиваемые разрешения
     * @param grantResults результаты предоставления разрешений
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено
                if (this.callbackEmptyReturn != null) {
                    this.callbackEmptyReturn.call();
                }
            } else {
                // Разрешение отклонено, можно показать пользователю сообщение
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (appCompatActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Пользователь отклонил разрешение, но не поставил галочку "Не спрашивать снова"
                        ActivityCompat.requestPermissions(appCompatActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                    } else {
                        // Пользователь отклонил разрешение и поставил галочку "Не спрашивать снова"
                        // Можно перенаправить пользователя в настройки
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", appCompatActivity.getPackageName(), null);
                        intent.setData(uri);
                        appCompatActivity.startActivity(intent);
                    }
                }
            }
        }
    }
}