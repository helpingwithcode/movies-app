package com.example.android.popularmovies.activities;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.ReviewAdapter;
import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.models.MovieTrailer;
import com.example.android.popularmovies.models.UserMovieReview;
import com.example.android.popularmovies.utils.BroadcastUtils;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.Utils;
import com.example.android.popularmovies.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.android.popularmovies.utils.ConstantsUtils.REVIEWS_QUERY;
import static com.example.android.popularmovies.utils.ConstantsUtils.VIDEOS_QUERY;

public class TrailerAndReviewActivity extends AppCompatActivity implements TrailerAdapter.OnTrailerClick {

    private static final int MAX_TRAILERS = 3;
    private static ArrayList<UserMovieReview> reviewList;
    private static ArrayList<MovieTrailer> trailerList;
    private int movieId;
    private String movieTitle;
    private static Context appContext;

    @BindView(R.id.rv_reviews)
    RecyclerView reviewsRv;
    @BindView(R.id.rv_trailers)
    RecyclerView trailersRv;
    @BindView(R.id.tv_review_title)
    TextView reviewTitleTv;
    @BindView(R.id.tv_trailers_title)
    TextView trailerTitleTv;
    @BindView(R.id.pb_loading)
    ProgressBar loaderPb;
    @BindView(R.id.tv_no_reviews)
    TextView noReviewsTv;
    @BindView(R.id.tv_no_trailers)
    TextView noTrailersTv;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer_review);
        ButterKnife.bind(this);
        appContext = getApplicationContext();
        getExtrasFromIntent();
        fetchTrailerAndReview();
        setViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(reviewsBroadcastReceiver, getReviewIntentFilters());
    }

    private IntentFilter getReviewIntentFilters() {
        IntentFilter filters = new IntentFilter();
        filters.addAction(ConstantsUtils.INTENT_FINISH_FETCH_REVIEW);
        filters.addAction(ConstantsUtils.INTENT_REVIEW_PARSE_END);
        filters.addAction(ConstantsUtils.INTENT_REVIEW_PARSE_START);
        filters.addAction(ConstantsUtils.INTENT_FINISH_FETCH_TRAILER);
        filters.addAction(ConstantsUtils.INTENT_TRAILER_PARSE_START);
        filters.addAction(ConstantsUtils.INTENT_TRAILER_PARSE_END);
        return filters;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(reviewsBroadcastReceiver);
    }

    private final BroadcastReceiver reviewsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcastAction = intent.getAction();
            switch (broadcastAction) {
                case ConstantsUtils.INTENT_FINISH_FETCH_TRAILER:
                    setRecyclerView(false);
                    break;
                case ConstantsUtils.INTENT_FINISH_FETCH_REVIEW:
                    setRecyclerView(true);
                    break;
                case ConstantsUtils.INTENT_REVIEW_PARSE_START:
                case ConstantsUtils.INTENT_TRAILER_PARSE_START:
                    showLoader(true);
                    break;
                case ConstantsUtils.INTENT_REVIEW_PARSE_END:
                case ConstantsUtils.INTENT_TRAILER_PARSE_END:
                    showLoader(false);
                    break;
            }
        }
    };

    private void getExtrasFromIntent() {
        Bundle extras = getIntent().getExtras();
        movieTitle = extras.getString(getString(R.string.key_movie_title));
        movieId = extras.getInt(getString(R.string.key_movie_id));
    }

    private void fetchTrailerAndReview() {
        VolleyUtils.getMovieRequest(getApplicationContext(), movieId, REVIEWS_QUERY);
        VolleyUtils.getMovieRequest(getApplicationContext(), movieId, VIDEOS_QUERY);
    }

    private void setViews() {
        setTitle(movieTitle);
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
            Toast.makeText(appContext, getString(R.string.toast_message_app_not_installed), Toast.LENGTH_SHORT).show();
        }

    }


    private void showConnectionToast() {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, getString(R.string.toast_message_connectivity), Toast.LENGTH_SHORT);
        mToast.show();
    }


    public static void populateReviewView(final String r) {
        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_REVIEW_PARSE_START);
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
                BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_FINISH_FETCH_REVIEW);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_REVIEW_PARSE_END);
            }
        }.execute();
    }

    public static void populateTrailerView(final String r) {
        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_TRAILER_PARSE_START);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                trailerList = new ArrayList();
                try {
                    JSONObject response = new JSONObject(r);
                    JSONArray result = response.getJSONArray("results");
                    for (int i = 0; i < MAX_TRAILERS; i++) {
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
                BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_FINISH_FETCH_TRAILER);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                BroadcastUtils.sendBroadcast(appContext, ConstantsUtils.INTENT_TRAILER_PARSE_END);
            }
        }.execute();
    }

    private void showLoader(boolean b) {
        reviewsRv.setVisibility((b) ? View.INVISIBLE : View.VISIBLE);
        trailersRv.setVisibility((b) ? View.INVISIBLE : View.VISIBLE);
        loaderPb.setVisibility((!b) ? View.INVISIBLE : View.VISIBLE);
    }

    private void setRecyclerView(boolean isReview) {
        if (isReview) {
            if (reviewList.size() > 0) {
                showRecyclerView(reviewsRv);
                reviewsRv.setAdapter(new ReviewAdapter(appContext, reviewList));
            }
            else {
                noReviewsTv.setVisibility(View.VISIBLE);
                reviewsRv.setVisibility(View.GONE);
            }
        } else {
            if(trailerList.size() > 0) {
                showRecyclerView(trailersRv);
                trailersRv.setAdapter(new TrailerAdapter(appContext, trailerList, this));
            }
            else{
                noTrailersTv.setVisibility(View.VISIBLE);
                trailersRv.setVisibility(View.GONE);
            }

        }
    }

    private void showRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void launchTrailer(String trailerKey) {
        launchTrailerIntent(trailerKey);
    }
}
