package ru.miacomsoft.huawei_meta;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;




public class MainActivity extends AppCompatActivity {
    //    adb tcpip 5037
    //    adb connect 192.168.15.50:5037
    private PermissionFile permissionFile;
    private PermissionGPS permissionGPS;
    private RunExternalApp runExternalApp;

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // перевернуть ориентацию приложения
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        //View rootView = findViewById(android.R.id.content);
        //rootView.setRotation(180);

        runExternalApp = new RunExternalApp(this);
        permissionGPS = new PermissionGPS(this);
        permissionFile = new PermissionFile(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        permissionFile.checkPermissions(()->{
            permissionGPS.checkPermissions(() -> {
                onStartApp();
            });
        });
    }


    private void onStartApp() {
        startservice();
        final Button buttonStartHuawei = (Button)findViewById(R.id.buttonStartHuawei);
        buttonStartHuawei.setOnClickListener(v -> {
            runExternalApp.run("com.huawei.cvIntl60");
        });
    }


    private void startservice() {
        if (serviceIntent!=null) {
            stopService(serviceIntent);
            serviceIntent = null;
        }
        serviceIntent = new Intent(this, FileObserverService.class);
        serviceIntent.putExtra("PATH_DIR", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/PANORAMA_HUAWEI/");
        startService(serviceIntent);
        Toast.makeText(this, "Сервис запущен ", Toast.LENGTH_LONG).show();
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
        permissionFile.onActivityResult(requestCode, resultCode, data);
    }
}