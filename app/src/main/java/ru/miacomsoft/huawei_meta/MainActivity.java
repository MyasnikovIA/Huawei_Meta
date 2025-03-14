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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

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
    private String TAG = "MainActivity";

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
        JSONObject config = SetupApp.getConfigJSON(this);
        if (config==null) {
            // если нет файла настройки, тогда открываем окно для конфигурирования приложения
            Intent intent = new Intent(this, SetupApp.class);
            startActivityForResult(intent, SetupApp.REQUEST_CODE_SETUP_APP);
            return;
        }
        //PATH_DIR= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/";

        try {
            if (config.has("PATH_DIR")) {
                PATH_DIR= config.getString("PATH_DIR");
            } else {
                PATH_DIR= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/";
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        startservice();
        final Button buttonStartHuawei = (Button)findViewById(R.id.buttonSelectPoint);
        buttonStartHuawei.setOnClickListener(v -> {
            runExternalApp.run("com.huawei.cvIntl60");
        });
        final Button buttonSaveImageInfo = (Button)findViewById(R.id.buttonDeletePoint);
        buttonSaveImageInfo.setOnClickListener(v -> {
            panorama.getSaveInfo();
        });
        final Button buttonDeletePanorama = (Button)findViewById(R.id.buttonDeletePanorama);
        buttonDeletePanorama.setOnClickListener(v -> {
            panorama.deletePanorama(()->{
                fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch,PATH_DIR);
            });
        });
        final Button buttonRenamePanprama = (Button)findViewById(R.id.buttonRenamePanprama);
        buttonRenamePanprama.setOnClickListener(v -> {
            panorama.renamePanorama(()->{
                fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch,PATH_DIR);
            });
        });

        final Button buttonMap = (Button)findViewById(R.id.buttonCancelSelectMapPoint);
        buttonMap.setOnClickListener(v -> {
            if (panorama.getFilePano() !=null) {
                // Запуск окна привязки панорамной фото к карте
                Intent intent = new Intent(this, MapView.class);
                intent.putExtra("filePano", panorama.getFilePano().getAbsolutePath()); // Передаем путь к Jpg файлу панорамы
                startActivity(intent);
            }
        });

        final Button buttonSetupApp = (Button)findViewById(R.id.buttonSetupApp);
        buttonSetupApp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetupApp.class);
            startActivityForResult(intent, SetupApp.REQUEST_CODE_SETUP_APP);
        });


        fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch,PATH_DIR);
        fileBrowser.onClick((File file)->{
            panorama.getPhoto(R.id.webView,file,new JSONObject());
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
        if (requestCode == Panorama.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectPanoString = data.getStringExtra("SELECT_PANO");
            if (selectPanoString != null) {
                try {
                    if (!selectPanoString.equals("{}")) {
                        panorama.addHotSpot(new JSONObject(selectPanoString));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (requestCode == Panorama.REQUEST_CODE_EDIT && resultCode == RESULT_OK && data != null) {
            String hot_spot = data.getStringExtra("HOT_SPOT");
            if (hot_spot != null) {
                try {
                    if (!hot_spot.equals("{}")) {
                        String action = data.getStringExtra("ACTION");
                        JSONObject hotSpot = new JSONObject(hot_spot);
                        if (action != null) {
                            if (action.equals("CANCELLATION")) {
                                panorama.reloadPanorama(hotSpot);
                            } else  if (action.equals("GOTO_HOT_SPOT")) {
                                panorama.gotoHotSpot(hotSpot);
                            } else  if (action.equals("DELETE_HOT_SPOT")) {
                                panorama.deleteHotSpot(hotSpot);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "createEmptyInfoFileJson: " + e.toString());
                }
            }
        } else if (requestCode == SetupApp.REQUEST_CODE_SETUP_APP && resultCode == RESULT_OK && data != null) {
            // после изменений настроек перезапускаем приложение снова
            onStartApp();
        }


    }


}