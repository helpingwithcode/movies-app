package com.example.android.popularmovies.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.popularmovies.activities.MovieDetailsActivity;
import com.example.android.popularmovies.activities.ReviewActivity;

/**
 * Created by helpingwithcode on 11/11/17.
 */

public class VolleyUtils {
    public static void getMovies(final Context thisContext, String query){
        final RequestQueue requestFromServer = Volley.newRequestQueue(thisContext);
        final StringRequest toSend = new StringRequest(Request.Method.POST, NetworkUtils.returnMovieUrl(query).toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ObjectUtil.parseServerResponse(response,thisContext);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                BroadcastUtils.sendBroadcast(thisContext, ConstantsUtils.INTENT_ERROR_TASK);
            }
        }){};

        requestFromServer.add(toSend);
    }

    public static void getMovieRequest(final Context thisContext, int movieId, final String query){
        final RequestQueue requestFromServer = Volley.newRequestQueue(thisContext);
        final StringRequest toSend = new StringRequest(Request.Method.GET, NetworkUtils.returnMovieRequestUrl(movieId,query).toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(query.equals(ConstantsUtils.VIDEOS_QUERY))
                    MovieDetailsActivity.createTrailerView(response);
                else if(query.equals(ConstantsUtils.REVIEWS_QUERY))
                    ReviewActivity.populateReviewView(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                BroadcastUtils.sendBroadcast(thisContext, ConstantsUtils.INTENT_ERROR_TASK);
            }
        });

        requestFromServer.add(toSend);
    }
}
