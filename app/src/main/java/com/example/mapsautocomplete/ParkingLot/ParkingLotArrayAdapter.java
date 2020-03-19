package com.example.mapsautocomplete.ParkingLot;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapsautocomplete.ParkingLotActivity;
import com.example.mapsautocomplete.R;

import java.util.ArrayList;

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
        String mName = mParkingLotList.get(position).getTitle();
        String mAddress = mParkingLotList.get(position).getSnippet();
        holder.name.setText(mName);
        holder.address.setText(mAddress);
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

        public ParkingLotViewHolder(@NonNull View itemView, ParkingLotArrayAdapter adapter) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            address = itemView.findViewById(R.id.item_address);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ParkingLot clickedItem = mParkingLotList.get(getAdapterPosition());
            Intent intent = new Intent(context, ParkingLotActivity.class);
            intent.putExtra("selectedLot", clickedItem);
            context.startActivity(intent);
        }
    }
}
