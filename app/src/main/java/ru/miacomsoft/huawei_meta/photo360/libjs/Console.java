package ru.miacomsoft.huawei_meta.photo360.libjs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import ru.miacomsoft.huawei_meta.photo360.lib.SqlLiteOrm;


public class Console {

    private long lastUpdate;
    private WebView webView;
    private SqlLiteOrm sqlLocal;
    private Activity parentActivity;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Console(Activity activity, WebView webView, SqlLiteOrm sqlLocal) {
        this.webView = webView;
        parentActivity = activity;
        lastUpdate = System.currentTimeMillis();
        this.sqlLocal = sqlLocal;
    }

/**
 * Вывод консоли
 *
 * @param msg
 */
@JavascriptInterface
public void log(String msg) {
    Log.d("console.log", msg);
}

@JavascriptInterface
public void error(String msg) {
        Log.e("console.log", msg);
    }

}
