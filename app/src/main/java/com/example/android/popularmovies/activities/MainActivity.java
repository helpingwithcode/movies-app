package com.example.android.popularmovies.activities;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.android.popularmovies.adapters.MovieAdapter;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.utils.BroadcastUtils;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.Utils;
import com.example.android.popularmovies.utils.VolleyUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClick, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_LOADER_ID = 1;
    @BindView(R.id.pb_loading)
    ProgressBar progressBar;
    @BindView(R.id.rv_movies)
    RecyclerView moviesRv;
    @BindView(R.id.rl_no_fav_holder)
    RelativeLayout noFavHolderRl;
    private MovieAdapter movieAdapter;
    private String currentQuery;
    private ContentResolver movieResolver;

    private final BroadcastReceiver mainActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcastAction = intent.getAction();
            switch (broadcastAction) {
                case ConstantsUtils.INTENT_RERUN_TASK:
                    getMoviesFromApi();
                    break;
                case ConstantsUtils.INTENT_INIT_TASK:
                    showLoadingStatus(true);
                    break;
                case ConstantsUtils.INTENT_END_TASK:
                    showLoadingStatus(false);
                    setAdapterToRecyclerView();
                    break;
                case ConstantsUtils.INTENT_ERROR_TASK:
                    showLoadingStatus(false);
                    Utils.showErrorDialog(MainActivity.this);
                    break;
            }
        }
    };

    private void setAdapterToRecyclerView() {
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        movieResolver = getApplicationContext().getContentResolver();
        checkSavedInstance(savedInstanceState);
        getMoviesFromApi();
        setRecyclerView();
    }

    private void checkSavedInstance(Bundle savedInstanceState) {
        boolean hasSavedInstance = (savedInstanceState != null);
        currentQuery = (hasSavedInstance) ? savedInstanceState.getString("currentQuery") : ConstantsUtils.POPULAR_QUERY;
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putString(getString(R.string.key_current_query), currentQuery);
        super.onSaveInstanceState(savedState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBroadcasts(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBroadcasts(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.query_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.item_query_popular) {
            if (!currentQuery.equals(ConstantsUtils.POPULAR_QUERY)) {
                currentQuery = ConstantsUtils.POPULAR_QUERY;
                getMoviesFromApi();
            }
        } else if (itemId == R.id.item_query_rated) {
            if (!currentQuery.equals(ConstantsUtils.TOP_RATED_QUERY)) {
                currentQuery = ConstantsUtils.TOP_RATED_QUERY;
                getMoviesFromApi();
            }
        } else if (itemId == R.id.item_favorites) {
            if (!currentQuery.equals(ConstantsUtils.FAVORITES_QUERY)) {
                currentQuery = ConstantsUtils.FAVORITES_QUERY;
                BroadcastUtils.sendBroadcast(getApplicationContext(), ConstantsUtils.INTENT_END_TASK);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMoviesFromApi() {
        movieResolver.delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        BroadcastUtils.sendBroadcast(getApplicationContext(), ConstantsUtils.INTENT_INIT_TASK);
        VolleyUtils.getMovies(getApplicationContext(), currentQuery);
    }

    private void setBroadcasts(boolean broadcastStatus) {
        if (broadcastStatus) {
            registerReceiver(mainActivityReceiver, getIntentFilters());
        } else {
            try {
                unregisterReceiver(mainActivityReceiver);
            } catch (Exception e) {
                log("Exception thrown when unregistering mainActivityReceiver!\nException: " + e.getLocalizedMessage());
            }
        }
    }

    private IntentFilter getIntentFilters() {
        IntentFilter intentFilters = new IntentFilter();
        intentFilters.addAction(ConstantsUtils.INTENT_INIT_TASK);
        intentFilters.addAction(ConstantsUtils.INTENT_END_TASK);
        intentFilters.addAction(ConstantsUtils.INTENT_ERROR_TASK);
        intentFilters.addAction(ConstantsUtils.INTENT_RERUN_TASK);
        return intentFilters;
    }

    private void setRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        movieAdapter = new MovieAdapter(this, getApplicationContext());

        moviesRv.setLayoutManager(gridLayoutManager);
        moviesRv.setHasFixedSize(true);
        moviesRv.setAdapter(movieAdapter);
    }

    @Override
    public void thisClick(int movieId) {
        Intent movieDetailsIntent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
        movieDetailsIntent.putExtra(getString(R.string.key_movie), movieId);
        movieDetailsIntent.putExtra(getString(R.string.key_current_query), currentQuery);
        startActivity(movieDetailsIntent);
    }

    private void showLoadingStatus(boolean b) {
        progressBar.setVisibility((!b) ? View.INVISIBLE : View.VISIBLE);
        moviesRv.setVisibility((b) ? View.INVISIBLE : View.VISIBLE);
    }

    private void log(String s) {
        Log.e("MainAc", s);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    Uri queryUri = MovieContract.MovieEntry.CONTENT_URI;
                    String movieSortOrder = (currentQuery.equals(ConstantsUtils.POPULAR_QUERY)) ? MovieContract.MovieEntry.COLUMN_POPULARITY : MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
                    movieSortOrder += " DESC";
                    if (currentQuery.equals(ConstantsUtils.FAVORITES_QUERY)) {
                        queryUri = MovieContract.MovieEntry.FAV_CONTENT_URI;
                        movieSortOrder = null;
                    }
                    return movieResolver.query(queryUri, null, null, null, movieSortOrder);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean showHasNoFavView = (currentQuery == ConstantsUtils.FAVORITES_QUERY && data.getCount() == 0);
        noFavHolderRl.setVisibility((showHasNoFavView) ? View.VISIBLE : View.GONE);
        movieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }
}
