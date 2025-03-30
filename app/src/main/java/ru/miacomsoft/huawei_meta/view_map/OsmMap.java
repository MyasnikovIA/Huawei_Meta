package ru.miacomsoft.huawei_meta.view_map;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import ru.miacomsoft.huawei_meta.setup.SetupApp;
import ru.miacomsoft.huawei_meta.view_map.libjs.OsmMapJs;

public class OsmMap {
    public interface CallbackEmptyReturn {
        void call();
    }

    public interface CallbackStringEmptyReturn {
        void call(String value);
    }
    public interface CallbackLatLon {
        void call(double lat, double lon);
    }

    private AppCompatActivity appCompatActivity;
    private String TAG = "view_map.OsmMap";
    public WebView webView;
    private CallbackLatLon callbackLatLon;

    public OsmMap(AppCompatActivity appCompatActivity) {
       this.appCompatActivity = appCompatActivity;
    }

    public void onViewMap(int R_id_webView,double lat, double lon,int zoom,CallbackEmptyReturn callbackEmptyReturn){
        webView = appCompatActivity.findViewById(R_id_webView);
        webView.setWebViewClient(new WebViewClient()); // Устанавливаем WebViewClient для загрузки URL внутри WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Включение JavaScript
        settings.setDomStorageEnabled(true); // Включение DOM Storage
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // Режим кэширования
        settings.setDatabaseEnabled(true); // Включение кэша приложения
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                //if (url.contains("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png")) {
                // todo: дописать механизм кэширования картинок карты
                if (url.indexOf(".tile.openstreetmap.org/")!=-1 && url.substring(url.lastIndexOf(".")+1).toLowerCase().equals("png")) {
                    // Заменяем {s}, {z}, {x}, {y} на реальные значения из URL
                    String[] parts = url.split("/");
                    String z = parts[parts.length - 3];
                    String x = parts[parts.length - 2];
                    String y = parts[parts.length - 1].replace(".png", "");
                    String s = url.replaceAll("https://","");
                    s = s.substring(0,s.indexOf("."));
                    String fileName = "map_" + s + "_"+ z + "_" + x + "_" + y + ".png";

                    // Хронение снимков карты в каталоге кэша приложения
                    // /data/data/<ваш.package.name>/cache/
                    // File mapDir =appCompatActivity.getCacheDir();
                    // Хронение снимков карты в каталоге фотографий
                    File mapDir = null;
                    if (SetupApp.CONFIG.has("MapTmpDir")) {
                        try {
                            mapDir = new File(SetupApp.CONFIG.getString("MapTmpDir"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        mapDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(), "MAP_IMG");
                    }
                    if (!mapDir.exists()) {
                        mapDir.mkdirs();
                    }

                    Map<String, String> headers = request.getRequestHeaders();
                    String method = request.getMethod();

                    // Логируем параметры запроса
                    Log.d("WebResourceRequest", "URL: " + url);
                    Log.d("WebResourceRequest", "Method: " + method);
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        Log.d("WebResourceRequest", "Header: " + entry.getKey() + " = " + entry.getValue());
                    }

                    // /data/data/<ваш.package.name>/cache/
                    File file = new File(mapDir, fileName);
                    if (file.exists()) {
                        // Если файл уже есть, возвращаем его
                        try {
                            InputStream inputStream = new FileInputStream(file);
                            return new WebResourceResponse("image/png", "UTF-8", inputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        new DownloadFileTask().execute(url, file.getAbsolutePath());
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        // Загрузка начальной карты
        // webView.loadUrl("https://www.openstreetmap.org/?mlat=53.3530966&mlon=83.6758076#map=17/53.3530966/83.6758076");
        loadPage("file:///android_asset/osm.html?lat="+lat+"&lon="+lon+"&zoom="+zoom,callbackEmptyReturn);
    }

    public void onViewMapArrayPoint(int R_id_webView, double lat, double lon, int zoom, JSONArray points){
        webView = appCompatActivity.findViewById(R_id_webView);
        webView.setWebViewClient(new WebViewClient()); // Устанавливаем WebViewClient для загрузки URL внутри WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Включение JavaScript
        settings.setDomStorageEnabled(true); // Включение DOM Storage
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // Режим кэширования
        settings.setDatabaseEnabled(true); // Включение кэша приложения
        webView.addJavascriptInterface(new OsmMapJs(appCompatActivity, webView), "OsmMapJs");
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                //if (url.contains("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png")) {
                // todo: дописать механизм кэширования картинок карты
                if (url.indexOf(".tile.openstreetmap.org/")!=-1 && url.substring(url.lastIndexOf(".")+1).toLowerCase().equals("png")) {
                    // Заменяем {s}, {z}, {x}, {y} на реальные значения из URL
                    String[] parts = url.split("/");
                    String z = parts[parts.length - 3];
                    String x = parts[parts.length - 2];
                    String y = parts[parts.length - 1].replace(".png", "");
                    String s = url.replaceAll("https://","");
                    s = s.substring(0,s.indexOf("."));
                    String fileName = "map_" + s + "_"+ z + "_" + x + "_" + y + ".png";

                    // Хронение снимков карты в каталоге кэша приложения
                    // /data/data/<ваш.package.name>/cache/
                    // File mapDir =appCompatActivity.getCacheDir();

                    // Хронение снимков карты в каталоге фотографий
                    File mapDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(), "MAP_IMG");
                    if (!mapDir.exists()) {
                        mapDir.mkdirs();
                    }

                    Map<String, String> headers = request.getRequestHeaders();
                    String method = request.getMethod();

                    // Логируем параметры запроса
                    Log.d("WebResourceRequest", "URL: " + url);
                    Log.d("WebResourceRequest", "Method: " + method);
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        Log.d("WebResourceRequest", "Header: " + entry.getKey() + " = " + entry.getValue());
                    }

                    // /data/data/<ваш.package.name>/cache/
                    File file = new File(mapDir, fileName);
                    if (file.exists()) {
                        // Если файл уже есть, возвращаем его
                        try {
                            InputStream inputStream = new FileInputStream(file);
                            return new WebResourceResponse("image/png", "UTF-8", inputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        new DownloadFileTask().execute(url, file.getAbsolutePath());
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        loadPage("file:///android_asset/osm.html?lat="+lat+"&lon="+lon+"&zoom="+zoom, ()->{
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: markers_data_arr="+points.toString()+";");
            sb.append("markers_data_arr.forEach(markerData => {const markeOne = L.marker([markerData.lat, markerData.lon]).addTo(map).bindPopup(`<b> <a href=\"javascript:onClickPointName(${JSON.stringify(markerData)});\">${markerData.name}</a> </b><br>Lat: ${markerData.lat}, Lon: ${markerData.lon}`).on('click', () => onMarkerClick(markerData));});");
            webView.loadUrl(sb.toString());
        });
    }

    private void loadPage(String urlPage,CallbackEmptyReturn callbackEmptyReturn) {
        webView.setWebChromeClient(new WebChromeClient(){
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
        webView.loadUrl(urlPage);
    }



    private class DownloadFileTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            String fileURL = urls[0];
            String savePath = urls[1];
            downloadFile(fileURL, savePath);
            return null;
        }
    }


    public void downloadFile(String fileUrl, String savePath){
        try {
            String path = fileUrl.split("tile.openstreetmap.org")[1];
            URL url = new URL(fileUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("sec-ch-ua-platform", "Android");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Infinix X6833B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/133.0.6943.137 Mobile Safari/537.36");
            connection.setRequestProperty("sec-ch-ua", "\"Not(A:Brand\";v=\"99\", \"Android WebView\";v=\"133\", \"Chromium\";v=\"133\"");
            connection.setRequestProperty("sec-ch-ua-mobile", "?1");
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Открываем поток для чтения данных
                try (InputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream out = new FileOutputStream(savePath)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                throw new IOException("Ошибка HTTP: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSelectGps(double lat ,double lon) {
        if (webView != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: marker.setLatLng([").append(lat).append(", ").append(lon).append("]).bindPopup(`Latitude: ${").append(lat).append("} Longitude: ${").append(lon).append("}`).openPopup();");
            webView.loadUrl(sb.toString());
        }
    }

    public void setCenterGps(double lat ,double lon) {
        if (webView != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: map.setView([").append(lat).append(", ").append(lon).append("]);");
            webView.loadUrl(sb.toString());
        }
    }
    public void addPoint(double lat ,double lon) {
        if (webView != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: markeOne = L.marker(["+lat+", "+lon+"]).addTo(map).bindPopup(`<b>markerData.name</b><br>"+lat+": markerData."+lat+"}, "+lon+": markerData.lon`).on('click', () => onMarkerClick(markerData));");
            webView.loadUrl(sb.toString());
        }
    }

    public void getVar(String key, CallbackStringEmptyReturn callbackJSONObjectEmptyReturn) {
        if (webView != null) {
            webView.evaluateJavascript("(function() { return "+key+";})();", value -> {
                callbackJSONObjectEmptyReturn.call(value);
            });
        } else {
            callbackJSONObjectEmptyReturn.call(null);
        }
    }

    public void getSelectGps(CallbackLatLon callbackLatLonLocal){
        this.callbackLatLon = callbackLatLonLocal;
        getVar("lat+'#'+lon",(String value)-> {
            String[] valueArr = value.replaceAll("\"","").split("#");
            if (callbackLatLon!=null) {
                callbackLatLon.call(Double.valueOf(valueArr[0]), Double.valueOf(valueArr[1]));
                callbackLatLon = null;
            }
        });
    }


}
