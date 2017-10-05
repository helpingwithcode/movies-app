package com.example.android.popularmovies.interfaces;

import com.example.android.popularmovies.models.PopularMovie;

import io.realm.RealmResults;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public interface IDAOMovie {
    void insertMovie(PopularMovie movie);
    PopularMovie getMovieById(int movieId);

    RealmResults<PopularMovie> getMovieList(String typeOfQuery);
}
