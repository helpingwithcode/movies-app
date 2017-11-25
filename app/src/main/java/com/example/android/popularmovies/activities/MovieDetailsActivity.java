package com.example.android.popularmovies.activities;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.ReviewAdapter;
import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.models.Movie;
import com.example.android.popularmovies.models.MovieTrailer;
import com.example.android.popularmovies.models.UserMovieReview;
import com.example.android.popularmovies.utils.BroadcastUtils;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.ObjectUtil;
import com.example.android.popularmovies.utils.Utils;
import com.example.android.popularmovies.utils.VolleyUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.android.popularmovies.utils.ConstantsUtils.REVIEWS_QUERY;
import static com.example.android.popularmovies.utils.ConstantsUtils.VIDEOS_QUERY;

public class MovieDetailsActivity extends AppCompatActivity implements TrailerAdapter.OnTrailerClick{
    @BindView(R.id.iv_poster)
    ImageView moviePosterIv;
    @BindView(R.id.tv_title)
    TextView movieTitleTv;
    @BindView(R.id.tv_plot)
    TextView moviePlotTv;
    @BindView(R.id.tv_release_date)
    TextView movieReleaseDateTv;
    @BindView(R.id.tv_ratings)
    TextView movieRatings;
    @BindView(R.id.rv_reviews)
    RecyclerView reviewsRv;
    @BindView(R.id.rv_trailers)
    RecyclerView trailersRv;
    @BindView(R.id.tv_review_title)
    TextView reviewTitleTv;
    @BindView(R.id.tv_trailers_title)
    TextView trailerTitleTv;
    @BindView(R.id.tv_no_reviews)
    TextView noReviewsTv;
    @BindView(R.id.tv_no_trailers)
    TextView noTrailersTv;
    @BindView(R.id.sv_holder)
    ScrollView holderSv;

    private int thisMovieId;
    private boolean isFavorited;
    private Menu menu;
    private Toast mToast;
    private ContentResolver movieResolver;
    private static Context context;
    private String movieTitle;
    private Movie thisMovie;
    private String currentQuery;
    private static ArrayList<UserMovieReview> reviewList = new ArrayList<>();
    private static ArrayList<MovieTrailer> trailerList = new ArrayList<>();
    private int loadedSoFar = 0;
    private int loadTotalCount = 2;
    private int scrollViewPosition;
    private final BroadcastReceiver reviewsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcastAction = intent.getAction();
            log("Received: "+broadcastAction);
            switch (broadcastAction) {
                case ConstantsUtils.INTENT_FINISH_FETCH_TRAILER:
                    setRecyclerView(false);
                    checkLoadingStatus();
                    break;
                case ConstantsUtils.INTENT_FINISH_FETCH_REVIEW:
                    setRecyclerView(true);
                    checkLoadingStatus();
                    break;
                case ConstantsUtils.INTENT_ERROR_VIDEO_TASK:
                case ConstantsUtils.INTENT_ERROR_REVIEW_TASK:
                    showErrorTextView(broadcastAction);
                    break;

            }
        }
    };

    private void showErrorTextView(String broadcastAction) {
        if(broadcastAction.equals(ConstantsUtils.INTENT_ERROR_REVIEW_TASK))
            showNoReviews();
        else
            showNoTrailers();
    }

    private void checkLoadingStatus() {
        loadedSoFar++;
        if(loadedSoFar == loadTotalCount)
            holderSv.scrollTo(0,scrollViewPosition);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        movieResolver = getApplicationContext().getContentResolver();
        checkSavedInstance(savedInstanceState);
        context = getApplicationContext();
        fetchTrailerAndReview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(reviewsBroadcastReceiver, getReviewIntentFilters());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(reviewsBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putInt(getString(R.string.key_movie), thisMovieId);
        savedState.putString(getString(R.string.key_current_query), currentQuery);
        savedState.putInt(getString(R.string.key_scrollview_position), holderSv.getScrollY());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean hasSavedInstance = savedInstanceState != null;
        thisMovieId = (hasSavedInstance) ? savedInstanceState.getInt(getString(R.string.key_movie)) : getMovieIdFromIntent();
        currentQuery = savedInstanceState.getString(getString(R.string.key_current_query));
        scrollViewPosition = savedInstanceState.getInt(getString(R.string.key_scrollview_position));
        populateLayoutWithMovieInfo(thisMovieId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.fav_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getFavoriteStatus(thisMovieId);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_mark_as_favorite)
            changeFavoriteStatus();
        return super.onOptionsItemSelected(item);
    }

    private IntentFilter getReviewIntentFilters() {
        IntentFilter filters = new IntentFilter();
        filters.addAction(ConstantsUtils.INTENT_FINISH_FETCH_REVIEW);
        filters.addAction(ConstantsUtils.INTENT_FINISH_FETCH_TRAILER);
        filters.addAction(ConstantsUtils.INTENT_ERROR_VIDEO_TASK);
        filters.addAction(ConstantsUtils.INTENT_ERROR_REVIEW_TASK);
        return filters;
    }

    private void changeFavoriteStatus() {
        if(!isFavorited)
            ObjectUtil.copyMovie(thisMovie,context);
        else
            movieResolver.delete(MovieContract.MovieEntry.buildMovieUri(thisMovieId, MovieContract.MovieEntry.FAV_MOVIE_TABLE),null,null);
        isFavorited = !isFavorited;
        showToastMessage();
    }

    private void showToastMessage() {
        setFavoriteMenuStatus(isFavorited);
        String toastMessage = getString((isFavorited) ? R.string.movie_detail_marked_favorite : R.string.movie_detail_unmarked_favorite);
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void checkSavedInstance(Bundle savedInstanceState) {
        boolean hasSavedInstanceState = (savedInstanceState != null);
        thisMovieId = (hasSavedInstanceState) ? savedInstanceState.getInt(getString(R.string.key_movie)) : getMovieIdFromIntent();
        currentQuery = (hasSavedInstanceState) ? savedInstanceState.getString(getString(R.string.key_current_query)) : getCurrentQueryFromIntent();
        try {
            populateLayoutWithMovieInfo(thisMovieId);
        }
        catch (Exception e){
            log("can't populate layout with movie info from here");
        }
    }

    private String getCurrentQueryFromIntent() {
        Bundle intents = getIntent().getExtras();
        return (intents != null) ? intents.getString(getString(R.string.key_current_query)) : "";
    }

    private int getMovieIdFromIntent() {
        Bundle intents = getIntent().getExtras();
        return (intents != null) ? intents.getInt(getString(R.string.key_movie)) : 0;
    }

    private void populateLayoutWithMovieInfo(int movieId) {
        Cursor movieCursor = movieResolver.query(MovieContract.MovieEntry.buildMovieUri(movieId, (currentQuery.equals(ConstantsUtils.FAVORITES_QUERY)) ? MovieContract.MovieEntry.FAV_MOVIE_TABLE:MovieContract.MovieEntry.MOVIE_TABLE), null, null, null, null);

        movieCursor.moveToFirst();
        thisMovie = ObjectUtil.createMovieFromCursor(movieCursor,movieId);
        movieTitle = thisMovie.getTitle();
        moviePlotTv.setText(thisMovie.getOverview());
        thisMovieId = movieId;
        movieTitleTv.setText(movieTitle);
        movieReleaseDateTv.setText(String.format(getString(R.string.release_date), thisMovie.getReleaseDate()));
        movieRatings.setText(String.format(getString(R.string.vote_average), thisMovie.getVoteAverage()));
        Picasso.with(getApplicationContext())
                .load(Utils.getImagePath(thisMovie.getPoster(),ConstantsUtils.POSTER_SIZE_185))
                .fit()
                .into(moviePosterIv);

        movieCursor.close();
    }

    private void getFavoriteStatus(int movieId) {
        boolean isMovieFavorited = false;
        Cursor favMovieCursor = movieResolver.query(MovieContract.MovieEntry.buildMovieUri(movieId, MovieContract.MovieEntry.FAV_MOVIE_TABLE), null, null, null, null);
        try {
            int cursorMovieId = favMovieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            favMovieCursor.moveToFirst();
            isMovieFavorited = (Integer.valueOf(favMovieCursor.getString(cursorMovieId)) == movieId);
        }
        catch (Exception e){
            log("getFavoriteStatus exception: "+e.getLocalizedMessage());
        }
        setFavoriteMenuStatus(isMovieFavorited);
        favMovieCursor.close();
    }

    private void setFavoriteMenuStatus(boolean favorite) {
        isFavorited = favorite;
        MenuItem favItem = getFavoriteItem();
        favItem.setIcon((favorite) ? R.mipmap.ic_favorite_white_24dp : R.mipmap.ic_favorite_border_white_24dp);
    }

    private MenuItem getFavoriteItem() {
        return menu.findItem(R.id.item_mark_as_favorite);
    }

    private void setRecyclerView(boolean isReview) {
        if (isReview) {
            if (reviewList.size() > 0) {
                showRecyclerView(reviewsRv);
                reviewsRv.setAdapter(new ReviewAdapter(context, reviewList));
            }
            else {
                showNoReviews();
            }
        } else {
            if(trailerList.size() > 0) {
                showRecyclerView(trailersRv);
                trailersRv.setAdapter(new TrailerAdapter(context, trailerList, this));
            }
            else{
                showNoTrailers();
            }

        }
    }

    private void showNoTrailers() {
        noTrailersTv.setVisibility(View.VISIBLE);
        trailersRv.setVisibility(View.GONE);
    }

    private void showNoReviews() {
        noReviewsTv.setVisibility(View.VISIBLE);
        reviewsRv.setVisibility(View.GONE);
    }

    private void showRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    public static void populateReviewView(final String r) {
        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_REVIEW_PARSE_START);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                reviewList = new ArrayList();
                try {
                    JSONObject response = new JSONObject(r);
                    JSONArray result = response.getJSONArray("results");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject review = (JSONObject) result.get(i);
                        String reviewAuthor = review.getString("author");
                        String reviewContent = review.getString("content");
                        UserMovieReview userMovieReview = new UserMovieReview(reviewAuthor, reviewContent);
                        reviewList.add(userMovieReview);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_FINISH_FETCH_REVIEW);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_REVIEW_PARSE_END);
            }
        }.execute();
    }

    public static void populateTrailerView(final String r) {
        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_TRAILER_PARSE_START);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                trailerList = new ArrayList();
                try {
                    JSONObject response = new JSONObject(r);
                    JSONArray result = response.getJSONArray("results");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject review = (JSONObject) result.get(i);
                        String trailerId = review.getString("id");
                        String trailerName = review.getString("name");
                        String trailerKey = review.getString("key");
                        MovieTrailer movieTrailer = new MovieTrailer(trailerId, trailerName, trailerKey);
                        trailerList.add(movieTrailer);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_FINISH_FETCH_TRAILER);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                BroadcastUtils.sendBroadcast(context, ConstantsUtils.INTENT_TRAILER_PARSE_END);
            }
        }.execute();
    }

    public void launchTrailerIntent(String trailerKey) {
        if (!Utils.isOnline(getApplicationContext())) {
            showConnectionToast();
            return;
        }
        try {
            Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKey));
            trailerIntent.putExtra("VIDEO_ID", trailerKey);
            trailerIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(trailerIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, getString(R.string.toast_message_app_not_installed), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void launchTrailer(String trailerKey) {
        launchTrailerIntent(trailerKey);
    }

    private void showConnectionToast() {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, getString(R.string.toast_message_connectivity), Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void fetchTrailerAndReview() {
        VolleyUtils.getMovieRequest(getApplicationContext(), thisMovieId, REVIEWS_QUERY);
        VolleyUtils.getMovieRequest(getApplicationContext(), thisMovieId, VIDEOS_QUERY);
    }

    public void log(String s) {
        Log.e("MoviesDetails", s);
    }
}
