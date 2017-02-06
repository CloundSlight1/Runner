package com.wuyz.runner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by wuyz on 2017/1/9.
 * MyReceiver
 */

public class MyReceiver extends BroadcastReceiver implements SensorEventListener {
    private static final String TAG = "MyReceiver";
    public static final String ACTION_STEP_COUNTER = "com.wuyz.runner.action.STEP_COUNTER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log2.d(TAG, "onReceive %s", intent.getAction());
        getWakeLock(context);
        registerSensor();
    }

    private boolean registerSensor() {
        try {
            SensorManager sensorManager = (SensorManager) App.getInstance().getSystemService(SENSOR_SERVICE);
            boolean ret = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log2.d(TAG, "registerSensor %b", ret);
            if (!ret)
                Toast.makeText(App.getInstance(), "can't find step counter sensor", Toast.LENGTH_SHORT).show();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void unregisterSensor() {
        try {
            SensorManager sensorManager = (SensorManager) App.getInstance().getSystemService(SENSOR_SERVICE);
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int step = (int) event.values[0];
//        final long time = Utils.getTime(event.timestamp);
        final long time = System.currentTimeMillis();
        Log2.d(TAG, "onSensorChanged %d %d", step, event.timestamp);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                StepProvider.Step.saveStep(App.getInstance(), step, time);
                unregisterSensor();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire(5000);
    }
}
