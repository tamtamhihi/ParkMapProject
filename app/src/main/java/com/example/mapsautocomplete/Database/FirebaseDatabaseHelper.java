package com.example.mapsautocomplete.Database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mapsautocomplete.ParkingLot.ParkingLot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceParkingLots;
    private ArrayList<ParkingLot> parkingLots = new ArrayList<>();

    public interface DataStatus {
        void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys);
        void DataIsInserted();
        void DataIsUpdated();
        void DataIsDeleted();
    }

        public FirebaseDatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance();
            mReferenceParkingLots = mDatabase.getReference("Parking_Lots").child("ParkingLot");
        }

    public void readParkingLots(final DataStatus dataStatus) {
        mReferenceParkingLots.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingLots.clear();
                List<String> keys = new ArrayList<>();
                for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                    keys.add(keyNode.getKey());
                    ParkingLot parkingLot = keyNode.getValue(ParkingLot.class);
                    parkingLot.setLatLng();
                    //Log.d("debugData", "Debugging...");
                    parkingLots.add(parkingLot);
                }
                dataStatus.DataLoaded(parkingLots,keys);
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
