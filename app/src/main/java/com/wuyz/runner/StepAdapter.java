package com.wuyz.runner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
        holder.time = (TextView) view.findViewById(R.id.day_time_text);
        holder.step = (TextView) view.findViewById(R.id.step_text);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
//        Log2.d(TAG, "bindView");
        if (cursor != null) {
            long time = cursor.getLong(cursor.getColumnIndex(StepProvider.Step.KEY_TIME));
            int step = cursor.getInt(cursor.getColumnIndex(StepProvider.Step.KEY_STEP));
            int startStep = cursor.getInt(cursor.getColumnIndex(StepProvider.Step.KEY_START_STEP));
            ViewHolder holder = (ViewHolder) view.getTag();
            long todayTime = Utils.getDayZeroTime(System.currentTimeMillis());
            if (time >= todayTime && time < (todayTime + Utils.DAY_SECONDS))
                holder.time.setText(Utils.dateFormat5.format(time));
            else
                holder.time.setText(Utils.dateFormat2.format(time));
            holder.step.setText((step - startStep) + "步");

            final long id = cursor.getLong(cursor.getColumnIndex(StepProvider.Step.KEY_ID));
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("是否删除该记录？");
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.getContentResolver().delete(StepProvider.Step.CONTENT_URL,
                                    StepProvider.Step.KEY_ID + "=" + id, null);
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }
    }

    private class ViewHolder {
        TextView time;
        TextView step;
    }
}
