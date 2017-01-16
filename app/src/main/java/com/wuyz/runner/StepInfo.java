package com.wuyz.runner;

import java.util.Locale;

/**
 * Created by wuyz on 2016/12/29.
 * StepInfo
 */

public class StepInfo {
    private long startTime;
    private long endTime;
    private int step;

    public StepInfo(long startTime, long endTime, int step) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.step = step;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s %s %d",
                Utils.dateFormat.format(startTime),
                Utils.dateFormat.format(endTime),
                step);
    }
}
