package ru.miacomsoft.huawei_meta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import ru.miacomsoft.huawei_meta.view_map.OsmMap;
import ru.miacomsoft.huawei_meta.view_photo.Panorama;

public class MapViewPoints extends AppCompatActivity {
    private String TAG = "MapViewPoints.OsmMap";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_view_points);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Перевернуть ориентацию приложения
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String filePano = intent.getStringExtra("filePano");
        if (filePano != null) {
            filePanoFile = new File(filePano);
            filePanoJson = new File(filePanoFile.getParentFile().getAbsolutePath(), filePanoFile.getName().substring(0, filePanoFile.getName().length() - 4) + ".json");
            String imageInfoJsonStr = readTextFile(filePanoJson.getParentFile(), filePanoJson.getName());
            try {
                objJson = new JSONObject(imageInfoJsonStr);
                JSONObject scenes1 = objJson.getJSONObject("scenes").getJSONObject("scene1");
                lat = scenes1.getDouble("lat");
                lon = scenes1.getDouble("lon");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        final Button buttonCloseMaps = (Button)findViewById(R.id.buttonCloseMaps);
        buttonCloseMaps.setOnClickListener(v -> {
            finish();
        });

        final Button buttonGetGps = (Button)findViewById(R.id.buttonGetGps);
        buttonGetGps.setOnClickListener(v -> {
            if (latGps!=0 && lonGps!=0) {
                osmMap.setCenterGps(latGps, lonGps);
            } else {
                Toast.makeText(this, "Положение не опредлелено", Toast.LENGTH_LONG).show();
            }
        });


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


        if (osmMap==null) {
            osmMap = new OsmMap(this);
        }
        osmMap.onViewMap(R.id.webView,lat,lon,19);
        File[] files = filePanoFile.getParentFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("json")) {
                        // todo: дописать чтение файла и получение координат из json
                    }
                }
            }
        }
                
        // { lat: 53.37294643320362, lon: 83.69589294910432, name: "Маркер 3" }
       // osmMap.addPoint(53.37294643320362,83.69589294910432);
 //         const markeOne = L.marker([markerData.lat, markerData.lon]).addTo(map).bindPopup(`<b>${markerData.name}</b><br>Lat: ${markerData.lat}, Lon: ${markerData.lon}`).on('click', () => onMarkerClick(markerData));
    }

    @Override
    protected void onStop() {
        super.onStop();

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
}