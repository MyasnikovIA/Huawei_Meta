package ru.miacomsoft.huawei_meta.view_photo.libjs;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.miacomsoft.huawei_meta.SelectPointActivity;
import ru.miacomsoft.huawei_meta.view_photo.Panorama;


public class PanoramaJs {
    private String TAG = "PanoramaJs";
    private WebView webView;
    private AppCompatActivity parentActivity;
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public PanoramaJs(AppCompatActivity activity, WebView webView) {
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

    @JavascriptInterface
    public void selectNewPoint(String imgInfoPath, String imgInfojsonStr, String positionNewPoint,String path_dir) {
        Intent intent = new Intent(parentActivity.getApplicationContext(), SelectPointActivity.class);
        intent.putExtra("imgInfoPath", imgInfoPath); // Передаем строку
        intent.putExtra("imgInfojsonStr", imgInfojsonStr);    // Передаем число
        intent.putExtra("positionNewPoint", positionNewPoint);    // Передаем число
        intent.putExtra("path_dir", path_dir);    // Передаем число
        parentActivity.startActivityForResult(intent, Panorama.REQUEST_CODE);
    }


}
