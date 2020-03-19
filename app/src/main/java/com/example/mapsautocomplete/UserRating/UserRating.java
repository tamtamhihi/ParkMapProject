package com.example.mapsautocomplete.UserRating;

import java.io.Serializable;

public class UserRating implements Serializable {
    private String username;
    private String comment;
    private int rating;

    public UserRating(String username, String comment, int rating) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
