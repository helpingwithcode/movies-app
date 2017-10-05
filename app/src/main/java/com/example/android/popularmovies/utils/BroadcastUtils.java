package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by helpingwithcode on 01/10/17.
 */

public class BroadcastUtils {
    public static void sendBroadcast(Context context, String intentName){
        context.sendBroadcast(new Intent(intentName));
    }
}
