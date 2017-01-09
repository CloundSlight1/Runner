package com.wuyz.runner;

/**
 * Created by wuyz on 2016/12/29.
 * StepInfo
 */

public class StepInfo {

    private long id;
    private long time;
    private int step;
    private long startTime;
    private int startStep;

    public StepInfo(long id, long time, int step, long startTime, int startStep) {
        this.id = id;
        this.time = time;
        this.step = step;
        this.startTime = startTime;
        this.startStep = startStep;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getStartStep() {
        return startStep;
    }

    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }
}
