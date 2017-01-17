package com.wuyz.runner;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private static final int MSG_SAVE_STEP = 1;
    private static final int MSG_DATABASE_CHANGE = 2;

    private SensorManager sensorManager;
    private StepAdapter adapter;
    private SharedPreferences preferences;
    private AlarmManager alarmManager;
    private PendingIntent saveStepAlarmIntent;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAVE_STEP:
                    final int step = msg.arg1;
                    final long time = (long) msg.obj;
                    ThreadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            StepProvider.Step.saveStep(MainActivity.this, step, time);
                        }
                    });
                    break;
                case MSG_DATABASE_CHANGE:
                    updateList();
                    break;
            }
        }
    };

    private ContentObserver contentObserver = new ContentObserver(null) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            handler.removeMessages(MSG_DATABASE_CHANGE);
            handler.sendEmptyMessageDelayed(MSG_DATABASE_CHANGE, 1000);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        saveStepAlarmIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(MyReceiver.ACTION_STEP_COUNTER), PendingIntent.FLAG_UPDATE_CURRENT);

        Switch statusSwitch = (Switch) findViewById(R.id.status_view);
        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (registerSensor()) {
                        alarmManager.cancel(saveStepAlarmIntent);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                System.currentTimeMillis() + Utils.HOUR_SECONDS * 2,
                                Utils.DAY_SECONDS * 2, saveStepAlarmIntent);
                    }
                } else {
                    unregisterSensor();
                    alarmManager.cancel(saveStepAlarmIntent);
                }
                preferences.edit().putBoolean("enable", b).apply();
            }
        });

        statusSwitch.setChecked(preferences.getBoolean("enable", true));

        adapter = new StepAdapter(this);
        RecyclerView listView = (RecyclerView) findViewById(R.id.list1);
        listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);

        updateList();
        getContentResolver().registerContentObserver(StepProvider.Step.CONTENT_URL, true, contentObserver);
    }

    @Override
    protected void onDestroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        unregisterSensor();
        getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();
        Log2.flush();
    }

    private boolean registerSensor() {
        boolean ret = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL);
        Log2.d(TAG, "registerSensor %b", ret);
        if (!ret)
            Toast.makeText(this, "can't find step counter sensor", Toast.LENGTH_SHORT).show();
        return ret;
    }

    private void unregisterSensor() {
        Log2.d(TAG, "unregisterSensor");
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final int step = (int) sensorEvent.values[0];
//        long time = Utils.getTime(sensorEvent.timestamp);
        long time = System.currentTimeMillis();
//        Log2.d(TAG, "onSensorChanged %d %d", step, sensorEvent.timestamp);
        handler.removeMessages(MSG_SAVE_STEP);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_SAVE_STEP, step, 0, time), 1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateList() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<StepInfo> list = StepProvider.Step.getStepListByDay(MainActivity.this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setItems(list);
                    }
                });
            }
        });
    }
}
