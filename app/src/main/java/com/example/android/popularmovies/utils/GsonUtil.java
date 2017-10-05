package com.example.android.popularmovies.utils;

import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies.dao.DAOMovie;
import com.example.android.popularmovies.interfaces.IDAOMovie;
import com.example.android.popularmovies.models.PopularMovie;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class GsonUtil {
    private static void createMovieObject(String jsonMovieString){
        IDAOMovie movieInterface = new DAOMovie();
        PopularMovie movie = new Gson().fromJson(jsonMovieString,PopularMovie.class);
        movieInterface.insertMovie(movie);
    }

    public static void parseServerResponse(String s, Context appContext) {
        try {
            JSONObject response = new JSONObject(s);
            JSONArray result = response.getJSONArray("results");
            for(int i = 0; i < result.length(); i++)
                GsonUtil.createMovieObject(String.valueOf(result.get(i)));
            BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_END_TASK);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
