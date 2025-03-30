package ru.miacomsoft.huawei_meta.photo360.chrome;


import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;



public class WebChromeClient extends android.webkit.WebChromeClient {

    private long lastUpdate;
    private WebView webView;
    // webView.loadUrl("javascript: Accel="+jsonObject.toString()   );
    private Activity parentActivity;

    public WebChromeClient(Activity activity, WebView webViewPar)  {
        webView=webViewPar;
        parentActivity = activity;
        lastUpdate = System.currentTimeMillis();
    }


    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        if (view instanceof FrameLayout) {
            FrameLayout frame = (FrameLayout) view;
            if (frame.getFocusedChild() instanceof VideoView) {
                VideoView video = (VideoView) frame.getFocusedChild();
                frame.removeView(video);
                video.start();
            }
        }
    }
    // Grant permissions for cam
    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        Log.d("TAG", "onPermissionRequest");
        parentActivity.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                Log.d("TAG", request.getOrigin().toString());
                if(request.getOrigin().toString().equals("file:///")) {
                    Log.d("TAG", "GRANTED");
                    request.grant(request.getResources());
                } else {
                    Log.d("TAG", "DENIED");
                    request.deny();
                }
            }
        });
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        if (progress < 100) {
            //  progressDialog.show();
        }
        if (progress == 100) {
            //  progressDialog.dismiss();
        }
    }
}
