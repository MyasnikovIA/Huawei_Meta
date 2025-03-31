package ru.miacomsoft.huawei_meta.photo360.libjs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ru.miacomsoft.huawei_meta.EditHotSpotMenuActivity;
import ru.miacomsoft.huawei_meta.AddHotSpotActivity;
import ru.miacomsoft.huawei_meta.photo360.Panorama;


public class PanoramaJs {
    public interface CallbackJSONObjectReturn {
        void call(JSONObject value);
    }
    private String TAG = "PanoramaJs";
    private WebView webView;
    private AppCompatActivity parentActivity;
    private CallbackJSONObjectReturn onClickHotSpotFun;
    private CallbackJSONObjectReturn onDblClickFun;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public PanoramaJs(AppCompatActivity activity, WebView webView) {
        this.webView = webView;
        parentActivity = activity;
    }

    public void setOnClickHotSpot(CallbackJSONObjectReturn onClickHotSpotFun){
        this.onClickHotSpotFun = onClickHotSpotFun;
    }

    public void setOnDblClick(CallbackJSONObjectReturn onDblClickFun){
        this.onDblClickFun = onDblClickFun;
    }

    @JavascriptInterface
    public void saveInfoJson(String imgInfoPath, String imgInfojsonStr) {
        try {
            JSONObject mgInfojson = new JSONObject(imgInfojsonStr);
            File imageInfoFile = new File(imgInfoPath);
            createTextFile(imageInfoFile.getParentFile(),imageInfoFile.getName(),mgInfojson.toString(4));
        } catch (JSONException e) {
            Log.e("console.log", "saveInfoJson error "+e.toString());
        }
    }


    @JavascriptInterface
    public String readInfoJson(String imgInfoPath) {
        File file = new File(imgInfoPath);
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
    private void createTextFile(File directory , String fileName, String content) {
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
    @JavascriptInterface
    public void onDblClick(String jsonStr) {
        JSONObject infojson = null;
        try {
            infojson = new JSONObject(jsonStr);
            if (onDblClickFun!=null) {
                onDblClickFun.call(infojson);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private int selectNewPointIndEdit = 0;
    @JavascriptInterface
    public void onClickHotSpot(String imgInfojsonStr) {
        selectNewPointIndEdit += 1;
        if (selectNewPointIndEdit == 2) {
            selectNewPointIndEdit = 0;
            return;
        }
        try {
            JSONObject infojson =  new JSONObject(imgInfojsonStr);
            if (onClickHotSpotFun !=null) {
                onClickHotSpotFun.call(infojson);
            }
//            String imgInfoPath = infojson.getString("imgInfoPath");
//            String path_dir = infojson.getString("path_dir");
//            infojson.remove("imgInfoPath");
//            infojson.remove("path_dir");
//            System.out.println(imgInfojsonStr);
//            Intent intent = new Intent(parentActivity.getApplicationContext(), EditPointActivity.class);
//            intent.putExtra("imgInfoPath", imgInfoPath); // Передаем строку
//            intent.putExtra("hsJsonStr", imgInfojsonStr.toString());    // Передаем число
//            parentActivity.startActivityForResult(intent, Panorama.REQUEST_CODE_EDIT);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
