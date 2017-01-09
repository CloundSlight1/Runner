package com.wuyz.runner;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private int curStep = 0;
    private CursorAdapter adapter;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Switch statusSwitch = (Switch) findViewById(R.id.status_view);
        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    registerSensor();
                else
                    unregisterSensor();
                preferences.edit().putBoolean("enable", b).apply();
            }
        });

        if (preferences.getBoolean("enable", true))
            statusSwitch.setChecked(true);

        adapter = new StepAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        ListView listView = (ListView) findViewById(R.id.list1);
        listView.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(MyReceiver.ACTION_STEP_COUNTER), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                Utils.getDayZeroTime(System.currentTimeMillis()) + 3600000,
                Utils.DAY_SECONDS, pendingIntent);
    }

    @Override
    protected void onDestroy() {
        unregisterSensor();
        super.onDestroy();
    }

    private boolean registerSensor() {
        Log2.d(TAG, "registerSensor");
        boolean ret = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL);
        Log2.d(TAG, "registerSensor %b", ret);
        App.getInstance().setRegistered(ret);
        if (!ret)
            Toast.makeText(this, "can't find step counter sensor", Toast.LENGTH_SHORT).show();
        return ret;
    }

    private void unregisterSensor() {
        Log2.d(TAG, "unregisterSensor");
        try {
            if (App.getInstance().isRegistered()) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        App.getInstance().setRegistered(false);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final int step = (int) sensorEvent.values[0];
        Log2.d(TAG, "onSensorChanged %d", step);
        if (curStep != step) {
            curStep = step;
            final long time = System.currentTimeMillis();
            ThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepProvider.Step.saveStep(App.getInstance(), step, time);
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, StepProvider.Step.CONTENT_URL, StepProvider.Step.COLUMNS, null, null,
                StepProvider.Step.KEY_START_TIME + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
