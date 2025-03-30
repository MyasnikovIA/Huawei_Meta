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
public class gyroscope extends Observable implements SensorEventListener,Isensor {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEvent event;

    public gyroscope(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    
    @Override
    public boolean isSupport() {
    	if (gyroscopeSensor == null)
    		return false;
    	return true;
    }

    @Override
    public void on(int speed) {
    	switch (speed) {
		case 0:
			sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
			break;

		case 1:
			sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
			break;

		case 2:
			sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
			break;

		case 3:
			sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
			break;
			
		default:
			sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
			break;
		}
    }

    @Override
    public void off(){
        sensorManager.unregisterListener(this, gyroscopeSensor);
    }

    @Override
    public float getMaximumRange() {
        return gyroscopeSensor.getMaximumRange();
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
