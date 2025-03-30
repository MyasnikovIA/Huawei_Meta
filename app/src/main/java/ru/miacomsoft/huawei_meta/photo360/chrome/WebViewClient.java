package ru.miacomsoft.huawei_meta.photo360.chrome;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Set;

public class WebViewClient extends android.webkit.WebViewClient {

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // https://russianblogs.com/article/8848276449/
        //view.loadUrl(url);
        Uri uri = Uri.parse(url);
        // Если протокол URL = предварительно согласованный протокол JS
        // Разобрать, чтобы разобрать параметры
        if (uri.getScheme().equals("js")) {
            // Если орган = предварительно согласовал веб-просмотр в соглашении, это означает, что все они соответствуют согласованному соглашению
            // Итак, перехватите URL, следующий JS начинает вызывать метод, необходимый для Android
            if (uri.getAuthority().equals("webview")) {
                // Шаг 3:
                // Логика, необходимая для выполнения JS
                System.out.println("js называется методом Android");
                // Может принимать параметры по протоколу и передавать их в Android
                HashMap<String, String> params = new HashMap<>();
                Set<String> collection = uri.getQueryParameterNames();
                Log.i("MainActivity", "collection:\n" + collection.toString());
            }
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        //progressBar.setVisibility(View.GONE);
    }
}
