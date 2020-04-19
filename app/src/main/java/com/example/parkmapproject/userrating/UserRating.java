package com.example.parkmapproject.userrating;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserRating implements Serializable {
    private String username;
    private String comment;
    private int rating;
    private long timestamp;
    private String relativeTime;

    public UserRating() {}
    public UserRating(String username, String comment, int rating, long timestamp) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
        setRelativeTime();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRelativeTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        ZoneId zone = ZoneId.systemDefault();
        long unixTimestamp = currentTime.atZone(zone).toEpochSecond();
        long dif = unixTimestamp - this.timestamp;
        String relative = new String();
        final long MIN = 60, HOUR = 3600, DAY = 86400, WEEK = 604800;
        if (dif < MIN)
            relative = String.valueOf(dif) + "s";
        else if (dif < HOUR)
            relative = String.valueOf((int)(dif / MIN)) + "m";
        else if (dif < DAY)
            relative = String.valueOf((int)(dif / HOUR)) + "h";
        else if (dif < WEEK)
            relative = String.valueOf((int)(dif / DAY)) + "d";
        else
            relative = String.valueOf((int)(dif / WEEK)) + "w";
        this.relativeTime = relative;
    }

    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRelativeTime() {
        return relativeTime;
    }

}