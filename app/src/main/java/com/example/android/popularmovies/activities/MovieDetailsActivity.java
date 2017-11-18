package com.example.android.popularmovies.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.models.Movie;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.ObjectUtil;
import com.example.android.popularmovies.utils.Utils;
import com.example.android.popularmovies.utils.VolleyUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.android.popularmovies.utils.ConstantsUtils.VIDEOS_QUERY;

public class MovieDetailsActivity extends AppCompatActivity {
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
    @BindView(R.id.bt_watch_trailer)
    Button watchTrailerBt;
    @BindView(R.id.bt_reviews)
    Button reviewsBt;

    private int thisMovieId;
    private boolean isFavorited;
    private Menu menu;
    private Toast mToast;
    private ContentResolver movieResolver;
    private static Context context;
    private static Intent trailerIntent;
    private String movieTitle;
    private Movie thisMovie;
    private String currentQuery;

    public static void createTrailerView(String serverResponse) {
        JSONObject response = null;
        try {
            response = new JSONObject(serverResponse);
            JSONArray result = response.getJSONArray("results");
            JSONObject movie = (JSONObject) result.get(0);
            trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + movie.getString("key")));
            trailerIntent.putExtra("VIDEO_ID", movie.getString("key"));
            trailerIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void launchTrailer(){
        if(!Utils.isOnline(getApplicationContext())){
            showConnectionToast();
            return;
        }
        try {
            context.startActivity(trailerIntent);
        } catch (ActivityNotFoundException ex) {
            log("youtube app not installed");
        }

    }

    @OnClick({R.id.bt_watch_trailer,R.id.bt_reviews})
    public void viewClickListener(View iv) {
        if (iv.getId() == R.id.bt_watch_trailer)
            launchTrailer();
        else if(iv.getId() == R.id.bt_reviews)
            startReviewActivity();
    }

    private void startReviewActivity() {
        if(!Utils.isOnline(getApplicationContext())){
            showConnectionToast();
            return;
        }
        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        reviewIntent.putExtra(getString(R.string.key_movie_id), thisMovieId);
        reviewIntent.putExtra(getString(R.string.key_movie_title), movieTitle);
        startActivity(reviewIntent);
    }

    private void showConnectionToast() {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, getString(R.string.toast_message_connectivity), Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        movieResolver = getApplicationContext().getContentResolver();
        checkSavedInstance(savedInstanceState);
        context = getApplicationContext();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putInt(getString(R.string.key_movie), thisMovieId);
        super.onSaveInstanceState(savedState);
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
        populateLayoutWithMovieInfo(thisMovieId);
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
        movieRatings.setText(String.format(getString(R.string.vote_avarage), thisMovie.getVoteAverage()));
        Picasso.with(getApplicationContext())
                .load(Utils.getImagePath(thisMovie.getPoster()))
                .fit()
                .into(moviePosterIv);

        movieCursor.close();

        VolleyUtils.getMovieRequest(getApplicationContext(), thisMovieId, VIDEOS_QUERY);
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

    public void log(String s) {
        Log.e("MoviesDetails", s);
    }
}
