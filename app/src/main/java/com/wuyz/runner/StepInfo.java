package com.wuyz.runner;

/**
 * Created by wuyz on 2016/12/29.
 * StepInfo
 */

public class StepInfo {

    private long dayTime;
    private long startTime;
    private long endTime;
    private int step;

    public StepInfo(long dayTime, long startTime, long endTime, int step) {
        this.dayTime = dayTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.step = step;
    }

    public long getDayTime() {
        return dayTime;
    }

    public void setDayTime(long dayTime) {
        this.dayTime = dayTime;
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
}
