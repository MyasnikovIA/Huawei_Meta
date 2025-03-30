package ru.miacomsoft.huawei_meta.permission;


import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

/**
 *
 *     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 *     <uses-permission android:name="android.permission.WRITE_INTERRNAL" />
 *     <uses-permission android:name="android.permission.READ_INTERRNAL" />
 *     <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
 *     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 *
 */
public class PermissionFile {
    public interface CallbackEmptyReturn {
        void call();
    }
    private AppCompatActivity appCompatActivity; // Активность, которая запрашивает разрешения
    private CallbackEmptyReturn callbackEmptyReturn; // Коллбэк для обработки успешного получения разрешений
    private static final int REQUEST_CODE_MANAGE_STORAGE = 100;

    public PermissionFile(AppCompatActivity appCompatActivity){
        this.appCompatActivity = appCompatActivity;
    }

    /**
     * Запуск проверки наличия разрешения на доступ к диску
     * @param callbackEmptyReturn
     */
    public void checkPermissions(CallbackEmptyReturn callbackEmptyReturn) {
        this.callbackEmptyReturn = callbackEmptyReturn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Запрашиваем разрешение
                setupPermission();
            } else {
                // Разрешение уже предоставлено, создаем файл
                if (this.callbackEmptyReturn!=null) {
                    this.callbackEmptyReturn.call();
                };
            }
        }
    }

    /**
     * Запуск модального окна для выдачи прав
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setupPermission(){
        Intent intent = new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", appCompatActivity.getPackageName(), null);
        intent.setData(uri);
        appCompatActivity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
    }


    /**
     *    Обработка на экшине события закрытия  модального окна для выдачи прав (новый вариант)
     *
     *     @Override
     *     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
     *         super.onActivityResult(requestCode, resultCode, data);
     *         permissionFile.onActivityResult(requestCode, resultCode, data);
     *     }
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    //@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Разрешение предоставлено, создаем файл
                    if (this.callbackEmptyReturn!=null) {
                        this.callbackEmptyReturn.call();
                    };
                } else {
                    setupPermission();
                }
            }
        }
    }


    /**
     *    Обработка на экшине события закрытия  модального окна для выдачи прав (старый вариант)
     *
     *     @Override
     *     public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
     *         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
     *         permissionFile.onRequestPermissionsResult(requestCode, permissions, grantResults);
     *     }
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    //@Override
    public  void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Разрешение предоставлено, создаем файл
                        if (this.callbackEmptyReturn!=null) {
                            this.callbackEmptyReturn.call();
                        };
                    } else {
                        setupPermission();
                    }
                }else {
                    if (this.callbackEmptyReturn!=null) {
                        this.callbackEmptyReturn.call();
                    };
                }
            }
        }
    }

}
