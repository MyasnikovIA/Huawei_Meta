package ru.miacomsoft.huawei_meta;

import static ru.miacomsoft.huawei_meta.gps_location.GpsManager.initGps;
import static ru.miacomsoft.huawei_meta.setup.SetupApp.LOCAL_STORAGE_APP;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

public class MapViewActivity extends AppCompatActivity {
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
    private boolean isExistGpsDevice=false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        initGps(this,(double latLoc, double lonLoc)->{
            latGps = latLoc;
            lonGps = lonLoc;
        });
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
                if (scenes1.has("lat")) {
                    lat = scenes1.getDouble("lat");
                }
                if (scenes1.has("lon")) {
                    lon = scenes1.getDouble("lon");
                }
                if (lat!=0) LOCAL_STORAGE_APP.put("last_lat",lat);
                if (lon!=0) LOCAL_STORAGE_APP.put("last_lon",lon);
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
                    if (lat!=0) LOCAL_STORAGE_APP.put("last_lat",lat);
                    if (lon!=0) LOCAL_STORAGE_APP.put("last_lon",lon);
                    createTextFile(filePanoJson.getParentFile(),filePanoJson.getName(),objJson.toString(4));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                finish();
            });
        });

        final Button buttonDeletePoint = (Button)findViewById(R.id.buttonSelectLastGps);
        buttonDeletePoint.setOnClickListener(v -> {
            if (LOCAL_STORAGE_APP.has("last_lat") && LOCAL_STORAGE_APP.has("last_lon") ) {
                try {
                    osmMap.setSelectGps(LOCAL_STORAGE_APP.getDouble("last_lat"), LOCAL_STORAGE_APP.getDouble("last_lon"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            // Выбрать предыдущую точку GPS
            //            try {
            //                JSONObject scenes1 = objJson.getJSONObject("scenes").getJSONObject("scene1");
            //                scenes1.put("lat",0);
            //                scenes1.put("lon",0);
            //                createTextFile(filePanoJson.getParentFile(),filePanoJson.getName(),objJson.toString(4));
            //            } catch (JSONException e) {
            //                throw new RuntimeException(e);
            //            }
           // finish();
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