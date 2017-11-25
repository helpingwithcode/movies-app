package com.example.android.popularmovies.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.activities.MovieDetailsActivity;

/**
 * Created by helpingwithcode on 11/11/17.
 */

public class VolleyUtils {
    public static void getMovies(final Context context, String query){
        String urlPath = NetworkUtils.returnMovieUrl(query, context.getString(R.string.TMDB_API_TOKEN)).toString();
        final RequestQueue requestFromServer = Volley.newRequestQueue(context);
        final StringRequest toSend = new StringRequest(Request.Method.POST, urlPath, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ObjectUtil.parseServerResponse(response,context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_ERROR_TASK);
            }
        }){};

        requestFromServer.add(toSend);
    }

    public static void getMovieRequest(final Context context, int movieId, final String query){
        String urlPath = NetworkUtils.returnMovieRequestUrl(movieId, query, context.getString(R.string.TMDB_API_TOKEN)).toString();
        final RequestQueue requestFromServer = Volley.newRequestQueue(context);
        final StringRequest toSend = new StringRequest(Request.Method.GET, urlPath, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(query.equals(ConstantsUtils.VIDEOS_QUERY))
                    MovieDetailsActivity.populateTrailerView(response);
                else if(query.equals(ConstantsUtils.REVIEWS_QUERY))
                    MovieDetailsActivity.populateReviewView(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String intentName = "";
                if(query.equals(ConstantsUtils.VIDEOS_QUERY))
                    intentName = ConstantsUtils.INTENT_ERROR_VIDEO_TASK;
                else if(query.equals(ConstantsUtils.REVIEWS_QUERY))
                    intentName = ConstantsUtils.INTENT_ERROR_REVIEW_TASK;
                BroadcastUtils.sendBroadcast(context, intentName);
            }
        });

        requestFromServer.add(toSend);
    }
}
