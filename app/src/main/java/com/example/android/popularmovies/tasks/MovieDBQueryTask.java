package com.example.android.popularmovies.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.utils.BroadcastUtils;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.GsonUtil;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.example.android.popularmovies.utils.RealmUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Created by helpingwithcode on 01/10/17.
 */

public class MovieDBQueryTask extends AsyncTask<URL, Void, String> {
    private final Context appContext;

    public MovieDBQueryTask(Context thisContext){
        this.appContext = thisContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_INIT_TASK);
    }

    @Override
    protected String doInBackground(URL... params) {
        URL urlToGet = params[0];
        String results = null;
        try {
            RealmUtils.resetMovies();
            results = NetworkUtils.getResponseFromHttpUrl(urlToGet);
            GsonUtil.parseServerResponse(results,appContext);
        }
        catch (IOException e) {
            log("Exception thrown on AsyncTask\nMessage: "+e.getLocalizedMessage());
        }
        return results;
    }

    @Override
    protected void onPostExecute(String serverResponse) {
        super.onPostExecute(serverResponse);
        if(serverResponse == null)
            BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_ERROR_TASK);
    }

    private void log(String s){
        Log.e("TASK", s);
    }
}
