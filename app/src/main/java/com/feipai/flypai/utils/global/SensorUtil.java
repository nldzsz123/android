package com.feipai.flypai.utils.global;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by YangLin on 2017-05-09.
 */

public class SensorUtil {
    private SensorManager manager;
    private Sensor sensor;
    private SensorListener listener;

    sensorLinstener l;

    public SensorUtil(Context context) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        listener = new SensorListener();
    }

    public final class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (l != null) {
                l.getSensorAngle(event.values[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


    public void registerSensorListener() {
        if (manager != null && listener != null && sensor != null) {
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void unRegisterSensorListener() {
        if (manager != null && listener != null) {
            manager.unregisterListener(listener);
        }
    }

    public void setListener(sensorLinstener l) {
        this.l = l;
    }

    public interface sensorLinstener {
        void getSensorAngle(float a);
    }
}
