package ru.miacomsoft.huawei_meta.view_photo.libjs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PanoramaJs {

    private String TAG = "PanoramaJs";
    private WebView webView;
    private Activity parentActivity;
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public PanoramaJs(Activity activity, WebView webView) {
        this.webView = webView;
        parentActivity = activity;
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
}
