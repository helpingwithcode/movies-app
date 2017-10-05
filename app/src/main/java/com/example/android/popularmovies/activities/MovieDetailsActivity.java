package com.example.android.popularmovies.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.dao.DAOMovie;
import com.example.android.popularmovies.interfaces.IDAOMovie;
import com.example.android.popularmovies.models.PopularMovie;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.Utils;
import com.squareup.picasso.Picasso;

public class MovieDetailsActivity extends AppCompatActivity {
    private IDAOMovie idaoMovie;
    private ImageView moviePosterIv;
    private TextView movieTitleTv;
    private TextView moviePlotTv;
    private TextView movieRatings;
    private TextView movieReleaseDateTv;
    private int thisMovieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        idaoMovie = new DAOMovie();
        castLayoutVariables();
        checkSavedInstance(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putInt("movieId", thisMovieId);
        Log.e("MovieDetails","saving movieId on onSavedInstaceState");
        super.onSaveInstanceState(savedState);
    }

    private void checkSavedInstance(Bundle savedInstanceState) {
        if(savedInstanceState == null)
            getIntents();
        else
            populateLayoutWithMovieInfo(savedInstanceState.getInt("movieId"));
    }

    private void castLayoutVariables() {
        moviePosterIv = (ImageView) findViewById(R.id.iv_poster);
        movieTitleTv = (TextView) findViewById(R.id.tv_title);
        moviePlotTv = (TextView) findViewById(R.id.tv_plot);
        movieReleaseDateTv = (TextView) findViewById(R.id.tv_release_date);
        movieRatings = (TextView) findViewById(R.id.tv_ratings);
    }

    private void getIntents() {
        Bundle intents = getIntent().getExtras();
        if(getIntent().getExtras() != null)
            populateLayoutWithMovieInfo(intents.getInt("movieId"));
    }

    private void populateLayoutWithMovieInfo(int movieId) {
        thisMovieId = movieId;
        PopularMovie thisMovie = idaoMovie.getMovieById(movieId);
        moviePlotTv.setText(thisMovie.getOverview());
        movieTitleTv.setText(thisMovie.getOriginal_title());
        movieReleaseDateTv.setText(getResources().getString(R.string.release_date)+" "+thisMovie.getRelease_date());
        movieRatings.setText(String.valueOf(getResources().getString(R.string.vote_avarage)+" "+thisMovie.getVote_average()));
        Picasso.with(getApplicationContext())
                .load(Utils.getImagePath(thisMovie.getPoster_path()))
//                .centerCrop()
                .fit()
                .into(moviePosterIv);
    }

}
