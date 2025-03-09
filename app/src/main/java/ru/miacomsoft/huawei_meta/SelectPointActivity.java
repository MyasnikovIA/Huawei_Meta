package ru.miacomsoft.huawei_meta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ru.miacomsoft.huawei_meta.view_photo.Panorama;

public class SelectPointActivity extends AppCompatActivity {

    private JSONObject imgInfojson;
    private JSONObject positionPoint;
    private String imgInfoPath;
    private String TAG = "SelectPointActivity";
    private String path_dir;
    private FileBrowser fileBrowser;
    private Panorama panorama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_point);
        try {
            Intent intent = getIntent();
            imgInfoPath = intent.getStringExtra("imgInfoPath");
            path_dir = intent.getStringExtra("path_dir");
            imgInfojson = new JSONObject(intent.getStringExtra("imgInfojsonStr"));
            positionPoint = new JSONObject(intent.getStringExtra("positionNewPoint"));
        } catch (JSONException e) {
            Log.e(TAG, "createEmptyInfoFileJson: " + e.toString());
        }
        fileBrowser = new FileBrowser(this);
        panorama = new Panorama(this);
        Button buttonReturn = findViewById(R.id.buttonSaveImageInfo);
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем Intent для возврата данных
                panorama.selectPointPano((JSONObject obj)->{
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("SELECT_PANO", obj.toString());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }
        });
        fileBrowser.getFileList(R.id.FileListView,R.id.editTextFilter,path_dir);
        fileBrowser.onClick((File file)->{
            panorama.getPhoto(R.id.webView,file);
        });

    }

}