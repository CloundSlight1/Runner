package com.wuyz.runner;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
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

import java.util.Calendar;

public class MainActivity extends Activity implements SensorEventListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Switch statusSwitch;
    private int curStep = 0;
    private CursorAdapter adapter;
    private ListView listView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        statusSwitch = (Switch) findViewById(R.id.status_view);
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
        listView = (ListView) findViewById(R.id.list1);
        listView.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        unregisterSensor();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (statusSwitch.isChecked()) {
            moveTaskToBack(false);
            return;
        }
        super.onBackPressed();
    }

    private void registerSensor() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensor() {
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    saveStep(step, time);
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private static long getDayZeroTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void saveStep(int step, long time) {
        Log2.d(TAG, "saveStep %d %d", time, step);
        long dayTime = getDayZeroTime(time);
        try (Cursor cursor = getContentResolver().query(StepProvider.Step.CONTENT_URL,
                new String[]{StepProvider.Step.KEY_ID, StepProvider.Step.KEY_START_STEP},
                StepProvider.Step.KEY_DAY_TIME + "=?",
                new String[]{String.valueOf(dayTime)}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                int startStep = cursor.getInt(1);
                cursor.close();

                ContentValues values = new ContentValues(2);
                values.put(StepProvider.Step.KEY_END_TIME, time);
                values.put(StepProvider.Step.KEY_STEP, step - startStep);
                getContentResolver().update(StepProvider.Step.CONTENT_URL, values,
                        StepProvider.Step.KEY_ID + "=" + id, null);
            } else {
                ContentValues values = new ContentValues(4);
                values.put(StepProvider.Step.KEY_DAY_TIME, dayTime);
                values.put(StepProvider.Step.KEY_START_TIME, time);
                values.put(StepProvider.Step.KEY_END_TIME, time);
                values.put(StepProvider.Step.KEY_STEP, 1);
                values.put(StepProvider.Step.KEY_START_STEP, step);
                getContentResolver().insert(StepProvider.Step.CONTENT_URL, values);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, StepProvider.Step.CONTENT_URL, StepProvider.Step.COLUMNS, null, null,
                StepProvider.Step.KEY_DAY_TIME + " desc");
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
