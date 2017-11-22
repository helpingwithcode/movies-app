package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.android.popularmovies.R;

import java.lang.reflect.Method;

/**
 * Created by helpingwithcode on 30/09/17.
 */

public class Utils {
    public static String getImagePath(String imagePath) {
        return ConstantsUtils.MOVIE_DB_IMAGE_URL+ConstantsUtils.POSTER_SIZE_185+"/"+imagePath;
    }

    public static String getImagePath(String imagePath, String size) {
        Log.e("Utils",ConstantsUtils.MOVIE_DB_IMAGE_URL+size+"/"+imagePath);
        return ConstantsUtils.MOVIE_DB_IMAGE_URL+size+"/"+imagePath;
    }

    public static void showErrorDialog(final Context context) {
        String dialogMessage, dialogTitle, tryAgain;
        dialogMessage = context.getResources().getString(R.string.dialog_error_message);
        dialogTitle = context.getResources().getString(R.string.dialog_error_title);
        tryAgain = context.getResources().getString(R.string.dialog_try_again);

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setCancelable(true)
                .setPositiveButton(tryAgain,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_RERUN_TASK);
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alert = ad.create();
        alert.show();
    }

    public static boolean isOnline(Context thisContext){
        boolean mobileDataEnabled = false;
        boolean wifiEnabled = false;
        ConnectivityManager dataManager = (ConnectivityManager) thisContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiStatus = dataManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        try {
            Class cmClass = Class.forName(dataManager.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataEnabled = dataManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            wifiEnabled = wifiStatus.isConnectedOrConnecting();
        }
        catch(Exception e){

        }
        return (mobileDataEnabled || wifiEnabled);
    }
}
