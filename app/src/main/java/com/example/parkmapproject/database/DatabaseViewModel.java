package com.example.parkmapproject.database;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.parkmapproject.R;
import com.example.parkmapproject.database.FirebaseDatabaseHelper;
import com.example.parkmapproject.database.FirebaseStorageHelper;
import com.example.parkmapproject.parkinglot.GeocodingAPI;
import com.example.parkmapproject.parkinglot.ParkingLot;
import com.example.parkmapproject.userrating.PlacesAPI;
import com.example.parkmapproject.userrating.UserRating;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
This ViewModel is used to retrieve ParkingLot data from Firebase
and fetch Place ID, User Ratings and Photos using Places API for
all fragments and remain unchanged during configuration changes.
 */
public class DatabaseViewModel extends AndroidViewModel {
    // ParkingLot database and a boolean to indicate if the fetching process is finished.
    private MutableLiveData<ArrayList<ParkingLot>> mParkingLotData;
    private MutableLiveData<Boolean> isLoading;

    // Context, client and API Key to invoke Places API.
    private Context context;
    private String apiKey;
    PlacesClient client;

    // Array to store database and DatabaseHelper to fetch data from Firebase.
    private ArrayList<ParkingLot> mParkingLot = new ArrayList<>();
    private FirebaseDatabaseHelper databaseHelper = new FirebaseDatabaseHelper();

    public DatabaseViewModel(@NonNull Application application) {
        super(application);
    }

    /*
    This function fetches all information about parking lots from Firebase just once for each
    time the application runs.
     */
    public LiveData<ArrayList<ParkingLot>> getDatabase() {
        if (mParkingLotData == null) {
            isLoading = new MutableLiveData<>(true);
            mParkingLotData = new MutableLiveData<>();
            setupParkingLotDatabase();
        }
        return mParkingLotData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    /*
    This function retrieves all parking lots data available on Firebase including name, address,
    LatLng, placeId (if available) and user ratings (if available). It also executes 2 AsyncTask to
    retrieve photos for each parking lot from internal storage or load new photos from Google Places
    Photos.
    */
    private void setupParkingLotDatabase() {
        databaseHelper.readParkingLots(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys) {
                mParkingLot.addAll(parkingLots);
                context = getApplication().getApplicationContext();
                apiKey = context.getResources().getString(R.string.google_maps_key);
                Places.initialize(context, apiKey);
                client = Places.createClient(context);
                new getPlaceId().execute();
                // new getUserRatings().execute();
                mParkingLotData.setValue(mParkingLot);
                isLoading.setValue(false);
            }

            @Override
            public void DataIsInserted() {
            }

            @Override
            public void DataIsUpdated() {
            }

            @Override
            public void DataIsDeleted() {
            }
        });
    }

    /*
    This AsyncTask is used to get the placeId for new parking lots added to the Firebase database
    and save that placeId information to Firebase. If the parking lot's placeId is available and
    as been retrieved from database, the doInBackground is skipped.
     */
    private class getPlaceId extends AsyncTask<Void, GeocodingAPI, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            for (ParkingLot parkingLot : mParkingLot) {
                if (parkingLot.getPlaceId() != null)
                    continue;
                GeocodingAPI geocodingAPI = new GeocodingAPI();
                try {
                    geocodingAPI = new GeocodingAPI(parkingLot.getLatitude(), parkingLot.getLongitude(), apiKey);
                } catch (MalformedURLException e) {
                    Log.d("DMVM Line 128", "MalformedURLException: " + e);
                }
                try {
                    ArrayList<String> placeId = geocodingAPI.getPlaceId();
                    if (placeId != null && placeId.size() > 0) {
                        parkingLot.setPlaceId(placeId.get(0));
                    }
                } catch (IOException | JSONException e) {
                    Log.d("DBVM Line 108 ViewModel", "is wrong");
                    e.printStackTrace();
                }
            }
            return null;
        }
        /*
        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            for (ParkingLot parkingLot : mParkingLot) {
                String key = parkingLot.getKey();
                String placeId = parkingLot.getPlaceId();
                if (placeId == null)
                    continue;
                if (parkingLot.getPlacePhotos() != null && parkingLot.getPlacePhotos().size() > 0)
                    continue;
                List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields).build();
                client.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    List<PhotoMetadata> photoMetadatas = place.getPhotoMetadatas();
                    if (photoMetadatas != null) {
                        for (PhotoMetadata photo : photoMetadatas) {
                            String attributions = photo.getAttributions();
                            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo).build();
                            client.fetchPhoto(photoRequest).addOnSuccessListener((photoResponse) -> {
                                Bitmap bitmap = photoResponse.getBitmap();
                                parkingLot.addPlacePhoto(bitmap);
                                databaseHelper.updateBitmap(bitmap, key);
                                Log.d("DBVM Line 161", "Save places photo done for PL " + key);
                                /*
                                try {
                                    savePhoto(bitmap, parkingLot.getKey());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.d("DBVM Line 154", "Save places photo not ok for PL " + parkingLot.getKey());
                                }

                            }).addOnFailureListener((e) -> {
                                if (e instanceof ApiException) {
                                    ApiException exception = (ApiException) e;
                                    Log.e("DBVM Line 177", "Api exception: " + e);
                                }
                            });
                        }
                    }
                }).addOnFailureListener((e) -> {
                    Log.d("BDVM Line 140 ViewModel", "Hu rui :((" + e.getMessage());
                });
                /*
                try {
                    savePlacesPhoto(parkingLot);
                } catch (IOException e) {
                    Log.d("DBVM Line 157", "Save places photo not ok for PL " + parkingLot.getKey());
                    e.printStackTrace();
                }

            }
        }
        */
    }

    private class getUserRatings extends AsyncTask<Void, PlacesAPI, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            PlacesAPI placesAPI = new PlacesAPI();
            for (int i = 0; i < mParkingLot.size(); ++i) {
                ParkingLot parkingLot = mParkingLot.get(i);
                if (parkingLot.getUserRatings() != null && parkingLot.getUserRatings().size() > 0)
                    continue;
                String key = parkingLot.getKey();
                try {
                    placesAPI = new PlacesAPI(parkingLot.getPlaceId(), apiKey);
                } catch (JSONException | IOException e) {
                    Log.d("DBVM Line165VM", "JSONException | IOException: ");
                    e.printStackTrace();
                }
                try {
                    parkingLot.setUserRatings(placesAPI.getUserRatings());
                    if (parkingLot.getUserRatings() != null && parkingLot.getUserRatings().size() > 0) {
                        for (UserRating userRating : parkingLot.getUserRatings())
                            databaseHelper.updateUserRating(userRating, key);
                    }
                } catch (JSONException | IOException e) {
                    Log.d("DBVM Line171VM", "JSONException | IOException");
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


    /*
    These functions are meant to save photos to device's internal storage but are not in use.
     */
    private void savePlacesPhoto(ParkingLot parkingLot) throws IOException {
        ContextWrapper wrapper = new ContextWrapper(context);
        File directory = wrapper.getDir(parkingLot.getKey(), Context.MODE_PRIVATE);
        ArrayList<Bitmap> bitmaps = parkingLot.getPlacePhotos();
        if (bitmaps != null && bitmaps.size() > 0) {
            for (Bitmap bitmap : bitmaps) {
                File newFile = new File(directory, bitmap.toString() + ".jpg");
                FileOutputStream fos = new FileOutputStream(newFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            }
        }
        Log.d("DBVM Line 199", "Finished saving for PL " + parkingLot.getKey());
    }
    private void savePhoto(Bitmap bitmap, String key) throws IOException {
        ContextWrapper wrapper = new ContextWrapper(context);
        File directory = wrapper.getDir(key, Context.MODE_PRIVATE);
        File newFile = new File(directory, bitmap.toString() + ".jpg");
        if (newFile.exists())
            return;
        FileOutputStream fos = new FileOutputStream(newFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        Log.d("DBVM Line 199", "Finished saving 1 bitmap for PL " + key);
    }
    private void readPlacesPhoto(ParkingLot parkingLot) throws FileNotFoundException {
        File directory = new File(parkingLot.getKey());
        if (directory.isDirectory()) {
            ArrayList<Bitmap> placesPhoto = new ArrayList<>();
            for (File image : Objects.requireNonNull(directory.listFiles())) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(image));
                placesPhoto.add(bitmap);
            }
            parkingLot.setPlacePhotos(placesPhoto);
        }
    }
}