package ru.miacomsoft.huawei_meta.view_photo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import ru.miacomsoft.huawei_meta.view_photo.lib.SqlLiteOrm;
import ru.miacomsoft.huawei_meta.view_photo.libjs.Android;
import ru.miacomsoft.huawei_meta.view_photo.libjs.Console;
import ru.miacomsoft.huawei_meta.view_photo.libjs.LocalStorage;
import ru.miacomsoft.huawei_meta.view_photo.libjs.PanoramaJs;

public class Panorama {
    public interface CallbackJSONObjectEmptyReturn {
        void call(JSONObject value);
    }
    public interface CallbackStringEmptyReturn {
        void call(String value);
    }

    private String jsResult = "";
    private CountDownLatch latch;
    public static final int REQUEST_CODE = 31001;
    private String TAG = "view_photo.Panorama";
    public WebView myWebView;
    private SqlLiteOrm sqlLiteORM;
    private AppCompatActivity appCompatActivity;
    private File filePano;
    private File fileInfo;
    private int webViewId;
    private JSONObject imageInfoJson;

    public Panorama(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }

    public void getPhoto(int webViewId, File file,JSONObject vars) {
        this.webViewId = webViewId;
        filePano = file;
        sqlLiteORM = new SqlLiteOrm(appCompatActivity);
        myWebView = (WebView) appCompatActivity.findViewById(webViewId);
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
        String imagePath = "file://" + file.getAbsolutePath();
        fileInfo = new File(file.getParentFile().getAbsolutePath(), file.getName().substring(0, file.getName().length() - 4) + ".json");
        if (!fileInfo.exists()) {
            createEmptyInfoFileJson(file);
        }
        String imageInfoJsonStr = readTextFile(fileInfo.getParentFile(), fileInfo.getName());
        try {
            imageInfoJson = new JSONObject(imageInfoJsonStr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // myWebView.loadUrl(imagePath);

        String from_pitch="";
        String from_yaw="";
        try {
            if (vars.has("from_pitch")) {
                from_pitch = vars.getString("from_pitch");
            }
            if (vars.has("from_yaw")) {
                from_yaw = vars.getString("from_yaw");
            }
        } catch (Exception e) {
            Log.e(TAG, "getPhoto vars: " + e.toString());
        }

        myWebView.loadUrl("file:///android_asset/pano2.html?img=" + fileInfo.getAbsolutePath() + "&width=" + myWebView.getWidth() + "&height=" + myWebView.getHeight() + "&json_info=" + imageInfoJsonStr + "&path_dir=" + file.getParentFile().getAbsolutePath()+"&from_pitch="+from_pitch+"&from_yaw="+from_yaw);
        StringBuffer sb = new StringBuffer();
        sb.append("javascript: ").append("local_file='").append("file://" + file.getAbsolutePath()).append("';");
        sb.append("path_dir = '" + file.getParentFile().getAbsolutePath() + "';");
        for (Iterator<String> it = vars.keys(); it.hasNext(); ) {
            try {
                String keyStr = it.next();
                Object keyvalue = vars.get(keyStr);
                sb.append(keyStr).append(" = '").append(keyvalue).append("';");
            } catch (Exception e) {
                Log.e(TAG, "getPhoto vars: " + e.toString());
            }
        }
        //sb.append("console.log('------'+path_dir);");
        myWebView.loadUrl(sb.toString());
    }
    public void setVar(String key, String value) {
        if (myWebView != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: ").append("").append(key).append("=").append("'").append(value).append("';");
            sb.append("console.log('------'+"+key+");");
            myWebView.loadUrl(sb.toString());
        }
    }

    public void setVar(String key, JSONObject value) {
        if (myWebView != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: ").append(key).append("=").append("").append(value.toString()).append(";");
            myWebView.loadUrl(sb.toString());
        }
    }

    public void getVar(String key,CallbackStringEmptyReturn callbackJSONObjectEmptyReturn) {
        if (myWebView != null) {
            myWebView.evaluateJavascript("(function() { return "+key+";})();", value -> {
                callbackJSONObjectEmptyReturn.call(value);
            });
        } else {
            callbackJSONObjectEmptyReturn.call(null);
        }
    }

    public void addHotSpot(JSONObject hotSpot) {
        try {
            String panoramaPathJpeg = hotSpot.getString("panorama").replace("\"","");
            File infoFilePath = new File(panoramaPathJpeg);
            imageInfoJson = new JSONObject(readTextFile(fileInfo.getParentFile(),fileInfo.getName()));
            JSONObject hotSpotNew = new JSONObject();
            hotSpotNew.put("title","");
            hotSpotNew.put("yaw", new BigDecimal(Double.valueOf(hotSpot.getString("from_yaw"))));
            hotSpotNew.put("pitch", new BigDecimal(Double.valueOf(hotSpot.getString("from_pitch"))));
            hotSpotNew.put("point_yaw", new BigDecimal(Double.valueOf(hotSpot.getString("yaw")))); // Куда смотреть после перехода на новую точку
            hotSpotNew.put("point_pitch", new BigDecimal(Double.valueOf(hotSpot.getString("pitch"))));// Куда смотреть после перехода на новую точку
            hotSpotNew.put("panorama_url", infoFilePath.getAbsolutePath()); // добавить путь к JSON файлу
            hotSpotNew.put("type", "scene");
            hotSpotNew.put("text_pint", "");
            hotSpotNew.put("sceneId", "scene_"+new Date().getTime());
            imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").getJSONArray("hotSpots").put(hotSpotNew);
            createTextFile(fileInfo.getParentFile(),fileInfo.getName(),imageInfoJson.toString(4));
            getPhoto(webViewId, filePano, new JSONObject());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void getSaveInfo() {
        if (myWebView==null) return;
        getVar("window.path_dir+'#'+sceneMain.getPitch()+'#'+sceneMain.getYaw()+'#'+imgInfoPath",(String value)-> {
            String[] valueArr = value.split("#");
            try {
                JSONObject loocAtJson = new JSONObject();
                loocAtJson.put("path_dir", valueArr[0]);
                loocAtJson.put("pitch", valueArr[1]);
                loocAtJson.put("yaw", valueArr[2]);
                loocAtJson.put("panorama", valueArr[3]);
                String panoramaPathJpeg = loocAtJson.getString("panorama").replace("\"","");
                File infoFilePath = new File(panoramaPathJpeg);
                imageInfoJson = new JSONObject(readTextFile(infoFilePath.getParentFile(),infoFilePath.getName()));
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("pitch",new BigDecimal(Double.valueOf(loocAtJson.getString("pitch"))));
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("yaw",new BigDecimal(Double.valueOf(loocAtJson.getString("yaw"))));
                createTextFile(infoFilePath.getParentFile(),infoFilePath.getName(),imageInfoJson.toString(4));
                getPhoto(webViewId, filePano, new JSONObject());
            } catch (Exception e) {
                Log.e(TAG, "buttonSaveImageInfo.onClick: " + e.toString());
            }
        });
//        StringBuffer sb = new StringBuffer();
//        sb.append("javascript: ").append("local_file='").append("file://"+filePano.getAbsolutePath()).append("';");
//        sb.append("imgInnfoJson.scenes.scene1.pitch = sceneMain.getPitch();");
//        sb.append("imgInnfoJson.scenes.scene1.yaw = sceneMain.getYaw();");
//        sb.append("panorama.saveInfoJson(imgInfoPath,JSON.stringify(imgInnfoJson));");
//        sb.append("console.log('imgInnfoJson--'+JSON.stringify(imgInnfoJson));");
//        myWebView.loadUrl(sb.toString());
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
    private void createTextFile(File directory, String fileName, String content) {
        // Создаем директорию, если она не существует
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Создаем файл
        File file = new File(directory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Записываем содержимое в файл
            writer.write(content);
        } catch (IOException e) {
            Log.e(TAG, "createTextFile: " + e.toString());
        }
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

}
