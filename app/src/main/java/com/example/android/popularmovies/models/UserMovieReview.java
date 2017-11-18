package com.example.android.popularmovies.models;

/**
 * Created by helpingwithcode on 12/11/17.
 */

public class UserMovieReview {

    private String user;
    private String review;

    public UserMovieReview(String user, String review){
        this.user = user;
        this.review = review;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}