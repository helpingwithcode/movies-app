package com.example.android.popularmovies.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.popularmovies.models.PopularMovie;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class RealmUtils {

    public static Realm appRealm(){ return Realm.getDefaultInstance(); }

    public static void init(Context context){
            Realm.init(context);
            buildAppReam();
    }

    private static void buildAppReam(){
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder()
                .name("PopularMovieRealm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static void insertWithTransaction(final RealmObject object) {
        Realm thisRealm = null;
        try {
            thisRealm = appRealm();
            thisRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.insertOrUpdate(object);
                }
            });
        } finally {
            if (thisRealm != null)
                thisRealm.close();
        }
    }

    public static void resetMovies() {
        Realm realm = null;
        final RealmResults<PopularMovie> movies;
        try{
            realm = appRealm();
            movies = realm.where(PopularMovie.class).findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    movies.deleteAllFromRealm();
                }
            });
        }
        finally {
            if(realm != null)
                realm.close();
        }
    }
}
