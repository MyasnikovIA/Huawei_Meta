
package ru.miacomsoft.huawei_meta.utils.orientation_sensor;

import android.content.Context;

import ru.miacomsoft.huawei_meta.utils.orientation_sensor.sensors.Orientation;
import ru.miacomsoft.huawei_meta.utils.orientation_sensor.utils.OrientationSensorInterface;


public class OrientationSensor implements OrientationSensorInterface {
    public interface CallbackOrientationSensor {
        void call(Double azimuth, Double pitch, Double ROLL);
    }

    private Context context;
    private Orientation orientationSensor;
    private CallbackOrientationSensor callbackOrientationSensor;

    private Double azimuth;
    private Double pitch;
    private Double roll;

    public OrientationSensor(Context context ) {
        this.context = context;
        this.callbackOrientationSensor = callbackOrientationSensor;
    }
    public void setOrientationSensor(CallbackOrientationSensor callbackOrientationSensor){
        this.callbackOrientationSensor = callbackOrientationSensor;
    }
    public double getAZIMUTH() {
        return azimuth;
    }
    public double getROLL() {
        return roll;
    }
    public double getPITCH() {
        return pitch;
    }

    //@Override
    public void onResume() {
        //super.onResume();
        orientationSensor = new Orientation(context, this);
        orientationSensor.init(1.0, 1.0, 1.0);
        orientationSensor.on(2); // SPEED: 0 Normal, 1 UI, 2 GAME, 3 FASTEST
        orientationSensor.isSupport();
    }


    //@Override
    public void onPause() {
        orientationSensor.off();
        //super.onPause();
    }

    //@Override
    public void onDestroy() {
        //super.onDestroy();
        orientationSensor.off();
    }
    @Override
    public void orientation(Double azimuth, Double pitch, Double roll) {
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        if (callbackOrientationSensor!=null) {
            callbackOrientationSensor.call( azimuth, pitch,  roll);
        }
    }
}
