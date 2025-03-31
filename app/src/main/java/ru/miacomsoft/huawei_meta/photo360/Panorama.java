package ru.miacomsoft.huawei_meta.photo360;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import ru.miacomsoft.huawei_meta.R;
import ru.miacomsoft.huawei_meta.file.JpegMetaInfo;
import ru.miacomsoft.huawei_meta.view_map.OsmMap;
import ru.miacomsoft.huawei_meta.photo360.lib.SqlLiteOrm;
import ru.miacomsoft.huawei_meta.photo360.libjs.Android;
import ru.miacomsoft.huawei_meta.photo360.libjs.Console;
import ru.miacomsoft.huawei_meta.photo360.libjs.LocalStorage;
import ru.miacomsoft.huawei_meta.photo360.libjs.PanoramaJs;

public class Panorama {
    public interface CallbackJSONObjectEmptyReturn {
        void call(JSONObject value);
    }
    public interface CallbackStringEmptyReturn {
        void call(String value);
    }
    public interface CallbackEmptyReturn {
        void call();
    }


    private String jsResult = "";
    private CountDownLatch latch;
    public static final int REQUEST_CODE = 31001;
    public static final int REQUEST_CODE_EDIT = 31002;
    private String TAG = "view_photo.Panorama";
    public WebView myWebView;
    private SqlLiteOrm sqlLiteORM;
    private AppCompatActivity appCompatActivity;
    private File filePano;
    private File dirPano;
    private File fileInfo;
    private int webViewId;
    private JSONObject imageInfoJson;
    private CallbackEmptyReturn callbackAfterDeletePanorama;
    private PanoramaJs panoramaJs;

    public Panorama(AppCompatActivity appCompatActivity, int R_id_webView) {
        this.appCompatActivity = appCompatActivity;
        this.webViewId = R_id_webView;
        myWebView = (WebView) appCompatActivity.findViewById(webViewId);
        panoramaJs = new PanoramaJs(appCompatActivity, myWebView);
    }

    public File getFilePano() {
        return filePano;
    }

    public void getPhoto( File file,JSONObject vars) {
        filePano = file;
        dirPano = filePano.getParentFile();
        sqlLiteORM = new SqlLiteOrm(appCompatActivity);
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
        myWebView.addJavascriptInterface(panoramaJs, "panorama");

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
            JSONObject scenes1 = imageInfoJson.getJSONObject("scenes").getJSONObject("scene1");
            String panorama = scenes1.getString("panorama");
            if (panorama.indexOf("/")==-1) {
                File panoramaFile = new File(filePano.getParentFile(),panorama);
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("panorama", "file://"+panoramaFile.getAbsolutePath());
            }
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
        String urlStr = "file:///android_asset/pano2.html?img=" + fileInfo.getAbsolutePath() + "&width=" + myWebView.getWidth() + "&height=" + myWebView.getHeight() + "&json_info=" + imageInfoJson.toString() + "&path_dir=" + file.getParentFile().getAbsolutePath()+"&from_pitch="+from_pitch+"&from_yaw="+from_yaw;
        loadPage(urlStr, ()->{
            //sb.append("console.log('------'+path_dir);");
            //myWebView.loadUrl(sb.toString());
        });
//            StringBuffer sb = new StringBuffer();
//            sb.append("javascript: ").append("local_file='").append("file://" + file.getAbsolutePath()).append("';");
//            //sb.append("path_dir = '" + file.getParentFile().getAbsolutePath() + "';");
//            myWebView.loadUrl(sb.toString());
        for (Iterator<String> it = vars.keys(); it.hasNext(); ) {
            try {
                String keyStr = it.next();
                Object keyvalue = vars.get(keyStr);
                StringBuffer sb = new StringBuffer();
                sb.append("javascript: ");
                sb.append(keyStr).append(" = '").append(keyvalue).append("';");
                myWebView.loadUrl(sb.toString());
            } catch (Exception e) {
                Log.e(TAG, "getPhoto vars: " + e.toString());
            }
        }

    }

    public void setOnClickHotSpot(PanoramaJs.CallbackJSONObjectReturn onClickHotSpotFun){
        panoramaJs.setOnClickHotSpot(onClickHotSpotFun);
    }

    public void setOnDblClick(PanoramaJs.CallbackJSONObjectReturn onDblClickFun){
        panoramaJs.setOnDblClick(onDblClickFun);
    }
    private long lastTouchTime = 0;
    private float lastTouchX;
    private float lastTouchY;
    private void loadPage(String urlPage, OsmMap.CallbackEmptyReturn callbackEmptyReturn) {
        myWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress==100){
                    if (callbackEmptyReturn!=null) {
                        callbackEmptyReturn.call();
                    }
                }
            }
        });
        myWebView.loadUrl(urlPage);
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

    public void deleteHotSpot(JSONObject hotSpot) {
        if (myWebView == null || hotSpot==null) return;
        try {
            File infoFilePath = new File(hotSpot.getString("panorama_url_from"));
            imageInfoJson = new JSONObject(readTextFile(infoFilePath.getParentFile(),infoFilePath.getName()));
            for (int i=0; i < imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").getJSONArray("hotSpots").length(); i++) {
                JSONObject hotSpotOne  = imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").getJSONArray("hotSpots").getJSONObject(i);
                if (hotSpotOne.getString("sceneId").equals(hotSpot.getString("sceneId"))) {
                    imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").getJSONArray("hotSpots").remove(i);
                }
            }
            createTextFile(fileInfo.getParentFile(),fileInfo.getName(),imageInfoJson.toString(4));
            getPhoto(filePano, new JSONObject());
            // {"title":"","yaw":-53.236924148647866,"pitch":-12.381230171877444,"point_yaw":115.1247005243223,"point_pitch":49.372493628283046,
            // "panorama_url":"\/storage\/emulated\/0\/DCIM\/CV60\/PIC_20250307_221549.json",
            // "type":"scene",
            // "text_pint":"",
            // "sceneId":"scene_1741646190151","div":{"clicked":true}}
        } catch (Exception e) {
            Log.e(TAG, "Panorama.gotoHotSpot: " + e.toString());
        }
    }

    public void reloadPanorama(JSONObject hotSpot) {
        if (myWebView == null || hotSpot==null) return;
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: ");
            sb.append("mgInnfoJson = JSON.parse(panorama.readInfoJson('" + hotSpot.getString("panorama_url_from") + "'));");
            // sb.append("imgInnfoJson.onDblClick = onDblClickScene;");
            sb.append("imgInnfoJson.onClickHotSpot = onClickHotSpot;");
            sb.append("if (sceneMain?.destroy) { sceneMain.destroy(); }");
            sb.append("sceneMain = pannellum.viewer('panorama', imgInnfoJson);");
            sb.append("sceneMain.setPitch("+hotSpot.getString("pitch")+");");
            sb.append("sceneMain.setYaw("+hotSpot.getString("yaw")+");");
            myWebView.loadUrl(sb.toString());
        }catch (Exception e) {
            Log.e(TAG, "Panorama.gotoHotSpot: " + e.toString());
        }
    }

    public void gotoHotSpot(JSONObject hotSpot) {
        if (myWebView == null || hotSpot==null) return;
        try {
            imageInfoJson = new JSONObject(readTextFile(dirPano.getAbsoluteFile(), hotSpot.getString("panorama_url")));
            JSONObject scenes1 = imageInfoJson.getJSONObject("scenes").getJSONObject("scene1");
            String panorama = scenes1.getString("panorama");
            if (panorama.indexOf("/")==-1) {
                File panoramaFile = new File(filePano.getParentFile(),panorama);
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("panorama", "file://"+panoramaFile.getAbsolutePath());
            }
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: ");
            sb.append("imgInnfoJson = ").append(imageInfoJson.toString()).append(";");
            // sb.append("imgInnfoJson.onDblClick = onDblClickScene;");
            sb.append("imgInnfoJson.onClickHotSpot = onClickHotSpot;");
            sb.append("if (sceneMain?.destroy) { sceneMain.destroy(); }");
            sb.append("sceneMain = pannellum.viewer('panorama', imgInnfoJson);");
            sb.append("sceneMain.setPitch(").append(hotSpot.getString("point_pitch")).append(");");
            sb.append("sceneMain.setYaw(").append(hotSpot.getString("point_yaw")).append(");");
            myWebView.loadUrl(sb.toString());
        }catch (Exception e) {
            Log.e(TAG, "Panorama.gotoHotSpot: " + e.toString());
        }
    }

    public void addHotSpot(JSONObject hotSpot) {
        try {
            String panoramaPathJpeg = hotSpot.getString("panorama").replace("\"","");
            panoramaPathJpeg = panoramaPathJpeg.substring(panoramaPathJpeg.lastIndexOf("/") + 1);
            File infoFilePath = new File(panoramaPathJpeg);
            imageInfoJson = new JSONObject(readTextFile(fileInfo.getParentFile(),fileInfo.getName()));
            JSONObject hotSpotNew = new JSONObject();
            hotSpotNew.put("title","");
            hotSpotNew.put("yaw", new BigDecimal(Double.valueOf(hotSpot.getString("from_yaw"))));
            hotSpotNew.put("pitch", new BigDecimal(Double.valueOf(hotSpot.getString("from_pitch"))));
            hotSpotNew.put("point_yaw", new BigDecimal(Double.valueOf(hotSpot.getString("yaw")))); // Куда смотреть после перехода на новую точку
            hotSpotNew.put("point_pitch", new BigDecimal(Double.valueOf(hotSpot.getString("pitch"))));// Куда смотреть после перехода на новую точку
            hotSpotNew.put("panorama_url", infoFilePath.getName());
            hotSpotNew.put("type", "scene");
            hotSpotNew.put("text_pint", "");
            hotSpotNew.put("sceneId", "scene_"+new Date().getTime());
            imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").getJSONArray("hotSpots").put(hotSpotNew);
            createTextFile(fileInfo.getParentFile(),fileInfo.getName(),imageInfoJson.toString(4));
            getPhoto(filePano, new JSONObject());
        } catch (JSONException e) {
            Log.e(TAG, "Panorama.addHotSpot: " + e.toString());
        }
    }

    public void deletePanorama(CallbackEmptyReturn callbackAfterDeletePano) {
        this.callbackAfterDeletePanorama = callbackAfterDeletePano;
        if (myWebView==null) return;
        showConfirmDialog(appCompatActivity, "Подтверждение", "Вы уверены, что хотите выполнить это действие?", new ConfirmDialogListener() {
            @Override
            public void onConfirm(boolean isConfirmed) {
                if (isConfirmed) {
                    getVar("window.path_dir+'#'+sceneMain.getPitch()+'#'+sceneMain.getYaw()+'#'+imgInfoPath",(String value)-> {
                        String[] valueArr = value.split("#");
                        String imgInfoPathStr = valueArr[3].replaceAll("\"","");
                        File panoJson = new File(imgInfoPathStr);
                        File panoDirImage = panoJson.getParentFile();
                        String nameJsonFile = imgInfoPathStr.substring(imgInfoPathStr.lastIndexOf("/")+1, imgInfoPathStr.lastIndexOf("."));
                        File panoImage = new File(imgInfoPathStr.substring(0,imgInfoPathStr.lastIndexOf("."))+".jpg");
                        // Просканировать каталог и последовательно вычитывая файлы описания JSON удалять все ссылки на удаляемую панораму
                        removeLinkFiles(panoDirImage,panoJson.getName().toLowerCase().replaceAll("\"",""));
                        //Удалить JSON описание панорамы
                        if (panoJson.exists()) {
                            panoJson.delete();
                        }
                        // Удалить изображение панорамы
                        if (panoImage.exists()) {
                            panoImage.delete();
                        }
                        if (callbackAfterDeletePanorama!=null) {
                            callbackAfterDeletePanorama.call();
                            callbackAfterDeletePanorama = null;
                        }
                    });
                }
            }
        });
    }

    CallbackEmptyReturn callbackAfterRenamePanorama;
    public void renamePanorama(CallbackEmptyReturn callbackAfterRenamePano) {
        if (myWebView==null) return;
        this.callbackAfterRenamePanorama = callbackAfterRenamePano;
        getVar("window.path_dir+'#'+sceneMain.getPitch()+'#'+sceneMain.getYaw()+'#'+imgInfoPath",(String value)-> {
            String[] valueArr = value.split("#");
            String imgInfoPathStr = valueArr[3].replaceAll("\"","");
            File panoJson = new File(imgInfoPathStr);
            File panoDirImage = panoJson.getParentFile();
            String nameJsonFile = imgInfoPathStr.substring(imgInfoPathStr.lastIndexOf("/")+1, imgInfoPathStr.lastIndexOf("."));
            File panoImage = new File(imgInfoPathStr.substring(0,imgInfoPathStr.lastIndexOf("."))+".jpg");
            showPromptDialog(appCompatActivity, "Переименование панорамы",nameJsonFile, "Введите новое имя:", new PromptDialogListener() {
                @Override
                public void onResult(String input) {
                    if (input != null && !input.isEmpty()) {
                        String tmpPath = panoDirImage.getAbsolutePath()+"/"+input;
                        File panoImageNew = new File(tmpPath+".jpg");
                        File panoJsonNew = new File(tmpPath+".json");
                        if (panoJson.renameTo(panoJsonNew)) { //  переименовать JSON файл
                            if (panoImage.renameTo(panoImageNew)) { // переименовать jpg файл
                                // Изменить имя ссылки на новый файл в других JSON в этом каталоге
                                renameLinkFiles(panoDirImage,panoJsonNew.getName(),panoJson.getName());
                                try {
                                    // Изменить ссылку на изображение панорамы в panoJsonNew scenes.scene1.panorama
                                    imageInfoJson = new JSONObject(readTextFile(panoJsonNew.getParentFile(), panoJsonNew.getName()));
                                    imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("panorama", panoImageNew.getName().substring(0,panoImageNew.getName().lastIndexOf("."))+".jpg");
                                    imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("title", "title:"+panoImageNew.getName().substring(0,panoImageNew.getName().lastIndexOf(".")));
                                    createTextFile(panoJsonNew.getParentFile(), panoJsonNew.getName(), imageInfoJson.toString(4));
                                    getPhoto(panoImageNew,new JSONObject());
                                } catch (Exception e){
                                    Log.e(TAG, "Panorama.renamePanorama: " + e.toString());
                                }
                            }
                        }
                        if (callbackAfterRenamePanorama!=null) {
                            callbackAfterRenamePanorama.call();
                            callbackAfterRenamePanorama = null;
                        }
                    }
                }
            });
        });
    }

    public  void removeLinkFiles(File directory, String targetFileName) {
        if (!directory.isDirectory()) {
            System.out.println("Указанный путь не является каталогом.");
            return;
        }

        // Получаем список файлов с расширением .json
        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("В каталоге нет JSON-файлов.");
            return;
        }

        // Обрабатываем каждый JSON-файл
        for (File jsonFile : jsonFiles) {
            try {
                JSONObject jsonObject = new JSONObject(readTextFile(jsonFile.getParentFile(), jsonFile.getName()));
                // Проверяем и удаляем объекты с targetFileName в panorama_url
                boolean isModified = removeHotSpotWithPanoramaUrl(jsonObject, targetFileName);
                // Если файл был изменен, перезаписываем его
                if (isModified) {
                    FileWriter fileWriter = new FileWriter(jsonFile);
                    fileWriter.write(jsonObject.toString(4)); // 4 - отступ для красивого форматирования
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Panorama.processJsonFiles Ошибка при обработке файла: " + e.toString());
            }
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
                loocAtJson.put("panorama", valueArr[3].substring(valueArr[3].lastIndexOf("/")+1));
                String panoramaPathJson =  loocAtJson.getString("path_dir").replace("\"","") + "/" + loocAtJson.getString("panorama").replace("\"","");
                File infoFilePath = new File(panoramaPathJson);
                imageInfoJson = new JSONObject(readTextFile(infoFilePath.getParentFile(), infoFilePath.getName()));
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("pitch",new BigDecimal(Double.valueOf(loocAtJson.getString("pitch"))));
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("yaw",new BigDecimal(Double.valueOf(loocAtJson.getString("yaw"))));
                createTextFile(infoFilePath.getParentFile(),infoFilePath.getName(),imageInfoJson.toString(4));
                getPhoto(filePano, new JSONObject());
            } catch (Exception e) {
                Log.e(TAG, "Panorama.getSaveInfo: " + e.toString());
            }
        });
    }


    /**
     * Процедура создания пустого информационного файла JSON
     * @param fileImg - файл изображения, для которого необходимо создать текстовой файл описания в формате JSON
     */
    public static void createEmptyInfoFileJson(File fileImg) {
        String fileName = fileImg.getAbsolutePath();
        try {
            String name = fileName.substring(fileName.lastIndexOf("/")+1, fileName.lastIndexOf("."));
            JSONObject scen = new JSONObject();
            JSONObject imageInfoJson = JpegMetaInfo.readJsonFromXmp(fileImg);
            if (imageInfoJson!=null) {
                scen.put("default", new JSONObject("{\"firstScene\": \"scene1\"}"));
                scen.put("hotSpotDebug", false);
                scen.put("hotPointDebug", true);
                scen.put("sceneFadeDuration", 1000);
                JSONObject scene1 = new JSONObject();
                scene1.put("hotSpots", new JSONArray());
                scene1.put("panorama", imageInfoJson.getString("panorama"));
                scene1.put("autoLoad", true);
                scene1.put("crossOrigin", "use-credentials");
                scene1.put("lon",  imageInfoJson.getDouble("lon"));
                scene1.put("lat",  imageInfoJson.getDouble("lat"));
                scene1.put("orient_azimuth",  imageInfoJson.getDouble("orient_azimuth"));
                scene1.put("orient_roll",  imageInfoJson.getDouble("orient_roll"));
                scene1.put("orient_pitch",  imageInfoJson.getDouble("orient_pitch"));
                scene1.put("title", imageInfoJson.getString("title"));
                scene1.put("pitch", imageInfoJson.getDouble("pitch"));
                scene1.put("yaw",  imageInfoJson.getDouble("yaw"));
                JSONObject scene = new JSONObject();
                scene.put("scene1", scene1);
                scen.put("scenes", scene);
            } else {
                scen = new JSONObject();
                scen.put("default", new JSONObject("{\"firstScene\": \"scene1\"}"));
                scen.put("hotSpotDebug", false);
                scen.put("hotPointDebug", true);
                scen.put("sceneFadeDuration", 1000);
                JSONObject scene1 = new JSONObject();
                scene1.put("hotSpots", new JSONArray());
                scene1.put("panorama", fileImg.getName());
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
            }
            createTextFile(fileImg.getParentFile(),name + ".json", scen.toString(4));
        } catch (JSONException e) {
            Log.e("Panorama", "createEmptyInfoFileJson: " + e.toString());
        }
    }
    /**
     * Функция создания тек4стового файла
     * @param directory - каталог создания
     * @param fileName - имя создаваемого файла
     * @param content - содержимое, которе помещается в файл
     */
    private  static void createTextFile(File directory, String fileName, String content) {
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
            Log.e("Panorama", "createTextFile: " + e.toString());
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

    public interface ConfirmDialogListener {
        void onConfirm(boolean isConfirmed);
    }

    public static void showConfirmDialog(Context context, String title, String message, final ConfirmDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirm(true);
                }
            }
        });

        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirm(false);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface PromptDialogListener {
        void onResult(String input);
    }


    public static void showPromptDialog(Context context, String title,String text, String message, final PromptDialogListener listener) {
        // Создаем EditText для ввода текста
        final EditText input = new EditText(context);
        input.setHint("Новое имя файла");
        input.setText(text);
        // Создаем AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setView(input); // Добавляем EditText в диалог
        // Кнопка "ОК"
        builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString();
                if (listener != null) {
                    listener.onResult(userInput); // Передаем введенный текст в listener
                }
            }
        });

        // Кнопка "Отмена"
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Закрываем диалог
            }
        });

        // Показываем диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void renameLinkFiles(File directory, String targetFileName, String newFileName) {
        if (!directory.isDirectory()) {
            System.out.println("Указанный путь не является каталогом.");
            return;
        }

        // Получаем список файлов с расширением .json
        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("В каталоге нет JSON-файлов.");
            return;
        }
        for (File jsonFile : jsonFiles) {
            try {
                JSONObject jsonObject = new JSONObject(readTextFile(jsonFile.getParentFile(), jsonFile.getName()));
                boolean isModified = renameHotSpotWithPanoramaUrl(jsonObject, targetFileName, newFileName);
                if (isModified) {
                    FileWriter fileWriter = new FileWriter(jsonFile);
                    fileWriter.write(jsonObject.toString(4)); // 4 - отступ для красивого форматирования
                    fileWriter.flush();
                    fileWriter.close();
                    System.out.println("Файл изменен и перезаписан: " + jsonFile.getName());
                } else {
                    System.out.println("Файл не изменен: " + jsonFile.getName());
                }
            } catch (Exception e) {
                System.out.println("Ошибка при обработке файла: " + jsonFile.getName());
                e.printStackTrace();
            }
        }
    }


    // Метод для переименования panorama_url
    private boolean renameHotSpotWithPanoramaUrl(JSONObject jsonObject, String targetFileName, String newFileName) {
        try {
            // Получаем массив hotSpots
            JSONArray hotSpots = jsonObject.getJSONObject("scenes")
                    .getJSONObject("scene1")
                    .getJSONArray("hotSpots");

            boolean isModified = false;

            // Перебираем массив hotSpots
            for (int i = 0; i < hotSpots.length(); i++) {
                JSONObject hotSpot = hotSpots.getJSONObject(i);
                String panoramaUrl = hotSpot.optString("panorama_url", "");

                // Если panoramaUrl совпадает с targetFileName, переименовываем
                if (panoramaUrl.equals(targetFileName)) {
                    hotSpot.put("panorama_url", newFileName);
                    isModified = true; // Файл был изменен
                }
            }

            // Возвращаем true, если были изменены элементы
            return isModified;
        } catch (Exception e) {
            System.out.println("Ошибка при обработке JSON-структуры.");
            e.printStackTrace();
            return false;
        }
    }
    // Метод для удаления объектов с targetFileName в panorama_url
    private boolean removeHotSpotWithPanoramaUrl(JSONObject jsonObject, String targetFileName) {
        try {
            // Получаем массив hotSpots
            JSONArray hotSpots = jsonObject.getJSONObject("scenes").getJSONObject("scene1").getJSONArray("hotSpots");
            // Создаем список для хранения индексов элементов, которые нужно удалить
            List<Integer> indicesToRemove = new ArrayList<>();
            // Перебираем массив hotSpots
            for (int i = 0; i < hotSpots.length(); i++) {
                JSONObject hotSpot = hotSpots.getJSONObject(i);
                String panoramaUrl = hotSpot.optString("panorama_url", "").toLowerCase();
                // Если panoramaUrl совпадает с targetFileName, добавляем индекс в список
                if (panoramaUrl.equals(targetFileName)) {
                    indicesToRemove.add(i);
                }
            }
            // Удаляем элементы с конца, чтобы не нарушить порядок индексов
            for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
                hotSpots.remove(indicesToRemove.get(i));
            }
            // Возвращаем true, если были удалены элементы
            return !indicesToRemove.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Panorama.removeHotSpotWithPanoramaUrl Ошибка при обработке JSON-структуры.: " + e.toString());
            return false;
        }
    }

}
