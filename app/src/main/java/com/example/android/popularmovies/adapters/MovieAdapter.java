package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ReviewAdapterViewHolder> {
    private final MovieAdapterOnClick mClickHandler;
    private final Context thisContext;
    private Cursor movieCursor;

    public MovieAdapter(MovieAdapterOnClick clickHandler, Context thisContext) {
        mClickHandler = clickHandler;
        this.thisContext = thisContext;
    }

    public interface MovieAdapterOnClick {
        void thisClick(int thisMovieId);
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView thumbIv;
        private int thisMovieId;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            thumbIv = view.findViewById(R.id.iv_thumb);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickHandler.thisClick(this.thisMovieId);
        }

        private void setThisMovieId(int id) {
            this.thisMovieId = id;
        }
    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_movie;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        int movieIdColumnIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        int posterPathColumnIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        movieCursor.moveToPosition(position);
        int thisMovieId = movieCursor.getInt(movieIdColumnIndex);
        holder.setThisMovieId(thisMovieId);
        Picasso.with(thisContext)
                .load(Utils.getImagePath(movieCursor.getString(posterPathColumnIndex)))
                .fit()
                .into(holder.thumbIv);

    }

    @Override
    public int getItemCount() {
        return (movieCursor == null)?0: movieCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        if (movieCursor == c)
            return null;
        Cursor temp = movieCursor;
        this.movieCursor = c;
        if (c != null)
            this.notifyDataSetChanged();
        return temp;
    }
}
