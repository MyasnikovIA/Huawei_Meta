package ru.miacomsoft.huawei_meta.view_map;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class OsmMap {
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

    public void onViewMap(int R_id_webView,double lat, double lon,int zoom){
        webView = appCompatActivity.findViewById(R_id_webView);
        webView.setWebViewClient(new WebViewClient()); // Устанавливаем WebViewClient для загрузки URL внутри WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Включение JavaScript
        settings.setDomStorageEnabled(true); // Включение DOM Storage
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // Режим кэширования
        settings.setDatabaseEnabled(true); // Включение кэша приложения
        // Загрузка начальной карты
        // webView.loadUrl("https://www.openstreetmap.org/?mlat=53.3530966&mlon=83.6758076#map=17/53.3530966/83.6758076");
        webView.loadUrl("file:///android_asset/osm.html?lat="+lat+"&lon="+lon+"&zoom="+zoom);
    }

    public void setSelectGps(double lat ,double lon) {
        if (webView != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("javascript: marker.setLatLng([").append(lat).append(", ").append(lon).append("]).bindPopup(`Latitude: ${").append(lat).append("} Longitude: ${").append(lon).append("}`).openPopup();");
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
