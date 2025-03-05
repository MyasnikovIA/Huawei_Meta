package ru.miacomsoft.huawei_meta;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileObserverService extends Service {
    private static final String TAG = "FileObserverService";
    private FileObserver fileObserver;
    private OrientationSensor orientationSensor;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude = 0;
    private double longitude = 0;

    private String Error="";
    private String path;
    @Override
    public void onCreate() {
        super.onCreate();
        startWatching();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        orientationSensor = new OrientationSensor(getBaseContext());
        orientationSensor.onResume();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Получаем координаты
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("LocationService", "Latitude: " + latitude + ", Longitude: " + longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        // Запрашиваем обновления местоположения
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return START_STICKY; // Сервис будет перезапущен, если система его завершит
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Сервис не поддерживает привязку
    }

    private void startWatching() {
        // Укажите путь к каталогу, который нужно отслеживать
        // String path = getExternalFilesDir(null).getAbsolutePath(); // Пример: внешнее хранилище приложения
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/PANORAMA_HUAWEI/";

        // Проверка доступности каталога
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        fileObserver = new FileObserver(path) {
            @Override
            public void onEvent(int event, String fileName) {
                if ((FileObserver.CREATE & event) != 0) {
                    addMetaInfo(directory,fileName);
                }
            }
        };
        fileObserver.startWatching(); // Начать отслеживание
    }

    private void addMetaInfo(File directory ,String fileName){
        try {
            if (!fileName.substring(fileName.lastIndexOf(".")).toLowerCase().equals(".json")) {
                File imageFile = new File(directory.getPath() + "/" + fileName);
                Double orient_azimuth = orientationSensor.getAZIMUTH();
                Double orient_roll = orientationSensor.getROLL();
                Double orient_pitch = orientationSensor.getPITCH();
                String name = fileName.substring(0, fileName.lastIndexOf("."));
                JSONObject scen = new JSONObject();
                scen.put("default", new JSONObject("{\"firstScene\": \"scene1\"}"));
                scen.put("hotSpotDebug", false);
                scen.put("hotPointDebug", true);
                scen.put("sceneFadeDuration", 1000);
                JSONObject scene1 = new JSONObject();
                scene1.put("hotSpots", new JSONArray());
                scene1.put("panorama", fileName);
                scene1.put("autoLoad", true);
                scene1.put("crossOrigin", "use-credentials");
                scene1.put("lon", longitude);
                scene1.put("lat", latitude);
                scene1.put("orient_azimuth", orient_azimuth);
                scene1.put("orient_roll", orient_roll);
                scene1.put("orient_pitch", orient_pitch);
                scene1.put("title", "title:" + name);
                scene1.put("pitch", ((-1 * orient_roll) - 45));
                scene1.put("yaw", orient_azimuth - 180);
                JSONObject scene = new JSONObject();
                scene.put("scene1", scene1);
                scen.put("scenes", scene);
                Log.d(TAG, scen.toString(4));
                createTextFile(directory,name + ".json", scen.toString(4));
                addCommentToImage(imageFile, scen.toString(4));
                String comment = readCommentFromImage(imageFile);
                Log.d(TAG, "Read comment: " + comment );
            }
        } catch (JSONException e) {
            Log.e(TAG, "addMetaInfo: " + e.toString());
        }
    }

    public String readCommentFromImage(File imageFile) {
        String extractedText = "";
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            // Чтение файла в байтовый массив
            byte[] fileData = new byte[(int) imageFile.length()];
            fis.read(fileData);
            String stopBlock = new String(new byte[]{(byte) 0xFF, (byte) 0xD9});
            String tmp = new String(fileData);
            if (tmp.lastIndexOf("COM")!=-1 && tmp.lastIndexOf(stopBlock)!=-1) {
                tmp = tmp.substring(tmp.lastIndexOf("COM")+3);
                extractedText = tmp.split(new String(new byte[]{(byte) 0xFF, (byte) 0xD9}))[0];
            }
        } catch (IOException e) {
            Log.e(TAG, "readCommentFromImage: " + e.toString());
        }
        return extractedText;
    }

    /**
     * Добавление в JPEG файл комментарий JSON формат
     * todo: дописать механизм чтения
     * @param imageFile
     * @param comment
     */
    private void addCommentToImage(File imageFile, String comment){
        try (RandomAccessFile raf = new RandomAccessFile(imageFile, "rw")) {
            long length = raf.length();
            if (length < 2) {
                throw new IOException("Файл слишком мал для JPEG.");
            }
            raf.seek(length - 2);
            byte[] lastTwoBytes = new byte[2];
            raf.read(lastTwoBytes);
            if (lastTwoBytes[0] != (byte) 0xFF || lastTwoBytes[1] != (byte) 0xD9) {
                 return;
                 //throw new IOException("Файл не является корректным JPEG.");
            }
            raf.seek(length - 2);
            raf.write(("COM" + comment).getBytes());
            raf.write(new byte[]{(byte) 0xFF, (byte) 0xD9});
        } catch (Exception e){
            Log.e(TAG, "addCommentToImage: " + e.toString());
        }
    }

    private void createTextFile( File directory , String fileName, String content) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "createTextFile: " + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fileObserver != null) {
            fileObserver.stopWatching(); // Остановить отслеживание при завершении сервиса
        }
        orientationSensor.onDestroy();
    }
}