package ru.miacomsoft.huawei_meta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ru.miacomsoft.huawei_meta.file.FileBrowser;
import ru.miacomsoft.huawei_meta.photo360.Panorama;

public class AddHotSpotActivity extends AppCompatActivity {
    public interface CallbackJsonReturn {
        void call(JSONObject jsonObj);
    }
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
        setContentView(R.layout.activity_select_hotspot);
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
        panorama = new Panorama(this, R.id.webView);
        Button buttonReturn = findViewById(R.id.buttonSaveImageInfo);
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panorama.getVar("window.path_dir+'#'+sceneMain.getPitch()+'#'+sceneMain.getYaw()+'#'+from_pitch+'#'+from_yaw+'#'+imgInfoPath",(String value)-> {
                    String[] valueArr = value.split("#");
                    try {
                        JSONObject loocAtJson = new JSONObject();
                        loocAtJson.put("path_dir", valueArr[0]);
                        loocAtJson.put("pitch", valueArr[1]);
                        loocAtJson.put("yaw", valueArr[2]);
                        loocAtJson.put("from_pitch", valueArr[3]);
                        loocAtJson.put("from_yaw", valueArr[4]);
                        loocAtJson.put("panorama", valueArr[5]);
                        // Создаем Intent для возврата данных
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("SELECT_PANO", loocAtJson.toString());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "buttonSaveImageInfo.onClick: " + e.toString());
                    }
                });
            }
        });
        try {
            JSONObject scenes1 = imgInfojson.getJSONObject("scenes").getJSONObject("scene1");
            String panorama = scenes1.getString("panorama");
            fileBrowser.getFileList(R.id.FileListView,R.id.editTextSearch,path_dir,new File(panorama).getName());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        fileBrowser.onClick((File file)->{
            JSONObject vars = new JSONObject();
            try {
                if (positionPoint.has("yaw")) {
                    vars.put("from_yaw", positionPoint.getString("yaw"));
                }
                if (positionPoint.has("pitch")) {
                    vars.put("from_pitch", positionPoint.getString("pitch"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "FileListView.onClick: " + e.toString());
            }
            panorama.getPhoto(file,vars);
        });

    }

    public  static void onActivityResult(AppCompatActivity appCompatActivity, int requestCode, int resultCode, @Nullable Intent data, CallbackJsonReturn callbackEmptyReturn) {
        if (requestCode == Panorama.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectPanoString = data.getStringExtra("SELECT_PANO");
            if (selectPanoString != null) {
                try {
                    if (!selectPanoString.equals("{}")) {
                        if (callbackEmptyReturn!=null){
                            //    panorama.addHotSpot(new JSONObject(selectPanoString));
                            callbackEmptyReturn.call(new JSONObject(selectPanoString));
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}