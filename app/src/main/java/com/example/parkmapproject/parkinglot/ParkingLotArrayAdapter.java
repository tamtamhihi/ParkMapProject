package com.example.parkmapproject.parkinglot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parkmapproject.R;
import com.example.parkmapproject.database.FirebaseStorageHelper;
import com.example.parkmapproject.userrating.UserRating;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ParkingLotArrayAdapter extends
        RecyclerView.Adapter<ParkingLotArrayAdapter.ParkingLotViewHolder>
        implements Filterable {
    private Context context;
    private ArrayList<ParkingLot> mParkingLotList; // Real parking lot for adapter to track
    private ArrayList<ParkingLot> allParkingLot; // All parking lots from database

    public ParkingLotArrayAdapter(@NonNull Context context, ArrayList<ParkingLot> list) {
        this.mParkingLotList = list;
        this.allParkingLot = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ParkingLotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.parkinglot_listitem, parent, false);
        return new ParkingLotViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingLotViewHolder holder, int position) {
        holder.photo.setImageDrawable(context.getDrawable(R.drawable.no_image));
        ParkingLot bindedParkingLot = mParkingLotList.get(position);
        String mName = bindedParkingLot.getTitle();
        String mAddress = bindedParkingLot.getSnippet();
        holder.name.setText(mName);
        holder.address.setText(mAddress);
        if (bindedParkingLot.getPlacePhotos() != null && bindedParkingLot.getPlacePhotos().size() > 0) {
            Glide.with(context).load(bindedParkingLot.getPlacePhotos().get(0)).into(holder.photo);
        }
    }

    @Override
    public int getItemCount() {
        return mParkingLotList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String constraintString = constraint.toString().toLowerCase().trim();
                if (constraintString.isEmpty()) {
                    mParkingLotList = allParkingLot;
                }
                else {
                    ArrayList<ParkingLot> filteredList = new ArrayList<>();
                    for (ParkingLot pl : allParkingLot) {
                        if (pl.getTitle().toLowerCase().trim().contains(constraintString)
                                || pl.getSnippet().toLowerCase().trim().contains(constraintString))
                            filteredList.add(pl);
                    }
                    mParkingLotList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mParkingLotList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mParkingLotList = (ArrayList<ParkingLot>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ParkingLotViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        ParkingLotArrayAdapter adapter;
        TextView name;
        TextView address;
        ImageView photo;

        public ParkingLotViewHolder(@NonNull View itemView, ParkingLotArrayAdapter adapter) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            address = itemView.findViewById(R.id.item_address);
            photo = itemView.findViewById(R.id.place_photo);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ParkingLot clickedItem = mParkingLotList.get(getAdapterPosition());
            Intent intent = new Intent(context, ParkingLotActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedLot", clickedItem);
            ArrayList<Bitmap> placePhotos = clickedItem.getPlacePhotos();
            if (placePhotos != null && placePhotos.size() > 0) {
                ArrayList<String> encodedStrings = new ArrayList<>();
                for (Bitmap bitmap : placePhotos) {
                    encodedStrings.add(encodeBitmap(bitmap));
                }
                bundle.putStringArrayList("placePhotos", encodedStrings);
            }
            ArrayList<UserRating> userRatings = clickedItem.getUserRatings();
            if (userRatings != null && userRatings.size() > 0) {
                bundle.putInt("numRatings", userRatings.size());
                for (int i = 0; i < userRatings.size(); ++i) {
                    bundle.putSerializable("rating" + String.valueOf(i), userRatings.get(i));
                }
            }
            intent.putExtras(bundle);
            context.startActivity(intent);

        }

        private String encodeBitmap(Bitmap bitmap) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            bitmap.recycle();
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }
    }
}
