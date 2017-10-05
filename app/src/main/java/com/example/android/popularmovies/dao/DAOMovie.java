package com.example.android.popularmovies.dao;

import com.example.android.popularmovies.interfaces.IDAOMovie;
import com.example.android.popularmovies.models.PopularMovie;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.RealmUtils;

import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class DAOMovie implements IDAOMovie{
    @Override
    public void insertMovie(PopularMovie movie) {
        RealmUtils.insertWithTransaction(movie);
    }

    @Override
    public PopularMovie getMovieById(int moviePosition) {
        return RealmUtils.appRealm().where(PopularMovie.class).equalTo("id",moviePosition).findFirst();
    }

    @Override
    public RealmResults<PopularMovie> getMovieList(String typeOfQuery) {
        if(typeOfQuery.equals(ConstantsUtils.POPULAR_QUERY))
            return RealmUtils.appRealm().where(PopularMovie.class).findAllSorted("popularity", Sort.DESCENDING);
        else if(typeOfQuery.equals(ConstantsUtils.TOP_RATED_QUERY))
            return RealmUtils.appRealm().where(PopularMovie.class).findAllSorted("vote_average", Sort.DESCENDING);
        return null;
    }
}
