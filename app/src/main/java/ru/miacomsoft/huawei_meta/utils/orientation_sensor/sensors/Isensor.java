package ru.miacomsoft.huawei_meta.utils.orientation_sensor.sensors;

public interface Isensor {
	public boolean isSupport();
	public void on(int speed);
	public void off();
	public float getMaximumRange();
}
