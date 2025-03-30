package ru.miacomsoft.huawei_meta.photo360.libjs;

import android.app.Activity;
import android.webkit.WebView;

import ru.miacomsoft.huawei_meta.utils.orientation_sensor.sensors.Orientation;
import ru.miacomsoft.huawei_meta.utils.orientation_sensor.utils.OrientationSensorInterface;


public class OrientationSensor  implements OrientationSensorInterface {
    // https://github.com/majidgolshadi/Android-Orientation-Sensor
    private WebView webView;
    private Activity parentActivity;
    private Orientation orientationSensor;
    public OrientationSensor(Activity activity, WebView webView) {
        this.webView = webView;
        this.parentActivity = activity;
    }

    //@Override
    public void onResume() {
        //super.onResume();
        orientationSensor = new Orientation(parentActivity.getApplicationContext(), this);
        orientationSensor.init(1.0, 1.0, 1.0);
        orientationSensor.on(2); // SPEED: 0 Normal, 1 UI, 2 GAME, 3 FASTEST
        //---------------------------------------
        // return true or false
        orientationSensor.isSupport();
    }


    //@Override
    public void onPause() {
        // turn orientation sensor off
        orientationSensor.off();
        //super.onPause();
    }


    @Override
    public void orientation(Double AZIMUTH, Double PITCH, Double ROLL) {
        StringBuffer sb = new StringBuffer();
        sb.append("javascript: orientation={};");
        sb.append("orientation_azimuth=");
        sb.append(AZIMUTH);
        sb.append(";");
        sb.append("orientation_pitch=");
        sb.append(PITCH);
        sb.append(";");
        sb.append("orientation_roll=");
        sb.append(ROLL);
        sb.append(";");
        // Log.d("Panorama360Log",sb.toString());
        // sb.append(" console.log(orientation_azimuth+' -- '+orientation_roll);");
        webView.loadUrl(sb.toString());
    }
}
