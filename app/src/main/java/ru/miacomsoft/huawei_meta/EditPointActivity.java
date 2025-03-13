package ru.miacomsoft.huawei_meta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import ru.miacomsoft.huawei_meta.view_photo.Panorama;

public class EditPointActivity extends AppCompatActivity {

    private JSONObject hsJson;
    private JSONObject positionPoint;
    private String imgInfoPath;
    private String TAG = "SelectPointActivity";
    private String path_dir;
    private FileBrowser fileBrowser;
    private Panorama panorama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_point);
        try {
            Intent intent = getIntent();
            imgInfoPath = intent.getStringExtra("imgInfoPath");
            hsJson = new JSONObject(intent.getStringExtra("hsJsonStr"));
        } catch (JSONException e) {
            Log.e(TAG, "createEmptyInfoFileJson: " + e.toString());
        }

        Button buttonReturn = findViewById(R.id.buttonSaveImageInfo);
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ACTION","GOTO_HOT_SPOT");
                resultIntent.putExtra("HOT_SPOT", hsJson.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        Button buttonСancellation = findViewById(R.id.buttonCanselMenu);
        buttonСancellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ACTION","CANCELLATION");
                resultIntent.putExtra("HOT_SPOT", hsJson.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        Button buttonDeleteHotSpot = findViewById(R.id.buttonDeleteHotSpot);
        buttonDeleteHotSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ACTION","DELETE_HOT_SPOT");
                resultIntent.putExtra("HOT_SPOT", hsJson.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}