package com.wuyz.runner;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by wuyz on 2017/1/9.
 * Utils
 */

public class Utils {
    private static final String TAG = "Utils";

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    public static final SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    public static final SimpleDateFormat dateFormat5 = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static final long HOUR_SECONDS = 3600L * 1000L;
    public static final long DAY_SECONDS = 24L * HOUR_SECONDS;

    public static long getDayZeroTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String getDateDesc(long time) {
        long todayTime = Utils.getDayZeroTime(System.currentTimeMillis());

        if (time >= todayTime && time < todayTime + Utils.DAY_SECONDS)
            return "今天";

        if (time < todayTime && time >= todayTime - Utils.DAY_SECONDS)
            return "昨天";

        if (time < todayTime - Utils.DAY_SECONDS && time >= todayTime - Utils.DAY_SECONDS * 2)
            return "前天";

        return dateFormat2.format(time);
    }

    public static long getTime(long nanoTime) {
        long delta = System.nanoTime() - nanoTime;
        return System.currentTimeMillis() - delta / 1000000L;
    }
}
