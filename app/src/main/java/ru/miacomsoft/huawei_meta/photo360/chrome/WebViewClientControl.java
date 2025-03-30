package ru.miacomsoft.huawei_meta.photo360.chrome;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;



public class WebViewClientControl extends WebViewClient {
    // String urlintent = "https://www.google.co.in";
    String urlintent = "index.html";
    private Activity activity;

    public WebViewClientControl(Activity mainActivity) {
        this.activity = mainActivity;
    }
    @Override
    public void onLoadResource(WebView view, String url) {
        // показать ресурс с которого идет загрузка
        Log.d("TAG", url);
        //Toast.makeText(activity, "Connecting url :" + url, Toast.LENGTH_LONG).show();
        super.onLoadResource(view, url);
    }


    //-----------------------------------------------------------------------------------------------------------
    //----- Запретить загрузку URL ------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------
/*
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

        // Запретить получение данных с ресурса (переопределение контента  InputStream)
        String url = request.getUrl().toString();
        WebResourceResponse response = super.shouldInterceptRequest(view, request); // load native js
        if (url != null ){
           if (
                   //   (url.contains("google"))||
                   //    (url.contains("yandex.ru"))||
                   //   (url.contains("s/desktop/"))|| // реклама на ютубе
                   url.contains("theme-classic-all.css")){
                response = new WebResourceResponse( "text/javascript", "utf-8",null );
            }
        }
        return response;
    }
*/
    //-----------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------
    /**
     *  Запретить открывать браузер по нажатию на ссылку
      * @param view
     * @param request
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        //  view.loadUrl(request.getUrl().toString());
        return true;
    }
    //-----------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        /*
        //  Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_LONG).show();
        Toast.makeText(activity, "Failed loading app!, No Internet Connection found.", Toast.LENGTH_LONG).show();
        // view.loadUrl("about:blank");
        // view.loadUrl("javascript:document.open();document.close();");
        if (Build.VERSION.SDK_INT < 18) {
            view.clearView();
        } else {
           view.loadUrl("file:///android_asset/myerrorpage.html");
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
         */
    }

}
