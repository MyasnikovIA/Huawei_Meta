package ru.miacomsoft.huawei_meta;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.miacomsoft.huawei_meta.view_photo.Panorama;

public class MainActivity extends AppCompatActivity {
    //    adb tcpip 5037
    //    adb connect 192.168.15.50:5037
    private PermissionFile permissionFile;
    private PermissionGPS permissionGPS;
    private RunExternalApp runExternalApp;
    private String PATH_DIR;
    private Intent serviceIntent;
    private FileBrowser fileBrowser;

    private Panorama panorama;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Перевернуть ориентацию приложения
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        //View rootView = findViewById(android.R.id.content);
        //rootView.setRotation(180);
        runExternalApp = new RunExternalApp(this);
        permissionGPS = new PermissionGPS(this);
        permissionFile = new PermissionFile(this);
        fileBrowser = new FileBrowser(this);
        panorama = new Panorama(this);
    }

    @Override
    public void onStart() {
        super.onStart();
            permissionFile.checkPermissions(() -> {
                permissionGPS.checkPermissions(() -> {
                    onStartApp();
                });
            });
    }


    private void onStartApp() {
        PATH_DIR= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/";
        startservice();
        final Button buttonStartHuawei = (Button)findViewById(R.id.buttonStartHuawei);
        buttonStartHuawei.setOnClickListener(v -> {
            runExternalApp.run("com.huawei.cvIntl60");
        });
        fileBrowser.getFileList(R.id.FileListView,R.id.editTextFilter,PATH_DIR);
        fileBrowser.onClick((File file)->{
            panorama.getPhoto(R.id.webView,file);
        });
    }


    private void startservice() {
        if (!isServiceRunning(FileObserverService.class)) {
            serviceIntent = new Intent(this, FileObserverService.class);
            serviceIntent.putExtra("PATH_DIR", PATH_DIR);
            startService(serviceIntent);
            Toast.makeText(this, "Сервис запущен ", Toast.LENGTH_LONG).show();
        }
        //  stopService(serviceIntent);
        //  serviceIntent = null;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            // Получаем список всех запущенных сервисов
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true; // Сервис запущен
                }
            }
        }
        return false; // Сервис не запущен
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionFile.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionGPS.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       // permissionFile.onActivityResult(requestCode, resultCode, data);
    }



}