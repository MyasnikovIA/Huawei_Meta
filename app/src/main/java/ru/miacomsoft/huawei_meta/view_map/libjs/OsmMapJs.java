package ru.miacomsoft.huawei_meta.view_map.libjs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import ru.miacomsoft.huawei_meta.ViewPanp360Activity;

public class OsmMapJs {

    private String TAG = "OsmMapJs";
    private WebView webView;
    private AppCompatActivity parentActivity;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public OsmMapJs(AppCompatActivity activity, WebView webView) {
        this.webView = webView;
        parentActivity = activity;
    }

    private int selectNewPointIndEdit = 0;
    @JavascriptInterface
    public void opanPanorama360(String jsonStr) {
        Intent intent = new Intent(parentActivity.getApplicationContext(), ViewPanp360Activity.class);
        intent.putExtra("jsonObjStr", jsonStr);
        parentActivity.startActivity(intent);
    }
}
