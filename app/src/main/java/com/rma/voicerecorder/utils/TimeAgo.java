package com.rma.voicerecorder.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    private String language;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public TimeAgo(String language) {
        this.language = language;
    }

    public String getTimeAgo(long duration) {
        Date now = new Date();
        long s = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long m = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long h = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long d = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        boolean isToday = false;
        if (dateFormat.format(now).equals(dateFormat.format(new Date(duration)))) {
            isToday = true;
        }

        if (s < 60) {
            if (language.equals("hr")) return "upravo sada";
            else return "just now";
        } else if (m >= 1 && m < 60) {
            if (language.equals("hr")) return "prije " + m + " min";
            else return m + " minutes ago";
        } else if (isToday) {
            return timeFormat.format(duration);
        } else {
            return dateFormat.format(duration);
        }

/*        if(s < 60){
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
        }*/
    }
}
