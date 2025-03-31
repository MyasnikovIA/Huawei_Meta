package ru.miacomsoft.huawei_meta;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ru.miacomsoft.huawei_meta.file.FileBrowser;
import ru.miacomsoft.huawei_meta.permission.PermissionFile;
import ru.miacomsoft.huawei_meta.permission.PermissionGPS;
import ru.miacomsoft.huawei_meta.services.FileObserverService;
import ru.miacomsoft.huawei_meta.setup.SetupApp;
import ru.miacomsoft.huawei_meta.utils.AppRestartUtil;
import ru.miacomsoft.huawei_meta.photo360.Panorama;
import ru.miacomsoft.huawei_meta.utils.RunExternalApp;
// /home/myasnikovia/AndroidStudioProjects/Huawei_Meta (другая копия)/app/src/main/java/ru/miacomsoft/huawei_meta/MainActivity.java
public class MainActivity extends AppCompatActivity {
    //    adb tcpip 5037
    //    adb connect 192.168.15.50:5037
    private PermissionFile permissionFile;
    private PermissionGPS permissionGPS;
    private String PATH_DIR;
    private String PATH_DIR_PROJECT;
    private Intent serviceIntent;
    private FileBrowser fileBrowser;
    private Panorama panorama;
    private String TAG = "MainActivity";
    private boolean isRunService = false;
    private String selectPhoto = null;
    private JSONObject config;
    private RunExternalApp runExternalApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        runExternalApp = new RunExternalApp(this);
        permissionGPS = new PermissionGPS(this);
        permissionFile = new PermissionFile(this);
        fileBrowser = new FileBrowser(this);
        panorama = new Panorama(this,R.id.webView);
        AppRestartUtil.onError(this);
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
        config = SetupApp.getConfigJSON(this);
        if (config == null)  return;

        try {
            Log.d("config",config.toString(4));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        //PATH_DIR= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/";
        try {
            if (config.has("observer")) {
                PATH_DIR= config.getString("observer");
            } else {
                PATH_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/";
            }
            if (config.has("projectDir")) {
                PATH_DIR_PROJECT= config.getString("projectDir");
            } else {
                PATH_DIR_PROJECT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/";
            }
            if (config.has("FlipTheScreen") && config.getBoolean("FlipTheScreen")) {
                // Перевернуть ориентацию приложения
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                //View rootView = findViewById(android.R.id.content);
                //rootView.setRotation(180);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        startservice();
        File pathProject = new File(PATH_DIR_PROJECT);
        if (!pathProject.exists()) {
            pathProject.mkdirs();
        }
        File pathDir = new File(PATH_DIR);
        if (!pathDir.exists()) {
            pathDir.mkdirs();
        }
        if (panorama.getFilePano() !=null) {
            selectPhoto = panorama.getFilePano().getName();
        }

        panorama.setOnClickHotSpot((JSONObject jsonObj)->{
            try {
                System.out.println(jsonObj.toString(4));
                String imgInfoPath = jsonObj.getString("imgInfoPath");
                String path_dir = jsonObj.getString("path_dir");
                jsonObj.remove("imgInfoPath");
                jsonObj.remove("path_dir");
                Intent intent = new Intent(getApplicationContext(), EditHotSpotMenuActivity.class);
                intent.putExtra("imgInfoPath", imgInfoPath); // Передаем строку
                intent.putExtra("hsJsonStr", jsonObj.toString());    // Передаем число
                startActivityForResult(intent, Panorama.REQUEST_CODE_EDIT);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        panorama.setOnDblClick((JSONObject jsonObj)->{
            try {
                String tmp = jsonObj.toString(4);
                String path_dir = jsonObj.getString("path_dir");
                String from_pitch = jsonObj.getString("from_pitch");
                String from_yaw = jsonObj.getString("from_yaw");
                String imgInfoPath = jsonObj.getString("imgInfoPath");
                JSONObject imgInnfoJson = jsonObj.getJSONObject("imgInnfoJson");
                jsonObj.remove("path_dir");
                jsonObj.remove("imgInfoPath");
                jsonObj.remove("imgInnfoJson");
                jsonObj.remove("path_dir");
                jsonObj.remove("path_dir");
                Intent intent = new Intent(getApplicationContext(), AddHotSpotActivity.class);
                intent.putExtra("imgInfoPath", imgInfoPath);
                intent.putExtra("imgInfojsonStr", imgInnfoJson.toString());    // Передаем число
                intent.putExtra("positionNewPoint", jsonObj.toString());    // Передаем число
                intent.putExtra("path_dir", path_dir);    // Передаем число
                startActivityForResult(intent, Panorama.REQUEST_CODE);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        final Button buttonStartHuawei = (Button)findViewById(R.id.buttonSelectPoint);
        buttonStartHuawei.setOnClickListener(v -> {
            // runExternalApp.run("com.huawei.cvIntl60");
            try {
                runExternalApp.run(SetupApp.CONFIG.getString("PacketNameCamera"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            ;
        });
        final Button buttonSaveImageInfo = (Button)findViewById(R.id.buttonSelectLastGps);
        buttonSaveImageInfo.setOnClickListener(v -> {
            panorama.getSaveInfo();
        });
        final Button buttonDeletePanorama = (Button)findViewById(R.id.buttonDeletePanorama);
        buttonDeletePanorama.setOnClickListener(v -> {
            fileBrowser.deleteFile(()->{
                fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch, PATH_DIR_PROJECT,selectPhoto);
            });
        });
        final Button buttonRenamePanprama = (Button)findViewById(R.id.buttonRenamePanprama);
        buttonRenamePanprama.setOnClickListener(v -> {
            panorama.renamePanorama(()->{
                fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch,PATH_DIR_PROJECT,selectPhoto);
            });
        });

        final Button buttonMap = (Button)findViewById(R.id.buttonCancelSelectMapPoint);
        buttonMap.setOnClickListener(v -> {
            if (panorama.getFilePano() != null  && panorama.getFilePano().exists()) {
                // Запуск окна привязки панорамной фото к карте
                Intent intent = new Intent(this, MapViewActivity.class);
                intent.putExtra("filePano", panorama.getFilePano().getAbsolutePath()); // Передаем путь к Jpg файлу панорамы
                startActivity(intent);
            }
        });

        final Button buttonSetupApp = (Button)findViewById(R.id.buttonSetupApp);
        buttonSetupApp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetupApp.class);
            startActivityForResult(intent, SetupApp.REQUEST_CODE_SETUP_APP);
        });

        final Button buttonMapsPoint = (Button)findViewById(R.id.buttonMapsPoint);
        buttonMapsPoint.setOnClickListener(v -> {
            if (panorama.getFilePano() !=null && panorama.getFilePano().exists() ) {
                Intent intent = new Intent(this, MapViewPointsActivity.class);
                intent.putExtra("filePano", panorama.getFilePano().getAbsolutePath());
                startActivity(intent);
            }
        });
        fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch,PATH_DIR_PROJECT,selectPhoto);
        fileBrowser.onClick((File file)->{
            if (file.exists()) {
                panorama.getPhoto(file, new JSONObject());
            }
        });
    }


    private void startservice() {
        if (!isServiceRunning(FileObserverService.class)) {
            serviceIntent = new Intent(this, FileObserverService.class);
            serviceIntent.putExtra("PATH_DIR", PATH_DIR);
            serviceIntent.putExtra("PATH_DIR_PROJECT", PATH_DIR_PROJECT);
            startService(serviceIntent);
            Toast.makeText(this, "Сервис запущен ", Toast.LENGTH_LONG).show();

//  todo: написать механизм  кликанья по координатам
//            // Координаты для эмуляции нажатия
//            int x = 500; // Пример координаты X
//            int y = 800; // Пример координаты Y
//            // Запуск сервиса
//            Intent intent = new Intent(this, ClickService.class);
//            intent.putExtra("x", x);
//            intent.putExtra("y", y);
//            startService(intent);

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
        SetupApp.onActivityResult(this, requestCode,  resultCode, data, ()->{
            AppRestartUtil.restartApp(this);
            //AppRestartUtil.restartApp(getApplicationContext());// Перезапуск приложения
        });
        AddHotSpotActivity.onActivityResult(this, requestCode,  resultCode, data, (JSONObject selectPano)->{
            panorama.addHotSpot(selectPano);
        });
        EditHotSpotMenuActivity.onActivityResult(this, requestCode,  resultCode, data, (JSONObject hotSpot)->{
            String action = hotSpot.getString("action");
            hotSpot.remove("action");
            if (action.equals("CANCELLATION")) {
                panorama.reloadPanorama(hotSpot);
            } else  if (action.equals("GOTO_HOT_SPOT")) {
                panorama.gotoHotSpot(hotSpot);
            } else  if (action.equals("DELETE_HOT_SPOT")) {
                panorama.deleteHotSpot(hotSpot);
            }
        });
    }


}