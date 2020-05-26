package com.rma.voicerecorder.utils;

import android.os.Handler;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import java.util.concurrent.TimeUnit;

public class Timer {

    private long timeStart;
    private long autoStopTime;
    private long timeInMs;
    private long timeSwapBuff;
    private long updatedTime;
    private Handler handler;
    private TextView tv;
    private boolean isRunning;

    public Timer(TextView tv){
        this(tv,0);
    }

    public Timer(TextView tv, long autoStopTime) {
        this.tv = tv;
        tv.setText(getTimeString());
        this.autoStopTime = autoStopTime;
        handler = new Handler();
    }

    public void start(){
        if(!isRunning){
            timeStart = SystemClock.uptimeMillis();
            handler.post(updateTimer);
            isRunning = true;
        }
    }

    public void stop(){
        if(isRunning){
            timeSwapBuff += timeInMs;
            handler.removeCallbacksAndMessages(null);
            isRunning = false;
        }
    }

    public void reset(){
        timeStart = 0L;
        timeInMs = 0L;
        updatedTime = 0L;
        timeSwapBuff = 0L;
        tv.setText(getTimeString());
    }

    public void freshStart(){
        if(!isRunning){
            reset();
            start();
        }
    }

    public long getTime(){
        return updatedTime;
    }

    public String getTimeString(){
        long min = TimeUnit.MILLISECONDS.toMinutes(updatedTime) % 60;
        long sec = TimeUnit.MILLISECONDS.toSeconds(updatedTime) % 60;
        long ms = (updatedTime % 1000) / 10;
        return String.format("%02d", min) + ":" + String.format("%02d", sec)
                + ":" + String.format("%02d", ms);
    }

    private Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMs = SystemClock.uptimeMillis() - timeStart;
            updatedTime = timeSwapBuff + timeInMs;
            if (autoStopTime != 0 && updatedTime >= autoStopTime){
                stop();
                updatedTime = autoStopTime;
            } else {
                handler.postDelayed(this, 10);
            }
            tv.setText(getTimeString());
        }
    };

    @NonNull
    @Override
    public String toString() {
        return getTimeString();
    }
}