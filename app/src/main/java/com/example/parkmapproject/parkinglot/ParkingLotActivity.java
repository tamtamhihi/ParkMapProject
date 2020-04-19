package com.example.parkmapproject.parkinglot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.parkmapproject.R;
import com.example.parkmapproject.database.FirebaseDatabaseHelper;
import com.example.parkmapproject.userrating.UserRating;
import com.example.parkmapproject.userrating.UserRatingAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class ParkingLotActivity extends AppCompatActivity {
    // Current ParkingLot
    private ParkingLot item;

    // Collapsing Toolbar
    private Toolbar toolbar;
    private ViewPager viewPager;
    ViewPagerAdapter imageAdapter;
    private FloatingActionButton addImageButton;

    // ParkingLot information views
    private TextView name;
    private TextView address;
    private RatingBar rating;
    private TextView price;
    private ArrayList<Bitmap> placePhotos = new ArrayList<>();
    private RecyclerView userRating;
    UserRatingAdapter userRatingAdapter;
    private TextView ratingEmpty;
    ArrayList<String> encodedStrings;

    // ParkingLot information
    String mName;
    String mAddress;
    float mRating = 0;
    String mPrice;
    ArrayList<UserRating> mUserRating = new ArrayList<>();

    private static final int PICK_IMAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;
    private boolean mStoragePermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_lot);
        item = (ParkingLot) getIntent().getSerializableExtra("selectedLot");
        encodedStrings = getIntent().getStringArrayListExtra("placePhotos");

        fetchParkingLotInfo();
        fetchToolbarView();
        fetchInfoView();
        setInfoToView();
    }

    /*
    This function decodes the strings to bitmaps.
     */
    private Bitmap decodeString(String encodedImage) {
        byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
        return decodedByte;
    }

    /*
     Navigate to previous fragment when clicking back button.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Navigate to previous fragment when clicking back button.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
    }

    /*
    This function fetch all available information about the current parking lot, including name,
    address, user rating. The encoded strings is decoded into bitmaps and added to placePhotos.
    If there's no photo, the default no_image bitmap will be added to placePhotos.
     */
    private void fetchParkingLotInfo() {
        mName = item.getName();
        mAddress = item.getAddress();
        mPrice = item.getPrice();
        Map<String, UserRating> ratings = item.getRatings();
        if (encodedStrings != null && encodedStrings.size() > 0)
            for (String encoded : encodedStrings)
                placePhotos.add(decodeString(encoded));
        if (placePhotos == null || placePhotos.size() == 0) {
            placePhotos = new ArrayList<>();
            placePhotos.add(BitmapFactory.decodeResource(getResources(), R.drawable.no_image));
        }
        if (ratings != null && ratings.size() > 0) {
            for (UserRating rating : ratings.values()) {
                if (rating != null)
                    mUserRating.add(rating);
            }
        }
    }

    /*
    This function fetch all views in AppBarlayout including toolbar, viewpager for parking lot's
    photos and a button to add image.
     */
    private void fetchToolbarView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mName);
        viewPager = findViewById(R.id.gallery_swipe);
        imageAdapter = new ViewPagerAdapter(this, placePhotos);
        viewPager.setAdapter(imageAdapter);
        addImageButton = findViewById(R.id.add_image);
        addImageButton.setOnClickListener(v -> {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
            startActivityForResult(chooserIntent, PICK_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null)
                return;
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                new FirebaseDatabaseHelper().updateBitmap(bitmap, item.getKey());
                placePhotos.add(bitmap);
                imageAdapter.notifyDataSetChanged();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        This function fetch all views that will display the parking lot's information.
         */
    private void fetchInfoView() {
        name = findViewById(R.id.title);
        address = findViewById(R.id.address);
        rating = findViewById(R.id.rating);
        price = findViewById(R.id.price);
        userRating = findViewById(R.id.user_rating_list);
        ratingEmpty = findViewById(R.id.comment_not_available);
    }

    /*
    This function set all retrieved information to the corresponding views and set to default when
    the information needed is missing.
     */
    private void setInfoToView() {
        toolbar.setTitle(mName);
        toolbar.setSubtitle(mAddress);
        name.setText(mName);
        address.setText(mAddress);
        if (mPrice != null)
            price.setText(mPrice);
        else
            price.setText(R.string.not_available_field);
        if (mUserRating == null || mUserRating.size() == 0)
            ratingEmpty.setVisibility(View.VISIBLE);
        else {
            for (UserRating ur : mUserRating)
                mRating += ur.getRating();
            mRating /= mUserRating.size();
            rating.setRating(mRating);
            // Set up rating recycler view
            userRatingAdapter = new UserRatingAdapter(mUserRating);
            userRating.setAdapter(userRatingAdapter);
            userRating.setLayoutManager(new LinearLayoutManager(ParkingLotActivity.this));
        }
    }
}
