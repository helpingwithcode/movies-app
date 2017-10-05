package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.android.popularmovies.R;

/**
 * Created by helpingwithcode on 30/09/17.
 */

public class Utils {
    public static String getImagePath(String imagePath) {
        return ConstantsUtils.MOVIE_DB_IMAGE_URL+ConstantsUtils.POSTER_SIZE_185+"/"+imagePath;
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
}
