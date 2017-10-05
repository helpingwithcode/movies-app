package com.example.android.popularmovies.utils;

import android.net.Uri;

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


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static URL returnMovieUrl(String typeOfQuery) {
        URL url = null;
        try {
            url = new URL(Uri.parse(ConstantsUtils.MOVIE_DB_BASE_URL+typeOfQuery+"?"+ConstantsUtils.API_KEY).buildUpon().build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
