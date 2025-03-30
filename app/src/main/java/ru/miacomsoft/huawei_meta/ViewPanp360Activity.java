package ru.miacomsoft.huawei_meta;

import static ru.miacomsoft.huawei_meta.setup.SetupApp.readTextFile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ru.miacomsoft.huawei_meta.photo360.Panorama;

public class ViewPanp360Activity extends AppCompatActivity {


    private String TAG = "ViewPanp360";
    private JSONObject pointJson;
    private WebView webView;
    private Panorama panorama;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_panp360);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        try {
            Intent intent = getIntent();
            String jsonObj = intent.getStringExtra("jsonObjStr");
            pointJson = new JSONObject(jsonObj);
        } catch (JSONException e) {
            Log.e(TAG, "ViewPanp360.onCreate: " + e.toString());
        }
        panorama = new Panorama(this, R.id.webView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        try {
            File file = new File(pointJson.getString("dir_name"), pointJson.getString("name"));
            File fileJson = new File(pointJson.getString("dir_name"), pointJson.getString("file_name"));
            String imageInfoJsonStr = readTextFile(fileJson.getParentFile(), fileJson.getName());
            panorama.getPhoto(file,new JSONObject(imageInfoJsonStr));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}