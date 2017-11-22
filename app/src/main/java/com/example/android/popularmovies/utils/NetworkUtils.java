package com.example.android.popularmovies.utils;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class NetworkUtils {

    public static URL returnMovieUrl(String typeOfQuery, String apiKey) {
        URL url = null;
        try {
            url = new URL(Uri.parse(ConstantsUtils.MOVIE_DB_BASE_URL+typeOfQuery+"?"+ ConstantsUtils.API_KEY_PREF +apiKey).buildUpon().build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL returnMovieRequestUrl(int movieId, String typeOfQuery, String apiKey) {
        URL url = null;
        try {
            url = new URL(Uri.parse(ConstantsUtils.MOVIE_DB_BASE_URL+movieId+"/"+typeOfQuery+"?"+ ConstantsUtils.API_KEY_PREF +apiKey).buildUpon().build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.e("NetworkUtil", url.toString());
        return url;
    }
}
