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
        public static final String KEY_DAY_TIME = "dayTime";
        public static final String KEY_START_TIME = "startTime";
        public static final String KEY_END_TIME = "endTime";
        public static final String KEY_STEP = "step";
        public static final String KEY_START_STEP = "startStep";

        public static final String[] COLUMNS = new String[]{
                StepProvider.Step.KEY_ID,
                StepProvider.Step.KEY_DAY_TIME,
                StepProvider.Step.KEY_START_TIME,
                StepProvider.Step.KEY_END_TIME,
                StepProvider.Step.KEY_STEP,
                StepProvider.Step.KEY_START_STEP,
        };
    }

    public class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE = "step.db";
        private static final int VERSION = 2;

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(String.format("create table if not exists %s(%s integer primary key autoincrement, " +
                    "%s integer, %s integer default 0, %s integer, %s integer, %s integer);",
                    Step.TABLE, Step.KEY_ID, Step.KEY_DAY_TIME, Step.KEY_START_TIME,
                    Step.KEY_END_TIME, Step.KEY_STEP, Step.KEY_START_STEP));
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log2.d(TAG, "onUpgrade oldVersion = " + oldVersion + ", newVersion = " + newVersion);
            db.execSQL("drop table if exists " + Step.TABLE);
            onCreate(db);
        }
    }
}
