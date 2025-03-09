package ru.miacomsoft.huawei_meta.view_photo;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.File;

import ru.miacomsoft.huawei_meta.view_photo.lib.SqlLiteOrm;
import ru.miacomsoft.huawei_meta.view_photo.libjs.Android;
import ru.miacomsoft.huawei_meta.view_photo.libjs.Console;
import ru.miacomsoft.huawei_meta.view_photo.libjs.LocalStorage;

public class Panorama {
    private WebView myWebView;
    private SqlLiteOrm sqlLiteORM;
    private AppCompatActivity appCompatActivity;
    public Panorama(AppCompatActivity appCompatActivity){
        this.appCompatActivity  = appCompatActivity;
    }

    public void getPhoto(int WebViewId , File file) {
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
        try {
            myWebView.addJavascriptInterface(new LocalStorage(appCompatActivity, myWebView, sqlLiteORM), "localStorage");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // String imagePath = "file:///storage/emulated/0/DCIM/PANORAMA_HUAWEI/Camera/img.jpg";
        String imagePath = "file://"+file.getAbsolutePath();
        // myWebView.loadUrl(imagePath);
        // Получаем экземпляр WindowManager
        WindowManager wm = (WindowManager) appCompatActivity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        myWebView.loadUrl("file:///android_asset/pano2.html?img="+imagePath+"&width="+myWebView.getWidth()+"&height="+myWebView.getHeight());


        StringBuffer sb = new StringBuffer();
        sb.append("javascript: ").append("local_file='").append("file://"+file.getAbsolutePath()).append("';");
        sb.append("console.log('------'+local_file);");
        sb.append("console.log('metrics.widthPixels=="+metrics.heightPixels+"');");
        sb.append("console.log('myWebView.getWidth()=="+myWebView.getWidth()+"');");
        sb.append("console.log('myWebView.getHeight()=="+myWebView.getHeight()+"');");
        // sb.append("viewPanoImage(local_file);");
        // sb.append("setTimeout(function tick() { window.loadPano(local_file);}, 1000);");
        myWebView.loadUrl(sb.toString());
    }

}
