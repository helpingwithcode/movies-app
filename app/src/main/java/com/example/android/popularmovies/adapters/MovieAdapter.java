package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.models.PopularMovie;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.ConstantsUtils;
import com.example.android.popularmovies.utils.Utils;
import com.squareup.picasso.Picasso;

import io.realm.RealmResults;

/**
 * Created by helpingwithcode on 27/09/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    private RealmResults<PopularMovie> dataBase;
    private final MovieAdapterOnClick mClickHandler;
    private final Context thisContext;
    private int thisMovieId;

    public MovieAdapter(MovieAdapterOnClick clickHandler, Context thisContext) {
        mClickHandler = clickHandler;
        this.thisContext = thisContext;
    }

    public interface MovieAdapterOnClick {
        void thisClick(int thisMovieId);
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView thumbIv;
        private int thisMovieId;

        public MovieAdapterViewHolder(View view) {
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
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        log("onCreateViewHolder(ViewGroup viewGroup, int viewType)");
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        try {
            PopularMovie thisMovie = dataBase.get(position);
            //thisMovieId = thisMovie.getId();
            holder.setThisMovieId(thisMovie.getId());
            Picasso.with(thisContext)
                    .load(Utils.getImagePath(thisMovie.getPoster_path()))
                    //.centerCrop()
                    .fit()
                    .into(holder.thumbIv);
        }
        catch (Exception e){
            log("Exception thrown on onBindViewHolder: "+e.getLocalizedMessage());
        }
    }

    @Override
    public int getItemCount() {
        if (dataBase == null) return 0;
        return dataBase.size();
    }

    public void setDataBase(RealmResults<PopularMovie> movies) {
        log("setDataBase(RealmResults<PopularMovie> movies)");
        log("movies.size: "+movies.size());
        dataBase = movies;
        notifyDataSetChanged();
    }

    private void log(String s) {
        Log.e("MovieAdapter",s);
    }
}
