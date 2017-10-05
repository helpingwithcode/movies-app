package com.example.android.popularmovies.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.popularmovies.adapters.MovieAdapter;
import com.example.android.popularmovies.dao.DAOMovie;
import com.example.android.popularmovies.interfaces.IDAOMovie;
import com.example.android.popularmovies.tasks.MovieDBQueryTask;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.RealmUtils;
import com.example.android.popularmovies.utils.Utils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClick {

    private RecyclerView moviesRv;
    private ProgressBar progressBar;
    private MovieAdapter movieAdapter;
    private IDAOMovie idaoMovie;
    private String currentQuery;

    private final BroadcastReceiver mainActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcastAction = intent.getAction();
            switch (broadcastAction) {
                case ConstantsUtils.INTENT_RERUN_TASK:
                    startTask();
                    break;
                case ConstantsUtils.INTENT_INIT_TASK:
                    showLoadingStatus(true);
                    break;
                case ConstantsUtils.INTENT_END_TASK:
                    showLoadingStatus(false);
                    movieAdapter.setDataBase(idaoMovie.getMovieList(currentQuery));
                    break;
                case ConstantsUtils.INTENT_ERROR_TASK:
                    showLoadingStatus(false);
                    Utils.showErrorDialog(MainActivity.this);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkSavedInstance(savedInstanceState);
        RealmUtils.init(getApplicationContext());
        idaoMovie = new DAOMovie();
        setViews();
        startTask();
        setRecyclerView();
    }

    private void checkSavedInstance(Bundle savedInstanceState) {
        if(savedInstanceState == null)
            currentQuery = ConstantsUtils.POPULAR_QUERY;
        else
            currentQuery = savedInstanceState.getString("currentQuery");
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putString("currentQuery", currentQuery);
        log("saving currentQuery on onSavedInstaceState");
        super.onSaveInstanceState(savedState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setBroadcasts(false);
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
                startTask();
            }
        }
        else if (itemId == R.id.item_query_rated) {
            if (!currentQuery.equals(ConstantsUtils.TOP_RATED_QUERY)) {
                currentQuery = ConstantsUtils.TOP_RATED_QUERY;
                startTask();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBroadcasts(boolean broadcastStatus) {
        log("setting setBroadcasts("+broadcastStatus+");");
        if (broadcastStatus) {
            registerReceiver(mainActivityReceiver, new IntentFilter(ConstantsUtils.INTENT_INIT_TASK));
            registerReceiver(mainActivityReceiver, new IntentFilter(ConstantsUtils.INTENT_END_TASK));
            registerReceiver(mainActivityReceiver, new IntentFilter(ConstantsUtils.INTENT_ERROR_TASK));
            registerReceiver(mainActivityReceiver, new IntentFilter(ConstantsUtils.INTENT_RERUN_TASK));
        }
        else {
            try {
                unregisterReceiver(mainActivityReceiver);
            }
            catch (Exception e) {
                log("Exception thrown when unregistering mainActivityReceiver!\nException: " + e.getLocalizedMessage());
            }
        }
    }

    private void setViews() {
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        moviesRv = (RecyclerView) findViewById(R.id.rv_movies);
    }

    private void startTask() {
        MovieDBQueryTask task = new MovieDBQueryTask(getApplicationContext());
        task.execute(NetworkUtils.returnMovieUrl(currentQuery));
    }

    private void setRecyclerView() {
        //LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        movieAdapter = new MovieAdapter(this, getApplicationContext());

        moviesRv.setLayoutManager(gridLayoutManager);//(layoutManager);
        moviesRv.setHasFixedSize(true);
        moviesRv.setAdapter(movieAdapter);
    }

    @Override
    public void thisClick(int movieId) {
        Intent movieDetailsIntent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
        movieDetailsIntent.putExtra("movieId", movieId);
        startActivity(movieDetailsIntent);
    }

    private void showLoadingStatus(boolean b) {
        progressBar.setVisibility((!b) ? View.INVISIBLE : View.VISIBLE);
        moviesRv.setVisibility((b) ? View.INVISIBLE : View.VISIBLE);
    }

    private void log(String s) {
        Log.e("MainAc", s);
    }
}
