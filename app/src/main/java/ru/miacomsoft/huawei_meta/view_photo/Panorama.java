package ru.miacomsoft.huawei_meta.view_photo;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ru.miacomsoft.huawei_meta.view_photo.lib.SqlLiteOrm;
import ru.miacomsoft.huawei_meta.view_photo.libjs.Android;
import ru.miacomsoft.huawei_meta.view_photo.libjs.Console;
import ru.miacomsoft.huawei_meta.view_photo.libjs.LocalStorage;
import ru.miacomsoft.huawei_meta.view_photo.libjs.PanoramaJs;

public class Panorama {
    private String TAG="view_photo.Panorama";
    private WebView myWebView;
    private SqlLiteOrm sqlLiteORM;
    private AppCompatActivity appCompatActivity;
    private File filePano;
    public Panorama(AppCompatActivity appCompatActivity){
        this.appCompatActivity  = appCompatActivity;
    }

    public void getPhoto(int WebViewId , File file) {
        filePano = file;
        sqlLiteORM = new SqlLiteOrm(appCompatActivity);
        myWebView = (WebView) appCompatActivity.findViewById(WebViewId);
        myWebView.clearCache(true);
        myWebView.clearHistory();
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(false);
        settings.setBuiltInZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(false);
        myWebView.addJavascriptInterface(new Android(appCompatActivity, myWebView, sqlLiteORM), "android");
        myWebView.addJavascriptInterface(new Console(appCompatActivity, myWebView, sqlLiteORM), "console");
        myWebView.addJavascriptInterface(new PanoramaJs(appCompatActivity, myWebView), "panorama");
        try {
            myWebView.addJavascriptInterface(new LocalStorage(appCompatActivity, myWebView, sqlLiteORM), "localStorage");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // String imagePath = "file:///storage/emulated/0/DCIM/PANORAMA_HUAWEI/Camera/img.jpg";
        String imagePath = "file://"+file.getAbsolutePath();
        File fileInfo = new File(file.getParentFile().getAbsolutePath(),file.getName().substring(0,file.getName().length()-4)+".json");
        if (!fileInfo.exists()) {
            createEmptyInfoFileJson(file);
        }
        String imageInfoJsonStr = readTextFile(fileInfo.getParentFile(), fileInfo.getName());
        // myWebView.loadUrl(imagePath);
        myWebView.loadUrl("file:///android_asset/pano2.html?img="+fileInfo.getAbsolutePath()+"&width="+myWebView.getWidth()+"&height="+myWebView.getHeight()+"&json_info="+imageInfoJsonStr);
        StringBuffer sb = new StringBuffer();
        sb.append("javascript: ").append("local_file='").append("file://"+file.getAbsolutePath()).append("';");
        sb.append("console.log('------'+local_file);");
        myWebView.loadUrl(sb.toString());
    }

    public void getSaveInfo() {
        if (myWebView==null) return;
        StringBuffer sb = new StringBuffer();
        sb.append("javascript: ").append("local_file='").append("file://"+filePano.getAbsolutePath()).append("';");
        sb.append("imgInnfoJson.scenes.scene1.pitch = sceneMain.getPitch();");
        sb.append("imgInnfoJson.scenes.scene1.yaw = sceneMain.getYaw();");
        sb.append("panorama.saveInfoJson(imgInfoPath,JSON.stringify(imgInnfoJson));");
        sb.append("console.log('imgInnfoJson--'+JSON.stringify(imgInnfoJson));");
        myWebView.loadUrl(sb.toString());

    }

    /**
     * Процедура создания пустого информационного файла JSON
     * @param fileImg - файл изображения, для которого необходимо создать текстовой файл описания в формате JSON
     */
    private void createEmptyInfoFileJson(File fileImg) {
        String fileName = fileImg.getAbsolutePath();
        try {
            String name = fileName.substring(fileName.lastIndexOf("/")+1, fileName.lastIndexOf("."));
            JSONObject scen = new JSONObject();
            scen.put("default", new JSONObject("{\"firstScene\": \"scene1\"}"));
            scen.put("hotSpotDebug", false);
            scen.put("hotPointDebug", true);
            scen.put("sceneFadeDuration", 1000);
            JSONObject scene1 = new JSONObject();
            scene1.put("hotSpots", new JSONArray());
            scene1.put("panorama", fileName);
            scene1.put("autoLoad", true);
            scene1.put("crossOrigin", "use-credentials");
            scene1.put("lon", 0);
            scene1.put("lat", 0);
            scene1.put("orient_azimuth", 0);
            scene1.put("orient_roll", 0);
            scene1.put("orient_pitch", 0);
            scene1.put("title", "title:" + name);
            scene1.put("pitch", -3.5001450183561142);
            scene1.put("yaw", 172.69391364740358);
            JSONObject scene = new JSONObject();
            scene.put("scene1", scene1);
            scen.put("scenes", scene);
            createTextFile(fileImg.getParentFile(),name + ".json", scen.toString(4));
        } catch (JSONException e) {
            Log.e(TAG, "createEmptyInfoFileJson: " + e.toString());
        }
    }

    /**
     * Функция создания тек4стового файла
     * @param directory - каталог создания
     * @param fileName - имя создаваемого файла
     * @param content - содержимое, которе помещается в файл
     */
    private void createTextFile( File directory , String fileName, String content) {
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

    private String readTextFile(File directory, String fileName) {
        File file = new File(directory, fileName);
        StringBuilder content = new StringBuilder();

        if (!file.exists()) {
            Log.e(TAG, "readTextFile: File does not exist");
            return null; // Возвращаем null, если файла нет
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // Добавляем новую строку после каждого прочитанного фрагмента
            }
        } catch (IOException e) {
            Log.e(TAG, "readTextFile: " + e.toString());
        }

        return content.toString(); // Возвращаем содержимое файла в виде строки
    }

}
