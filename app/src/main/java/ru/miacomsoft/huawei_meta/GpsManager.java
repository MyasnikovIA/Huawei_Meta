package ru.miacomsoft.huawei_meta;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


public class GpsManager implements LocationListener, android.location.OnNmeaMessageListener  {
    private static final int REQUEST_LOCATION_PERMISSION = 1000;

    public interface CallbackOnChangeLatLon {
        public String call(JSONObject query) throws JSONException, IOException;
    }
    public interface CallbackOnChangeLocation {
        public void call(double lat,double lon);
    }



    private static final String TAG = "GpsManager";
    private Context context;
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private CallbackOnChangeLatLon callbackOnChangeLatLon=null;
    private CallbackOnChangeLocation callbackOnChangeLocation=null;


    public GpsManager(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // 1) Включение GPS навигации
    public JSONObject enableGps() {
        JSONObject result = new JSONObject();
        try {
            if (locationManager != null) {
                try {
                    if (!isGpsEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
                        locationManager.addNmeaListener(this);
                        isGpsEnabled = true;
                    }
                    result.put("isGpsEnabled",isGpsEnabled);
                    Log.d(TAG, "GPS enabled");
                } catch (SecurityException e) {
                    result.put("isGpsEnabled",false);
                    result.put("Error",formatException(e));
                }
            } else {
                result.put("isGpsEnabled",false);
                result.put("Error","locationManager == null");
            }
        } catch (JSONException e) {
            Log.e(TAG, "enableGps: " + e.toString());
        }
        return result;
    }

    // 2) Выключение GPS навигации
    public JSONObject disableGps() throws JSONException {
        JSONObject result = new JSONObject();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
                locationManager.removeNmeaListener(this);
                isGpsEnabled = false;
                result.put("isGpsDisable",true);
                Log.d(TAG, "GPS disabled");
            } catch (SecurityException e) {
                result.put("isGpsDisable",false);
                result.put("Error",formatException(e));
                Log.e(TAG, "disableGps: " + e.toString());
            }
        } else {
            result.put("isGpsEnabled",false);
            result.put("Error","locationManager == null");
        }
        return result;
    }

    public void setOnChangeLatLon(CallbackOnChangeLatLon callbackOnChangeLatLon){
        this.callbackOnChangeLatLon=callbackOnChangeLatLon ;
    }
    public boolean isExistOnChangeLatLon(){
        return this.callbackOnChangeLatLon==null;
    }

    public boolean isEnabelGps() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    protected void onChangeLocation(CallbackOnChangeLocation callbackOnChangeLocation) {
        this.callbackOnChangeLocation = callbackOnChangeLocation;
    }

    @SuppressLint("MissingPermission")
    protected JSONObject showLocation() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("isGpsEnabled",isGpsEnabled);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            result.put("provider","GPS_PROVIDER");
            result.put("lon",location.getLongitude());
            result.put("lat",location.getLatitude());
            // Toast.makeText(this, message, Toast.LENGTH_LONG).show(); //всплывающее сообщение
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                result.put("provider","NETWORK_PROVIDER");
                result.put("lon", location.getLongitude());
                result.put("lat", location.getLatitude());
                // Toast.makeText(this, message, Toast.LENGTH_LONG).show(); //всплывающее сообщение
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (location != null) {
                    result.put("provider","PASSIVE_PROVIDER");
                    result.put("lon", location.getLongitude());
                    result.put("lat", location.getLatitude());
                    // Toast.makeText(this, message, Toast.LENGTH_LONG).show(); //всплывающее сообщение
                } else {
                    result.put("Error", "location == null");
                    if (!isEnabelGps()) {
                        result.put("isGpsEnabled", isGpsEnabled);
                        result.put("GPS", "OFF");
                    }
                }
            }
        }
        return result;
    }

    // 4) Получение данных из NETWORK_PROVIDER, если GPS_PROVIDER выключен
    public void requestLocationUpdates() {
        if (locationManager != null) {
            try {
                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
                }
            } catch (SecurityException e) {
                Log.e(TAG, "requestLocationUpdates: " + e.toString());
            }
        }
    }

    // Методы LocationListener
    @Override
    public void onLocationChanged(Location location) {

        if (callbackOnChangeLocation!=null) {
            callbackOnChangeLocation.call(location.getLatitude(),location.getLongitude());
        }
        if (callbackOnChangeLatLon!=null) {
            try {
                JSONObject inputParam = new JSONObject();
                inputParam.put("lat",location.getLatitude());
                inputParam.put("lon",location.getLongitude());
                callbackOnChangeLatLon.call(inputParam);
            } catch (JSONException | IOException e) {
                Log.e(TAG, "onLocationChanged: " + e.toString());
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status changed: " + provider + ", status: " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
    }



    @SuppressLint("MissingPermission")
    protected void onResume() {
        // super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, this);
        locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, this);
    }

    protected void onPause() {
        //super.onPause();
        locationManager.removeUpdates(this);
    }

    public void onLocationSettings() {
        context.startActivity(new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    @Override
    public void onNmeaMessage(String s, long l) {

    }
    /**
     * Форматирует исключение в строку.
     * @param exception Исключение
     * @return Форматированная строка с ошибкой
     */
    public static String formatException(Exception exception) {
        StringBuilder sbError = new StringBuilder();
        sbError.append("\nПроизошла ошибка: ").append(exception.getMessage());
        sbError.append("\nТип ошибки: ").append(exception.getClass().getName());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        sbError.append("\nПодробное описание:\n").append(sw.toString());
        int index = 0;
        for (StackTraceElement element : exception.getStackTrace()) {
            sbError.append("\n").append(++index).append(")\nLine number:")
                    .append(element.getLineNumber()).append("\nClassName:")
                    .append(element.getClassName()).append("\nFileName:")
                    .append(element.getFileName()).append("\n")
                    .append(element.toString());
        }
        return sbError.toString();
    }
}