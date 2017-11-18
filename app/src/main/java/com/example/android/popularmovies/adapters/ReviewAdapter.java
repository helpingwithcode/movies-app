package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.models.UserMovieReview;

import java.util.ArrayList;

/**
 * Created by helpingwithcode on 13/11/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {
    final Context thisContext;
    private final ArrayList<UserMovieReview> reviewList;

    public ReviewAdapter(Context thisContext, ArrayList reviewList) {
        this.thisContext = thisContext;
        this.reviewList = reviewList;
        log("ReviewAdapterConstructor");
    }


    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder{
        private final TextView contentTv;
        private final TextView authorTv;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            authorTv = view.findViewById(R.id.tv_author);
            contentTv = view.findViewById(R.id.tv_content);
        }

    }

    @Override
    public ReviewAdapter.ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        log("onCreateViewHolder(ViewGroup viewGroup, int viewType)");
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_review;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new ReviewAdapter.ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewAdapterViewHolder holder, int position) {
        UserMovieReview thisUserReview = reviewList.get(position);
        holder.authorTv.setText(String.format(thisContext.getString(R.string.review_author),thisUserReview.getUser()));
        holder.contentTv.setText(thisUserReview.getReview());
    }

    @Override
    public int getItemCount() {
        return (reviewList != null) ? reviewList.size() : 0;
    }

    private void log(String s) {
        Log.e("MovieAdapter", s);
    }
}
