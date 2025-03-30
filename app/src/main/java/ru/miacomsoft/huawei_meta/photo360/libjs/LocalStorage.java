package ru.miacomsoft.huawei_meta.photo360.libjs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.miacomsoft.huawei_meta.photo360.lib.SqlLiteOrm;


public class LocalStorage {

    private long lastUpdate;
    private WebView webView;
    private SqlLiteOrm sqlLocal;
    private Activity parentActivity;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public LocalStorage(Activity activity, WebView webView, SqlLiteOrm sqlLocal) throws JSONException {
        this.webView = webView;
        parentActivity = activity;
        lastUpdate = System.currentTimeMillis();
        this.sqlLocal = sqlLocal;
        if (!this.sqlLocal.getIsExistTab("localStorage")) {
            JSONObject raw = new JSONObject();
            raw.put("key", "");
            raw.put("value", "");
            this.sqlLocal.insertJson("localStorage", raw);
        }
    }
    @JavascriptInterface
    public String getItem(String keyName) {
        JSONArray res = sqlLocal.getRows("localStorage", "key='" + keyName + "'");
        Log.d("MainActivity",res.toString());
        Log.d("res.length()",String.valueOf(res.length()));
        if (res.length() == 0) {
            return "null";
        }
        try {
            return res.getJSONObject(0).getString(keyName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "null";
    }

    @JavascriptInterface
    public void setItem(String keyName, String value) {
        try {
            JSONArray res = sqlLocal.getRows("localStorage", "key='" + keyName + "'");
            JSONObject raw;
            if (res.length() == 0) {
                raw = new JSONObject();
                raw.put("key", keyName);
                raw.put("value", value);
                this.sqlLocal.insertJson("localStorage", raw);
            } else {
                raw = res.getJSONObject(0);
                raw.put("value", value);
                this.sqlLocal.updateJson("localStorage", raw);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void removeItem(String keyName) {
        try {
            JSONArray res = sqlLocal.getRows("localStorage", "key='" + keyName + "'");
            if (res.length() == 0) {
                return;
            }
            JSONObject raw = res.getJSONObject(0);
            sqlLocal.del("localStorage",raw.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
