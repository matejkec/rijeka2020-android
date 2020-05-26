package com.rma.voicerecorder.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    public String getTimeAgo(long duration){
        Date now = new Date();
        long s = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long m = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long h = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long d = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        if(s < 60){
            return "just now";
        } else if (m == 1){
            return "a minute ago";
        } else if (m > 1 && m < 60){
            return m + " minutes ago";
        } else if (h == 1){
            return "a hour ago";
        } else if (h > 1 && h < 24){
            return h + " hours ago";
        } else if (d == 1) {
            return "a day ago";
        } else {
            return d + " days ago";
        }
    }
}
