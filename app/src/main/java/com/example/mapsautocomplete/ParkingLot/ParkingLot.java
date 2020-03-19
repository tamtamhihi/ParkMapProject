package com.example.mapsautocomplete.ParkingLot;

import com.example.mapsautocomplete.UserRating.UserRating;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;

public class ParkingLot implements ClusterItem, Serializable {
    private transient LatLng mPosition;
    private String mTitle;
    private String mAddress;
    private String mPrice;
    private String mOpeningHours;
    private String mNotes;
    private ArrayList<UserRating> mUserRating = new ArrayList<>();
    private float mAverageRating;

    private float latitude;
    private float longitude;
    private String name;

    // No-argument constructor for firebase getValue
    public ParkingLot() {}

    public ParkingLot(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public ParkingLot(double latitude, double longitude, String name, String note, String price) {
        String address = "Temporarily no correct address yet";
        mPosition = new LatLng(latitude, longitude);
        mTitle = name;
        mAddress = name;
        mUserRating.add(new UserRating("Tam Ngoc Bang Nguyen", "Tui rất thích bãi giữ xe này", 5));
        mUserRating.add(new UserRating("Tấn Phát", "Bãi giữ xe chặt chém, không thân thiện, cẩn thận bị trầy xe", 2));
        mUserRating.add(new UserRating("Nguyễn Hữu Hưng", "Dịch vụ bãi giữ xe rất tốt, cho 5 sao", 1));
        mUserRating.add(new UserRating("Đào Thiên Phú", "Bãi giữ xe dành cho wibu", 5));
        float totalRating = 0;
        for (UserRating ur : mUserRating) {
            totalRating += ur.getRating();
        }
        if (totalRating != 0)
            mAverageRating = (totalRating / mUserRating.size());
        else mAverageRating = 0;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mAddress;
    }

    public ArrayList<UserRating> getUserRating() {
        return mUserRating;
    }

    public float getAverageRating() {
        return mAverageRating;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getOpeningHours() {
        return mOpeningHours;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        mTitle = name;
        mAddress = name;
    }

    public String getName() {
        return mTitle;
    }

    public void setNote(String note) {
        mNotes = note;
    }

    public String getNote() {
        return mNotes;
    }

    public void setPrice(String price){
        mPrice = price;
    }


    public void setLatLng() {
        mPosition = new LatLng(latitude, longitude);
    }

}
