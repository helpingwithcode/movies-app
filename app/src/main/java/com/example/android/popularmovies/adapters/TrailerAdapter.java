package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.models.MovieTrailer;

import java.util.ArrayList;

/**
 * Created by helpingwithcode on 20/11/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {
    final Context thisContext;
    private final ArrayList<MovieTrailer> trailerList;

    OnTrailerClick trailerClickListener;

    public interface OnTrailerClick{
        void launchTrailer(String trailerKey);
    }

    public TrailerAdapter(Context thisContext, ArrayList trailerList, OnTrailerClick trailerClickHandler) {
        trailerClickListener = trailerClickHandler;
        this.thisContext = thisContext;
        this.trailerList = trailerList;
    }


    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView trailerNameTv;
        private String trailerKey;

        public TrailerAdapterViewHolder(View view) {
            super(view);
            trailerNameTv = view.findViewById(R.id.tv_trailer_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            trailerClickListener.launchTrailer(this.trailerKey);
        }

        private void setTrailerKey(String trailerKey) {
            this.trailerKey = trailerKey;
        }
    }

    @Override
    public TrailerAdapter.TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new TrailerAdapter.TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.TrailerAdapterViewHolder holder, int position) {
        MovieTrailer movieTrailer = trailerList.get(position);
        holder.trailerNameTv.setText(setTrailerTvText(String.valueOf(position+1),movieTrailer.getName()));
        holder.setTrailerKey(movieTrailer.getKey());
    }

    private String setTrailerTvText(String position, String name) {
        return String.format(thisContext.getString(R.string.trailer_text),position,name);
    }

    @Override
    public int getItemCount() {
        return (trailerList != null) ? trailerList.size() : 0;
    }
}
