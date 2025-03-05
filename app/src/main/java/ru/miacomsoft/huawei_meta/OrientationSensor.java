
package ru.miacomsoft.huawei_meta;

import android.content.Context;

import ru.miacomsoft.huawei_meta.orientation_sensor.sensors.Orientation;
import ru.miacomsoft.huawei_meta.orientation_sensor.utils.OrientationSensorInterface;


public class OrientationSensor implements OrientationSensorInterface {
    public interface CallbackOrientationSensor {
        void call(Double AZIMUTH, Double PITCH, Double ROLL);
    }

    private Context context;
    private Orientation orientationSensor;
    private CallbackOrientationSensor callbackOrientationSensor;

    private Double AZIMUTH;
    private Double PITCH;
    private Double ROLL;

    public OrientationSensor(Context context ) {
        this.context = context;
        this.callbackOrientationSensor = callbackOrientationSensor;
    }
    public void setOrientationSensor(CallbackOrientationSensor callbackOrientationSensor){
        this.callbackOrientationSensor = callbackOrientationSensor;
    }
    public double getAZIMUTH() {
        return AZIMUTH;
    }
    public double getROLL() {
        return ROLL;
    }
    public double getPITCH() {
        return PITCH;
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
    public void orientation(Double AZIMUTH, Double PITCH, Double ROLL) {
        this.AZIMUTH = AZIMUTH;
        this.PITCH = PITCH;
        this.ROLL = ROLL;
        if (callbackOrientationSensor!=null) {
            callbackOrientationSensor.call( AZIMUTH, PITCH,  ROLL);
        }
    }
}
