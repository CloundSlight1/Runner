package com.wuyz.runner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by wuyz on 2016/10/8.
 *
 */

public class App extends android.app.Application {

    private static final String TAG = "App";
    private static App instance;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log2.d(TAG, "onReceive %s", action);
            if (Log2.ACTION_FLUSH_LOG.equals(action)) {
                Log2.flush();
            }
        }
    };

    @Override
    public void onCreate() {
        Log2.d(TAG, "onCreate");
        super.onCreate();
        instance = this;
        initExceptionHandler();
        if (Log2.ENABLE)
            registerReceiver(receiver, new IntentFilter(Log2.ACTION_FLUSH_LOG));
        ThreadExecutor.initExecutorService();
    }

    private void initExceptionHandler() {
        final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log2.e(TAG, e);
                File file;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    file = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                } else {
                    file = getExternalFilesDir("Documents");
                }
                if (!file.exists()) {
                    file.mkdirs();
                }
                file = new File(file, "error");
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
                    writer.println(StringUtils.dateFormat.format(new Date()));
                    e.printStackTrace(writer);
                    Throwable throwable = e.getCause();
                    while (throwable != null) {
                        throwable.printStackTrace(writer);
                        throwable = throwable.getCause();
                    }
                    writer.close();
                } catch (Exception e2) {
                    Log2.e(TAG, e2);
                } finally {
                    if (writer != null)
                        writer.close();
                }
                if (handler != null) {
                    handler.uncaughtException(t, e);
                }
            }
        });
    }

    public static App getInstance() {
        return instance;
    }
}
