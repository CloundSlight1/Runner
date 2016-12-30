package com.wuyz.runner;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by wuyz on 2016/12/29.
 * StepAdapter
 */

public class StepAdapter extends CursorAdapter {

    private static final String TAG = "StepAdapter";

    public StepAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        Log2.d(TAG, "newView %d", cursor.getLong(0));
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.dayTime = (TextView) view.findViewById(R.id.day_time_text);
        holder.startTime = (TextView) view.findViewById(R.id.start_time_text);
        holder.endTime = (TextView) view.findViewById(R.id.end_time_text);
        holder.step = (TextView) view.findViewById(R.id.step_text);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        Log2.d(TAG, "bindView");
        if (cursor != null) {
            long dayTime = cursor.getLong(cursor.getColumnIndex(StepProvider.Step.KEY_DAY_TIME));
            long startTime = cursor.getLong(cursor.getColumnIndex(StepProvider.Step.KEY_START_TIME));
            long endTime = cursor.getLong(cursor.getColumnIndex(StepProvider.Step.KEY_END_TIME));
            int step = cursor.getInt(cursor.getColumnIndex(StepProvider.Step.KEY_STEP));
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.dayTime.setText(StringUtils.dateFormat2.format(dayTime));
            holder.startTime.setText(StringUtils.dateFormat5.format(startTime));
            holder.endTime.setText(StringUtils.dateFormat5.format(endTime));
            holder.step.setText(step + "步");
        }
    }

    private class ViewHolder {
        TextView dayTime;
        TextView startTime;
        TextView endTime;
        TextView step;
    }
}
