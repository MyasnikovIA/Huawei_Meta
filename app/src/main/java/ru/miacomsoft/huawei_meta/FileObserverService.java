package ru.miacomsoft.huawei_meta;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;

import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class FileObserverService extends Service {
    private static final String TAG = "FileObserverService";
    private FileObserver fileObserver;
    private OrientationSensor orientationSensor;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude = 0;
    private double longitude = 0;
    private String Error="";
    private File pathDirFile;
    private File pathProjectDirFile;
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String pathDir = intent.getStringExtra("PATH_DIR");
        if (pathDir != null) {
            pathDirFile = new File(pathDir);
        } else {
            pathDirFile = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/");
        }
        String pathProjectDir = intent.getStringExtra("PATH_DIR_PROJECT");
        if (pathDir != null) {
            pathProjectDirFile = new File(pathProjectDir);
        } else {
            pathProjectDirFile = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CV60/");
        }
        startWatching();

        orientationSensor = new OrientationSensor(getBaseContext());
        orientationSensor.onResume();

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
        // path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/PANORAMA_HUAWEI/";
        if (!pathDirFile.exists()) {
            pathDirFile.mkdirs();
        }
        if (!pathProjectDirFile.exists()) {
            pathProjectDirFile.mkdirs();
        }
        fileObserver = new FileObserver(pathDirFile.getAbsolutePath()) {
            @Override
            public void onEvent(int event, String fileName) {
                if ((FileObserver.CREATE & event) != 0) {
                    addMetaInfo(pathDirFile,pathProjectDirFile,fileName);
                }
            }
        };
        fileObserver.startWatching();
    }

    public static class MyThread extends Thread {
        private File imageFilesrc;
        private File imageFile;
        public MyThread(File imageFilesrc,File imageFile) {
            this.imageFilesrc = imageFilesrc;
            this.imageFile = imageFile;
        }
        @Override
        public void run() {
            imageFilesrc.renameTo(imageFile);
        }
    }
    public static void copyFile(File src, File dest) throws IOException {
        // Проверяем, существует ли исходный файл
        if (!src.exists()) {
            throw new IOException("Source file does not exist: " + src.getAbsolutePath());
        }

        // Создаем целевой файл, если он не существует
        if (!dest.exists()) {
            dest.createNewFile();
        }

        // Копируем данные из исходного файла в целевой
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
    public static boolean compareFiles(File file1, File file2) throws IOException {
        // Проверяем, имеют ли файлы одинаковый размер
        if (file1.length() != file2.length()) {
            return false;
        }

        try (FileInputStream fis1 = new FileInputStream(file1);
             FileInputStream fis2 = new FileInputStream(file2)) {

            int byte1, byte2;
            do {
                byte1 = fis1.read();
                byte2 = fis2.read();
                if (byte1 != byte2) {
                    return false;
                }
            } while (byte1 != -1 && byte2 != -1);
        }
        return true;
    }

    public static void copyFile(Context context, Uri srcUri, Uri destUri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(srcUri);
             OutputStream outputStream = resolver.openOutputStream(destUri)) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void copyFileToDownloads(Context context, File srcFile, String displayName) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        // Создаем ContentValues для нового файла
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
        values.put(MediaStore.Downloads.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        // Вставляем файл в MediaStore
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Failed to create new MediaStore record.");
        }

        // Копируем данные из исходного файла в новый файл
        try (OutputStream out = resolver.openOutputStream(uri);
             FileInputStream in = new FileInputStream(srcFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void addMetaInfo(File directory ,File pathProjectDirFile ,String fileName){
        try {
            if (!fileName.substring(fileName.lastIndexOf(".")).toLowerCase().equals(".json")) {
                if (longitude==0 || latitude==0) {
                    @SuppressLint("MissingPermission")
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                }
                File imageFilesrc = new File(directory.getPath() + "/" + fileName);
                File imageFile = new File(pathProjectDirFile.getPath() + "/" + fileName);
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
                createTextFile(pathProjectDirFile,name + ".json", scen.toString(4));
                addCommentToImage(imageFile, scen.toString(4));
                String comment = readCommentFromImage(imageFile);
                Log.d(TAG, "Read comment: " + comment );

                // todo: написать копирорвание файла
                // copyFileToDownloads(getApplicationContext(), imageFilesrc,imageFile);
                if (!imageFilesrc.getAbsolutePath().equals(imageFile.getAbsolutePath())) {
                    copyFile(imageFilesrc,imageFile);
                    while (!compareFiles(imageFilesrc,imageFile)) {
                        Thread.sleep(1000);
                    }
                    // FileUtils.copyFile(imageFilesrc,imageFile);
                }
            }
        } catch (Exception e) {
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