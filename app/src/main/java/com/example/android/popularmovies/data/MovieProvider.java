package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

public class MovieProvider extends ContentProvider {

    private static final int MOVIE = 100;
    private static final int MOVIE_ID = 101;
    private static final int FAV_MOVIE = 102;
    private static final int FAV_MOVIE_ID = 103;

    private static final UriMatcher thisUriMatcher = buildUriMatcher();
    private MovieDatabaseHelper movieDatabaseHelper;
    private FavMovieDatabaseHelper favMovieDatabaseHelper;

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIE);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_ID);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAV_MOVIES, FAV_MOVIE);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAV_MOVIES+ "/#", FAV_MOVIE_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        movieDatabaseHelper = new MovieDatabaseHelper(context);
        favMovieDatabaseHelper = new FavMovieDatabaseHelper(context);
        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int match = thisUriMatcher.match(uri);

        Uri uriToReturn;
        switch (match) {
            case MOVIE: {
                long _id = getMovieDb().insert(MovieContract.MovieEntry.MOVIE_TABLE, null, values);
                if ( _id > 0 )
                    uriToReturn = MovieContract.MovieEntry.buildMovieUri(_id, MovieContract.PATH_MOVIES);
                else
                    throw new android.database.SQLException("Row insert failed into "+uri);
                break;
            }
            case FAV_MOVIE: {
                long _id = getFavoriteDb().insert(MovieContract.MovieEntry.FAV_MOVIE_TABLE, null, values);
                Log.e("MovieProvider", "_id: "+_id);
                if (_id > 0)
                    uriToReturn = MovieContract.MovieEntry.buildMovieUri(_id, MovieContract.PATH_FAV_MOVIES);
                else {
                    throw new android.database.SQLException("Row insert failed into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return uriToReturn;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.e("MovieProvider", "query");
        Log.e("MovieProvider", "uri: "+uri.toString());
        Cursor cursorToReturn;
        SQLiteDatabase db;// = movieDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (thisUriMatcher.match(uri)) {

            case MOVIE: {
                Log.e("MovieProvider", "query case MOVIE");
                db = movieDatabaseHelper.getReadableDatabase();
                builder.setTables(MovieContract.MovieEntry.MOVIE_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = MovieContract.MovieEntry.SORT_ORDER_DEFAULT;
                break;
            }

            case MOVIE_ID: {
                Log.e("MovieProvider", "query case MOVIE_ID");
                db = movieDatabaseHelper.getReadableDatabase();
                builder.setTables(MovieContract.MovieEntry.MOVIE_TABLE);
                builder.appendWhere(MovieContract.MovieEntry.COLUMN_MOVIE_ID +" = "+uri.getLastPathSegment());
                break;
            }

            case FAV_MOVIE: {
                Log.e("MovieProvider", "query case FAV_MOVIE");
                db = favMovieDatabaseHelper.getReadableDatabase();
                builder.setTables(MovieContract.MovieEntry.FAV_MOVIE_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = MovieContract.MovieEntry.SORT_ORDER_DEFAULT;
                break;
            }

            case FAV_MOVIE_ID: {
                Log.e("MovieProvider", "query case FAV_MOVIE_ID");
                db = favMovieDatabaseHelper.getReadableDatabase();
                builder.setTables(MovieContract.MovieEntry.FAV_MOVIE_TABLE);
                builder.appendWhere(MovieContract.MovieEntry.COLUMN_MOVIE_ID +" = "+uri.getLastPathSegment());
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursorToReturn = builder.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        cursorToReturn.setNotificationUri(getContext().getContentResolver(), uri);
        Log.e("Provider",cursorToReturn.getColumnCount()+"");
        Log.e("Provider",cursorToReturn.toString()+"");
        return cursorToReturn;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.e("MovieProvider", "delete("+uri+")");
        final SQLiteDatabase db;// = getMovieDb();
        int deletedRows;

        switch (thisUriMatcher.match(uri)) {
            case MOVIE:
                Log.e("MovieProvider", "delete case MOVIE");
                db = getMovieDb();
                deletedRows = db.delete(MovieContract.MovieEntry.MOVIE_TABLE, selection, selectionArgs);
                break;
            case FAV_MOVIE:
                Log.e("MovieProvider", "delete case FAV_MOVIE");
                db = getFavoriteDb();
                deletedRows = db.delete(MovieContract.MovieEntry.FAV_MOVIE_TABLE, selection, selectionArgs);
                break;
            case FAV_MOVIE_ID:
                Log.e("MovieProvider", "delete case FAV_MOVIE_ID");
                db = getFavoriteDb();
                deletedRows = db.delete(MovieContract.MovieEntry.FAV_MOVIE_TABLE, MovieContract.MovieEntry.COLUMN_MOVIE_ID +" = "+uri.getLastPathSegment(), selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || deletedRows != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        Log.e("MovieProvider", "Deleted: "+deletedRows+" rows");
        return deletedRows;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = getMovieDb();
        final int match = thisUriMatcher.match(uri);
        int updatedRows = 0;

        switch (match) {
            case MOVIE:
                updatedRows = db.update(MovieContract.MovieEntry.MOVIE_TABLE,values,selection,selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (updatedRows != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updatedRows;
    }

    private SQLiteDatabase getMovieDb() {
        return movieDatabaseHelper.getWritableDatabase();
    }

    private SQLiteDatabase getFavoriteDb() {
        return favMovieDatabaseHelper.getWritableDatabase();
    }


    @Override
    public String getType(@NonNull Uri uri) {
        switch (thisUriMatcher.match(uri)) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case FAV_MOVIE:
                return MovieContract.MovieEntry.CONTENT_FAV_TYPE;
            case FAV_MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_FAV_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

}
