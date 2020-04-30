package com.example.parkmapproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkmapproject.database.DatabaseViewModel;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int MY_PERMISSIONS_REQUEST_FIND_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private DatabaseViewModel parkingLotDatabase;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        loadingBar = findViewById(R.id.progress_bar);
        getLocationPermission();
        getStoragePermisison();
        loadParkingLotDatabase();
    }

    /*
    This function checks if location permission has been granted. If not, it will show a request
    dialog to user. This permission is needed for finding user's current location.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(Objects.requireNonNull(this),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FIND_LOCATION);
    }

    /*
    This function checks if reading storage permission has been granted. If not, it will show a
    request dialog to user. This permission is needed for user to upload new images or parking lots.
     */
    private void getStoragePermisison() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
    }

    /*
    This function checks if the two permissions have both been granted. If so, it will start fetching
    the database from Firebase.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            loadParkingLotDatabase();
    }

    /*
    This function fetch database from Firebase. On finishing, the progress bar will disappear and
    the application will start.
     */
    private void loadParkingLotDatabase() {
        loadingBar.setVisibility(View.VISIBLE);
        parkingLotDatabase = new ViewModelProvider(this).get(DatabaseViewModel.class);
        parkingLotDatabase.getDatabase();
        parkingLotDatabase.isLoading().observe(this, isLoading -> {
            if (isLoading != null && !isLoading) {
                loadingBar.setVisibility(View.GONE);
                startApplication();
            }
        });
    }

    /*
    This function starts the MainActivity of the application.
     */
    private void startApplication() {
        new Handler().postDelayed(() -> {
            SplashScreen.this.finish();
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            SplashScreen.this.startActivity(intent);
        }, SPLASH_DISPLAY_LENGTH);
    }
}
