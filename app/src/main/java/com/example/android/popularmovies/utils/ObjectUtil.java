package com.example.android.popularmovies.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_BACKDROP_PATH;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_OVERVIEW;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_POPULARITY;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_POSTER_PATH;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_VOTE_COUNT;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class ObjectUtil {
    @SuppressLint("NewApi")
    public static void parseServerResponse(final String s, final Context appContext) {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    ContentResolver movieContentResolver = appContext.getContentResolver();
                    JSONObject response = new JSONObject(s);
                    JSONArray result = response.getJSONArray("results");
                    ContentValues contentValues = new ContentValues();
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject movie = (JSONObject) result.get(i);
                        contentValues.put(COLUMN_MOVIE_ID, movie.getString(COLUMN_MOVIE_ID));
                        contentValues.put(COLUMN_VOTE_COUNT, movie.getString(COLUMN_VOTE_COUNT));
                        contentValues.put(COLUMN_VOTE_AVERAGE, movie.getString(COLUMN_VOTE_AVERAGE));
                        contentValues.put(COLUMN_TITLE, movie.getString(COLUMN_TITLE));
                        contentValues.put(COLUMN_POPULARITY, movie.getString(COLUMN_POPULARITY));
                        contentValues.put(COLUMN_POSTER_PATH, movie.getString(COLUMN_POSTER_PATH));
                        contentValues.put(COLUMN_ORIGINAL_LANGUAGE, movie.getString(COLUMN_ORIGINAL_LANGUAGE));
                        contentValues.put(COLUMN_ORIGINAL_TITLE, movie.getString(COLUMN_ORIGINAL_TITLE));
                        contentValues.put(COLUMN_BACKDROP_PATH, movie.getString(COLUMN_BACKDROP_PATH));
                        contentValues.put(COLUMN_OVERVIEW, movie.getString(COLUMN_OVERVIEW));
                        contentValues.put(COLUMN_RELEASE_DATE, movie.getString(COLUMN_RELEASE_DATE));
                        log(contentValues.toString());
                        movieContentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
                    }
                    BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_END_TASK);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static void copyMovie(final Movie movie, final Context appContext) {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                ContentResolver movieContentResolver = appContext.getContentResolver();
                ContentValues contentValues = new ContentValues();

                contentValues.put(COLUMN_MOVIE_ID, movie.getMovieId());
                contentValues.put(COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                contentValues.put(COLUMN_TITLE, movie.getTitle());
                contentValues.put(COLUMN_POSTER_PATH, movie.getPoster());
                contentValues.put(COLUMN_OVERVIEW, movie.getOverview());
                contentValues.put(COLUMN_RELEASE_DATE, movie.getReleaseDate());
                contentValues.put(COLUMN_VOTE_COUNT, "");
                contentValues.put(COLUMN_POPULARITY, "");
                contentValues.put(COLUMN_ORIGINAL_LANGUAGE, "");
                contentValues.put(COLUMN_ORIGINAL_TITLE, "");
                contentValues.put(COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                movieContentResolver.insert(MovieContract.MovieEntry.FAV_CONTENT_URI, contentValues);
                return null;
            }
        }.execute();
    }

    public static Movie createMovieFromCursor(Cursor mc, int movieId) {
        int overview = mc.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        int originalTitle = mc.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int releaseDate = mc.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        int voteAverage = mc.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        int posterPath = mc.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        int backdropPath = mc.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH);

        return new Movie(movieId,mc.getString(originalTitle),mc.getString(posterPath), mc.getString(backdropPath),mc.getString(overview),mc.getString(voteAverage),mc.getString(releaseDate));
    }

    private static void log(String s) {
        Log.e("GSONUtil", s);
    }
}
