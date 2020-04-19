package com.example.parkmapproject.userrating;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parkmapproject.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

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
        String relativeTime = mUserRating.get(position).getRelativeTime();

        holder.username.setText(username);
        holder.comment.setText(comment);
        holder.rating.setRating(rating);
        holder.relativeTime.setText(relativeTime);
    }

    @Override
    public int getItemCount() {
        return mUserRating.size();
    }

    public class RatingViewholder extends RecyclerView.ViewHolder {
        TextView username;
        TextView comment;
        RatingBar rating;
        TextView relativeTime;
        UserRatingAdapter adapter;

        public RatingViewholder(@NonNull View itemView, UserRatingAdapter adapter) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            relativeTime = itemView.findViewById(R.id.timestamp);
            rating = itemView.findViewById(R.id.rating);
            this.adapter = adapter;
        }
    }
}