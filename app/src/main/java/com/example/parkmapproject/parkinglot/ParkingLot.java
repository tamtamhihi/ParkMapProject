package com.example.parkmapproject.parkinglot;

import android.graphics.Bitmap;

import com.example.parkmapproject.userrating.UserRating;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParkingLot implements ClusterItem, Serializable, Comparable {
    private String name;
    private String address;
    private transient LatLng position;
    private float latitude;
    private float longitude;
    private String price;
    private transient ArrayList<UserRating> userRatings = new ArrayList<>();
    private Map<String, UserRating> ratings = new HashMap<>();
    private float averageRating;
    private double distanceFromSelected;
    private String placeId;
    private String key;
    private transient ArrayList<Bitmap> placePhotos = new ArrayList<>();
    private Map<String, String> encodedPhotos = new HashMap<>();
    private boolean imageFromCloud;
    private boolean suggested;

    // CONSTRUCTORS
    public ParkingLot() {
    }

    public ParkingLot(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ParkingLot(float latitude, float longitude, String name, String address, String price) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.setLatLng();
        this.name = name;
        this.address = address;
        this.price = price;
        suggested = false;
    }

    // SETTERS
    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatLng() {
        position = new LatLng(latitude, longitude);
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setUserRatings(ArrayList<UserRating> userRatings) {
        this.userRatings = userRatings;
    }

    public void setRatings(Map<String, UserRating> ratings) {
        this.ratings = ratings;
    }

    public void setDistanceFromSelected(LatLng selected) {
        this.distanceFromSelected = distanceFromLatLng(selected, this.position);
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPlacePhotos(ArrayList<Bitmap> placePhotos) {
        this.placePhotos = placePhotos;
    }

    public void setImageFromCloud(boolean imageFromCloud) {
        this.imageFromCloud = imageFromCloud;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
    }

    public void setEncodedPhotos(Map<String, String> encodedPhotos) {
        this.encodedPhotos = encodedPhotos;
    }

    // GETTERS
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getPrice() {
        return price;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public ArrayList<UserRating> getUserRatings() {
        return userRatings;
    }

    public Map<String, UserRating> getRatings() {
        return ratings;
    }

    public double getDistanceFromSelected() {
        return distanceFromSelected;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<Bitmap> getPlacePhotos() {
        return placePhotos;
    }

    public boolean isImageFromCloud() {
        return imageFromCloud;
    }

    public boolean isSuggested() {
        return suggested;
    }

    public Map<String, String> getEncodedPhotos() {
        return encodedPhotos;
    }

    // CLUSTER MARKERS
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return address;
    }

    @Override
    public int compareTo(Object parkingLot) {
        double comparedDistance = ((ParkingLot) parkingLot).getDistanceFromSelected();
        if (distanceFromSelected < comparedDistance)
            return -1;
        if (distanceFromSelected == comparedDistance)
            return 0;
        return 1;
    }

    // Distance
    private double radian(double deg) {
        return deg * Math.PI / 180;
    }

    private double distanceFromLatLng(LatLng start, LatLng destination) {
        double R = 6371; // Earth radius
        double sLat = start.latitude, sLng = start.longitude,
                dLat = destination.latitude, dLng = destination.longitude;
        double difLat = radian(dLat - sLat);
        double difLng = radian(dLng - sLng);
        double a = Math.sin(difLat / 2) * Math.sin(difLat / 2)
                + Math.cos(radian(sLat)) * Math.cos(radian(dLat))
                * Math.sin(difLng / 2) * Math.sin(difLng / 2);
        double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * b;
    }

    public void addPlacePhoto(Bitmap bitmap) {
        placePhotos.add(bitmap);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        //result.put("userRatings", userRatings);
        result.put("placeId", placeId);
        //result.put("placePhotos", placePhotos);
        return result;
    }
}