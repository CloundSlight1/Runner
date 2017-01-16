package com.wuyz.runner;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class StepProvider extends ContentProvider {

    private static final String TAG = "StepProvider";

    public static final String AUTHORITY = "com.wuyz.runner.StepProvider";

    private static final int ITEM_ALL = 1;
    private static final int ITEM_SINGLE = 2;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "step", ITEM_ALL);
        sUriMatcher.addURI(AUTHORITY, "step/#", ITEM_SINGLE);
    }

    private DatabaseHelper databaseHelper;
    private ContentResolver contentResolver;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        contentResolver = context.getContentResolver();
        databaseHelper = new DatabaseHelper(context, DatabaseHelper.DATABASE, null, DatabaseHelper.VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        Log2.d(TAG, "query: %s %s", uri, selection);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case ITEM_SINGLE:
                builder.appendWhere(Step.KEY_ID + " = " + uri.getPathSegments().get(1));
                builder.setTables(Step.TABLE);
                break;
            case ITEM_ALL:
                builder.setTables(Step.TABLE);
                break;
            default:
                return null;
        }
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        if (TextUtils.isEmpty(sortOrder))
            sortOrder = Step.KEY_ID + " asc";
        Cursor cursor = builder.query(database, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(contentResolver, uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ITEM_ALL:
                return "vnd.android.cursor.dir/com.wuyz.runner.step";
            case ITEM_SINGLE:
                return "vnd.android.cursor.item/com.wuyz.runner.step";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log2.d(TAG, "insert %s", values);
        String table;
        switch (sUriMatcher.match(uri)) {
            case ITEM_ALL:
            case ITEM_SINGLE:
                table = Step.TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long rowId = databaseHelper.getWritableDatabase().insert(table, null, values);
        if (rowId > -1) {
            Uri newUri = ContentUris.withAppendedId(uri, rowId);
            contentResolver.notifyChange(newUri, null);
            return newUri;
        }
        try {
            throw new SQLException("Failed to insert row into " + uri);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log2.d(TAG, "delete: uri[%s] selection[%s] selectionArgs[%s]", uri, selection,
                (selectionArgs != null && selectionArgs.length > 0) ? selectionArgs[0] : "");
        int count;
        switch (sUriMatcher.match(uri)) {
            case ITEM_ALL:
                count = databaseHelper.getWritableDatabase().delete(
                        Step.TABLE, selection, selectionArgs);
//                Log2.d(TAG, "delete all: count[%d]", count);
                break;
            case ITEM_SINGLE:
                count = databaseHelper.getWritableDatabase().delete(
                        Step.TABLE,
                        Step.KEY_ID + " = " + uri.getPathSegments().get(1) +
                                (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ")"),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (count > 0)
            contentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log2.d(TAG, "update %s", values);
        int count;
        switch (sUriMatcher.match(uri)) {
            case ITEM_ALL:
                count = databaseHelper.getWritableDatabase().update(
                        Step.TABLE, values, selection, selectionArgs);
                break;
            case ITEM_SINGLE:
                count = databaseHelper.getWritableDatabase().update(
                        Step.TABLE, values,
                        Step.KEY_ID + " = " + uri.getPathSegments().get(1) +
                                (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ")"),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        contentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log2.d(TAG, "bulkInsert: %s, size[%d]", uri, values.length);
        String table;
        switch (sUriMatcher.match(uri)) {
            case ITEM_ALL:
            case ITEM_SINGLE:
                table = Step.TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int count = values.length;
            for (int i = 0; i < count; i++) {
                if (db.insert(table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return values.length;
    }

    public static class Step {
        public static final Uri CONTENT_URL = Uri.parse("content://com.wuyz.runner.StepProvider/step");
        public static final String TABLE = "step";

        public static final String KEY_ID = "_id";
        public static final String KEY_TIME = "time";
        public static final String KEY_STEP = "step";

        public static final int INDEX_ID = 0;
        public static final int INDEX_TIME = 1;
        public static final int INDEX_STEP = 2;

        public static final String[] COLUMNS = new String[]{
                KEY_ID,
                KEY_TIME,
                KEY_STEP
        };

        public static void saveStep(Context context, int step, long time) {
            Log2.d(TAG, "saveStep %d %s", step, Utils.dateFormat.format(time));
            try (Cursor cursor = context.getContentResolver().query(CONTENT_URL,
                    COLUMNS,
                    String.format("%s<?", KEY_TIME),
                    new String[]{String.valueOf(time)},
                    KEY_TIME + " desc limit 1")) {
                if (cursor != null && cursor.moveToFirst() && cursor.getInt(INDEX_STEP) == step) {
                    Log2.d(TAG, "find same record, ignore it");
                    return;
                }
            }

            ContentValues values = new ContentValues(2);
            values.put(Step.KEY_TIME, time);
            values.put(Step.KEY_STEP, step);
            Uri uri = context.getContentResolver().insert(CONTENT_URL, values);
            if (uri == null) {
                Log2.e(TAG, "saveStep return null");
                return;
            }

            long todayTime = Utils.getDayZeroTime(time);
            long tomorrowTime = todayTime + Utils.DAY_SECONDS;

            // delete the useless data
            try (Cursor cursor = context.getContentResolver().query(CONTENT_URL,
                    COLUMNS,
                    String.format("%1$s>=? and %1$s<?", KEY_TIME),
                    new String[]{String.valueOf(todayTime), String.valueOf(tomorrowTime)},
                    KEY_TIME + " asc")) {
                if (cursor != null) {
                    final int n = cursor.getCount();
                    if (n <= 2)
                        return;
                    long[] ids = new long[n];
                    int i = 0;
                    while (cursor.moveToNext()) {
                        if (i > 1 && i < n) {
                            ids[i] = cursor.getLong(INDEX_ID);
                            Log2.d(TAG, "delete data at %s", Utils.dateFormat.format(cursor.getLong(INDEX_TIME)));
                        } else {
                            ids[i] = -1;
                        }
                        i++;
                    }
                    cursor.close();

                    for (long id : ids) {
                        if (id > -1) {
                            context.getContentResolver().delete(CONTENT_URL,
                                    KEY_ID + "=" + id, null);
                        }
                    }
                }
            }
        }

        public static List<StepInfo> getStepListByDay(Context context) {
            List<StepInfo> list = null;
            try (Cursor cursor = context.getContentResolver().query(CONTENT_URL,
                    COLUMNS,
                    null, null, KEY_TIME + " desc")) {
                if (cursor != null) {
                    int n = cursor.getCount();
                    if (n <= 0)
                        return list;
                    list = new ArrayList<>(n);
                    long[] days = new long[n];
                    int len = 0;

                    while (cursor.moveToNext()) {
                        long time = cursor.getLong(INDEX_TIME);
                        int curStep = cursor.getInt(INDEX_STEP);
                        Log2.d(TAG, "database: %s %d", Utils.dateFormat.format(time), curStep);
                        long dayTime = Utils.getDayZeroTime(time);

                        boolean find = false;
                        for (int i = 0; i < len; i++) {
                            if (days[i] == dayTime) {
                                find = true;
                                break;
                            }
                        }

                        if (find) {
                            continue;
                        }
                        days[len++] = dayTime;
                        int lastStep = 0;
                        long lastTime = time;
                        try (Cursor cursor2 = context.getContentResolver().query(CONTENT_URL,
                                COLUMNS,
                                KEY_TIME + "<?",
                                new String[]{String.valueOf(time)},
                                KEY_TIME + " desc limit 1")) {
                            if (cursor2 != null && cursor2.moveToFirst()) {
                                lastStep = cursor2.getInt(INDEX_STEP);
                                lastTime = cursor2.getLong(INDEX_TIME);
                                if (Utils.getDayZeroTime(lastTime) != dayTime)
                                    lastTime = time;
                            }
                        }
                        int step = curStep - lastStep;
                        StepInfo stepInfo = new StepInfo(lastTime, time, step);
//                    Log2.d(TAG, "%s", stepInfo);
                        list.add(stepInfo);
                    }
                    cursor.close();
                }
            }
            return list;
        }

        public static int deleteBeforeTime(Context context, long time) {
            Log2.d(TAG, "deleteBeforeTime %s", Utils.dateFormat.format(time));
            return context.getContentResolver().delete(CONTENT_URL,
                    KEY_TIME + "<?", new String[]{String.valueOf(time)});
        }

        public static int deleteAfterTime(Context context, long time) {
            Log2.d(TAG, "deleteAfterTime %s", Utils.dateFormat.format(time));
            return context.getContentResolver().delete(CONTENT_URL,
                    KEY_TIME + ">?", new String[]{String.valueOf(time)});
        }
    }

    public class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE = "step.db";
        private static final int VERSION = 4;

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(String.format("create table if not exists %s(%s integer primary key autoincrement, " +
                            "%s integer default 0, %s integer default 0);",
                    Step.TABLE, Step.KEY_ID,
                    Step.KEY_TIME, Step.KEY_STEP));
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log2.d(TAG, "onUpgrade oldVersion = " + oldVersion + ", newVersion = " + newVersion);
            db.execSQL("drop table if exists " + Step.TABLE);
            onCreate(db);
        }
    }
}
