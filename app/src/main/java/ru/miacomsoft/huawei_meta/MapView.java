package ru.miacomsoft.huawei_meta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import ru.miacomsoft.huawei_meta.view_map.OsmMap;

public class MapView extends AppCompatActivity {
    private String TAG = "view_map.OsmMap";
    private WebView webView;
    private OsmMap osmMap;
    private File filePanoFile;
    private File filePanoJson;
    private JSONObject objJson;
    private double lat=0;
    private double lon=0;
    private double latGps=0;
    private double lonGps=0;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Перевернуть ориентацию приложения
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latGps = location.getLatitude();
                lonGps = location.getLongitude();
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String filePano = intent.getStringExtra("filePano");
        if (filePano!=null) {
            filePanoFile = new File(filePano);
            filePanoJson = new File(filePanoFile.getParentFile().getAbsolutePath(), filePanoFile.getName().substring(0, filePanoFile.getName().length() - 4) + ".json");
            String imageInfoJsonStr = readTextFile(filePanoJson.getParentFile(), filePanoJson.getName());
            try {
                objJson = new JSONObject(imageInfoJsonStr);
                JSONObject scenes1 = objJson.getJSONObject("scenes").getJSONObject("scene1");
                lat=scenes1.getDouble("lat");
                lon=scenes1.getDouble("lon");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        final Button buttonCancelSelectMapPoint = (Button)findViewById(R.id.buttonCancelSelectMapPoint);
        buttonCancelSelectMapPoint.setOnClickListener(v -> {
            finish();
        });

        final Button buttonSelectPoint = (Button)findViewById(R.id.buttonSelectPoint);
        buttonSelectPoint.setOnClickListener(v -> {
            osmMap.getSelectGps((double lat, double lon)->{
                try {
                    JSONObject scenes1 = objJson.getJSONObject("scenes").getJSONObject("scene1");
                    scenes1.put("lat",lat);
                    scenes1.put("lon",lon);
                    createTextFile(filePanoJson.getParentFile(),filePanoJson.getName(),objJson.toString(4));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                finish();
            });
        });

        final Button buttonDeletePoint = (Button)findViewById(R.id.buttonDeletePoint);
        buttonDeletePoint.setOnClickListener(v -> {
            try {
                JSONObject scenes1 = objJson.getJSONObject("scenes").getJSONObject("scene1");
                scenes1.put("lat",0);
                scenes1.put("lon",0);
                createTextFile(filePanoJson.getParentFile(),filePanoJson.getName(),objJson.toString(4));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            finish();
        });

        final Button buttonGetGps = (Button)findViewById(R.id.buttonGetGps);
        buttonGetGps.setOnClickListener(v -> {
            if (latGps!=0 && lonGps!=0) {
                osmMap.setSelectGps(latGps, lonGps);
            } else {
                Toast.makeText(this, "Положение не опредлелено", Toast.LENGTH_LONG).show();
            }
        });

        if (osmMap==null) {
            osmMap = new OsmMap(this);
        }
        osmMap.onViewMap(R.id.webView,lat,lon,19,null);
    }


    private String readTextFile(File directory, String fileName) {
        File file = new File(directory.getAbsolutePath()+"/"+fileName);
        StringBuilder content = new StringBuilder();
        if (!file.exists()) {
            Log.e(TAG, "readTextFile: File does not exist");
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "readTextFile: " + e.toString());
        }
        return content.toString();
    }

    /**
     * Функция создания тек4стового файла
     * @param directory - каталог создания
     * @param fileName - имя создаваемого файла
     * @param content - содержимое, которе помещается в файл
     */
    private  static void createTextFile(File directory, String fileName, String content) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            Log.e("Panorama", "createTextFile: " + e.toString());
        }
    }
}