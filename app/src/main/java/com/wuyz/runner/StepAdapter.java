package com.wuyz.runner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyz on 2016/12/29.
 * StepAdapter
 */

public class StepAdapter extends RecyclerView.Adapter {
    private static final String TAG = "StepAdapter";

    private List<StepInfo> items = new ArrayList<>();
    private Context context;

    public StepAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<StepInfo> items) {
        this.items.clear();
        if (items != null)
            this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder) holder;
        StepInfo data = items.get(position);
        long startTime = data.getStartTime();
        long endTime = data.getEndTime();
        myHolder.date.setText(Utils.getDateDesc(endTime));
        myHolder.startTime.setText(Utils.dateFormat5.format(startTime));
        myHolder.endTime.setText(Utils.dateFormat5.format(endTime));
        myHolder.step.setText(data.getStep() + "步");

//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setMessage("是否删除该记录？");
//                builder.setNegativeButton(android.R.string.cancel, null);
//                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                builder.show();
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView startTime;
        private TextView endTime;
        private TextView step;

        public MyHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date_text);
            startTime = (TextView) itemView.findViewById(R.id.start_time_text);
            endTime = (TextView) itemView.findViewById(R.id.end_time_text);
            step = (TextView) itemView.findViewById(R.id.step_text);
        }
    }

}
