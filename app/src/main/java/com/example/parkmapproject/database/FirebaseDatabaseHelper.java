package com.example.parkmapproject.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.example.parkmapproject.parkinglot.ParkingLot;
import com.example.parkmapproject.userrating.UserRating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        mReferenceParkingLots = mDatabase.getReference().child("parking_lot");
        mReferenceParkingLots.keepSynced(true);
    }

    /*
    Load all parking lot information including name, address, latitude, longitude, photos
    and reviews.
     */
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
                    parkingLot.setKey(keyNode.getKey());
                    if (parkingLot.getEncodedPhotos() != null && parkingLot.getEncodedPhotos().size() > 0) {
                        for (String encodedString : parkingLot.getEncodedPhotos().values()) {
                            parkingLot.addPlacePhoto(decodeString(encodedString));
                        }
                    }
                    if (parkingLot.getRatings() != null && parkingLot.getRatings().size() > 0) {
                        ArrayList<UserRating> userRatings = new ArrayList<>(parkingLot.getRatings().values());
                        parkingLot.setUserRatings(userRatings);
                    }
                    parkingLots.add(parkingLot);
                }
                dataStatus.DataLoaded(parkingLots, keys);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*
    This function uploads placeId of new parking lots to Firebase.
     */
    public void updateParkingLots(ArrayList<ParkingLot> parkingLots) {
        Map<String, Object> parkingLotUpdates = new HashMap<>();
        for (ParkingLot parkingLot : parkingLots) {
            parkingLotUpdates.put(parkingLot.getKey() + "/placeId", parkingLot.getPlaceId());
            new FirebaseStorageHelper().uploadPlacePhotos(parkingLot);
        }
        mReferenceParkingLots.updateChildren(parkingLotUpdates);
    }

    /*
    This function resizes a bitmap, encodes it and uploads it to Firebase.
     */
    public void updateBitmap(Bitmap originalBitmap, String key) {
        Bitmap bitmap = getResizedBitmap(originalBitmap, 1000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmap.recycle();
        byte[] bytes = baos.toByteArray();
        String encodedBitmap = Base64.encodeToString(bytes, Base64.DEFAULT);
        mReferenceParkingLots.child(key).child("encodedPhotos").push().setValue(encodedBitmap);
    }

    /*
    This function uploads a new user rating/review to Firebase.
     */
    public void updateUserRating(UserRating userRating, String key) {
        String newRatingKey = mReferenceParkingLots.child(key).child("ratings").push().getKey();
        Map<String, Object> updateNewRating = new HashMap<>();
        updateNewRating.put(key + "/ratings/" + newRatingKey + "/username", userRating.getUsername());
        updateNewRating.put(key + "/ratings/" + newRatingKey + "/comment", userRating.getComment());
        updateNewRating.put(key + "/ratings/" + newRatingKey + "/rating", userRating.getRating());
        updateNewRating.put(key + "/ratings/" + newRatingKey + "/timestamp", userRating.getTimestamp());
        mReferenceParkingLots.updateChildren(updateNewRating);
    }

    public void updatePhotos(ArrayList<ParkingLot> parkingLots) {
        if (parkingLots == null || parkingLots.size() == 0)
            return;
        Map<String, Object> parkingLotUpdates = new HashMap<>();
        for (ParkingLot parkingLot : parkingLots) {
            ArrayList<Bitmap> placePhotos = parkingLot.getPlacePhotos();
            if (placePhotos == null || placePhotos.size() == 0)
                return;
            for (Bitmap bitmap : placePhotos) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
                bitmap.recycle();
                byte[] bytes = baos.toByteArray();
                String encodedBitmap = Base64.encodeToString(bytes, Base64.DEFAULT);
                parkingLotUpdates.put(parkingLot.getKey() + "/placePhotos/", encodedBitmap);
            }
        }
    }

    /*
    This function decodes the string from Firebase into a bitmap on downloading.
     */
    private Bitmap decodeString(String encodedImage) {
        byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
        return decodedByte;
    }

    /*
    This function resizes the bitmap.
     */
    public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = scaleWidth;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }

}
