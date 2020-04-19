package com.example.parkmapproject.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.parkmapproject.parkinglot.ParkingLot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FirebaseStorageHelper {
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public FirebaseStorageHelper() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("placePhotos");
    }
    public void uploadPlacePhotos(ParkingLot parkingLot) {
        StorageReference placePhotoReference
                = storageReference.child(parkingLot.getKey());
        ArrayList<Bitmap> placePhotos = parkingLot.getPlacePhotos();
        if (placePhotos != null && placePhotos.size() > 0) {
            for (Bitmap bitmap : placePhotos) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = placePhotoReference.child(bitmap.toString()).putBytes(data);
                uploadTask.addOnFailureListener(e ->
                        Log.d("Storage", "Upload unsuccessful")
                ).addOnSuccessListener(taskSnapshot -> {
                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                });
            }
        }
    }
    public ArrayList<Bitmap> downloadPlacePhotos(String key) {
        final long ONE_MEGABYTE = 1024 * 1024;
        ArrayList<Bitmap> placePhotos = new ArrayList<>();
        StorageReference parkingLotReference = storageReference.child(key);
        parkingLotReference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    placePhotos.add(bitmap);
                }).addOnFailureListener(e -> Log.d("Line63FBSR", "Get bytes failed: " + key));
            }
        }).addOnFailureListener(e -> Log.d("Line65FBSR", "List all failed"));
        return placePhotos;
    }
}