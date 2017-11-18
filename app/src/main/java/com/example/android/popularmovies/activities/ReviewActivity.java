package com.example.android.popularmovies.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.ReviewAdapter;
import com.example.android.popularmovies.models.UserMovieReview;
import com.example.android.popularmovies.utils.BroadcastUtils;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.android.popularmovies.utils.ConstantsUtils.REVIEWS_QUERY;

public class ReviewActivity extends AppCompatActivity {

    private static ArrayList<UserMovieReview> reviewList;
    private int movieId;
    private String movieTitle;
    private static Context appContext;

    @BindView(R.id.rv_reviews)
    RecyclerView reviewsRv;
    @BindView(R.id.tv_review_title)
    TextView reviewTitleTv;
    @BindView(R.id.pb_loading)
    ProgressBar loaderPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);
        appContext = getApplicationContext();
        getExtrasFromIntent();
        fetchReviews();
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
                case ConstantsUtils.INTENT_FINISH_FETCH_REVIEW:
                    setRecyclerView();
                    break;
                case ConstantsUtils.INTENT_REVIEW_PARSE_START:
                    showLoader(true);
                    break;
                case ConstantsUtils.INTENT_REVIEW_PARSE_END:
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

    private void fetchReviews() {
        VolleyUtils.getMovieRequest(getApplicationContext(), movieId, REVIEWS_QUERY);
    }

    private void setViews() {
        reviewTitleTv.setText(String.format(getString(R.string.review_title),movieTitle));
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
                log("ReviewList: " + reviewList);
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

    private void showLoader(boolean b) {
        reviewsRv.setVisibility((b) ? View.INVISIBLE : View.VISIBLE);
        loaderPb.setVisibility((!b) ? View.INVISIBLE : View.VISIBLE);
    }

    private void setRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(appContext);

        ReviewAdapter reviewAdapter = new ReviewAdapter(getApplicationContext(), reviewList);
        reviewsRv.setLayoutManager(linearLayoutManager);
        reviewsRv.setHasFixedSize(true);
        reviewsRv.setAdapter(reviewAdapter);
    }

    public static void log(String s) {
        Log.e("ReviewActivity", s);
    }
}
