package ru.miacomsoft.huawei_meta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ru.miacomsoft.huawei_meta.setup.SetupApp;
import ru.miacomsoft.huawei_meta.view_map.OsmMap;

public class MapViewPointsActivity extends AppCompatActivity {
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
            String imageInfoJsonStr = SetupApp.readTextFile(filePanoJson.getParentFile(), filePanoJson.getName());
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
        File[] files = filePanoFile.getParentFile().listFiles();
        JSONArray pointsJson = new JSONArray();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("json")) {
                        try {
                            JSONObject infoPanoLocal = new JSONObject(SetupApp.readTextFile(file.getParentFile(), file.getName()));
                            JSONObject infoPanoLocalOne = infoPanoLocal.getJSONObject("scenes").getJSONObject("scene1");
                            JSONObject row = new JSONObject();
                            row.put("name",infoPanoLocalOne.getString("panorama"));
                            row.put("dir_name",file.getParentFile());
                            row.put("file_name",file.getName());
                            row.put("lon",infoPanoLocalOne.getDouble("lon"));
                            row.put("lat",infoPanoLocalOne.getDouble("lat"));
                            pointsJson.put(row);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        osmMap.onViewMapArrayPoint(R.id.webView,lat,lon,19,pointsJson);
       // { lat: 53.37294643320362, lon: 83.69589294910432, name: "Маркер 3" }
       // osmMap.addPoint(53.37294643320362,83.69589294910432);
       // const markeOne = L.marker([markerData.lat, markerData.lon]).addTo(map).bindPopup(`<b>${markerData.name}</b><br>Lat: ${markerData.lat}, Lon: ${markerData.lon}`).on('click', () => onMarkerClick(markerData));
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}