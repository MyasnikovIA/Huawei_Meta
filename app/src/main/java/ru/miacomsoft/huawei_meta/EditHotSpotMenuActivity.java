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

import ru.miacomsoft.huawei_meta.file.FileBrowser;
import ru.miacomsoft.huawei_meta.photo360.Panorama;

public class EditHotSpotMenuActivity extends AppCompatActivity {
    public interface CallbackJsonReturn {
        void call(JSONObject jsonObj) throws JSONException;
    }
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
        setContentView(R.layout.activity_edit_point);
        try {
            Intent intent = getIntent();
            imgInfoPath = intent.getStringExtra("imgInfoPath");
            hsJson = new JSONObject(intent.getStringExtra("hsJsonStr"));
        } catch (JSONException e) {
            Log.e(TAG, "createEmptyInfoFileJson: " + e.toString());
        }

        Button buttonReturn = findViewById(R.id.buttonSelectLastGps);
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
    public  static void onActivityResult(AppCompatActivity appCompatActivity, int requestCode, int resultCode, @Nullable Intent data, CallbackJsonReturn callbackEmptyReturn) {
        if (requestCode == Panorama.REQUEST_CODE_EDIT && resultCode == RESULT_OK && data != null) {
            String hot_spot = data.getStringExtra("HOT_SPOT");
            if (hot_spot != null) {
                try {
                    if (!hot_spot.equals("{}")) {
                        String action = data.getStringExtra("ACTION");
                        JSONObject hotSpot = new JSONObject(hot_spot);
                        hotSpot.put("action",action);
                        if (action != null) {
                            if (callbackEmptyReturn!=null){
                                callbackEmptyReturn.call(hotSpot);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e("EditHotSpotMenuActivity", "createEmptyInfoFileJson: " + e.toString());
                }
            }
        }
    }
}