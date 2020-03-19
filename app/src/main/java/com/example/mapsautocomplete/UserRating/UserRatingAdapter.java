package com.example.mapsautocomplete.UserRating;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapsautocomplete.R;

import java.util.ArrayList;

public class UserRatingAdapter extends RecyclerView.Adapter<UserRatingAdapter.RatingViewholder> {
    private ArrayList<UserRating> mUserRating;

    public UserRatingAdapter(ArrayList<UserRating> userRatingList) {
        this.mUserRating = userRatingList;
    }

    @NonNull
    @Override
    public RatingViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_rating, parent, false);
        return new RatingViewholder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewholder holder, int position) {
        String username = mUserRating.get(position).getUsername();
        String comment = mUserRating.get(position).getComment();
        int rating = mUserRating.get(position).getRating();

        holder.username.setText(username);
        holder.comment.setText(comment);
        holder.rating.setRating(rating);
    }

    @Override
    public int getItemCount() {
        return mUserRating.size();
    }

    public class RatingViewholder extends RecyclerView.ViewHolder {
        TextView username;
        TextView comment;
        RatingBar rating;
        UserRatingAdapter adapter;
        public RatingViewholder(@NonNull View itemView, UserRatingAdapter adapter) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            rating = itemView.findViewById(R.id.rating);
            this.adapter = adapter;
        }
    }
}
