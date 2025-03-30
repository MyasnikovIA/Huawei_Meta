package ru.miacomsoft.huawei_meta.utils.orientation_sensor.sensors;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Observable;
/**
 * User: MyasnikovIA
 * Date: 20/12/2024
 */
public class accelerometer  extends Observable implements SensorEventListener,Isensor {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEvent event;
    public accelerometer(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    
    @Override
    public boolean isSupport(){
    	if (accelerometerSensor == null)
    		return false;
    	return true;
    }

    @Override
    public void on(int speed){
    	switch (speed) {
		case 0:
			sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			break;

		case 1:
			sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
			break;

		case 2:
			sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
			break;

		case 3:
			sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
			break;
			
		default:
			sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			break;
		}
    }

    @Override
    public void off(){
        sensorManager.unregisterListener(this, accelerometerSensor);
    }

    @Override
    public float getMaximumRange()
    {
        return accelerometerSensor.getMaximumRange();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        event = sensorEvent;
        setChanged();
        notifyObservers();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public SensorEvent getEvent(){
        return event;
    }
}
