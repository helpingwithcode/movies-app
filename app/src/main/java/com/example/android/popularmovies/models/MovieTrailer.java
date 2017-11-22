package com.example.android.popularmovies.models;

/**
 * Created by helpingwithcode on 12/11/17.
 */

public class MovieTrailer {


    private String id;
    private String key;
    private String name;

    public MovieTrailer(String trailerId, String trailerName, String trailerKey) {
        setId(trailerId);
        setName(trailerName);
        setKey(trailerKey);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}