package com.example.android.popularmovies.models;

/**
 * Created by helpingwithcode on 12/11/17.
 */

public class Movie{
    private int movieId;
    private String title;
    private String poster;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    private String movieReviews;
    private String moviePreviews = "";

    public Movie(int movieId, String title, String poster, String overview, String voteAverage, String releaseDate){
        this.movieId = movieId;
        this.title = title;
        this.poster = poster;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPoster() {
        return poster;
    }

    public String getOverview() {
        return overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getReviews() {
        return movieReviews;
    }

    public void setReviews(String reviews) {
        movieReviews = reviews;
    }

    public String getMoviePreviews() {
        return moviePreviews;
    }

    public void setMoviePreviews(String previews) {
        moviePreviews = previews;
    }
}