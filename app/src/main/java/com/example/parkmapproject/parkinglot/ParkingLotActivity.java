package com.example.parkmapproject.parkinglot;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.parkmapproject.R;
import com.example.parkmapproject.database.DatabaseViewModel;
import com.example.parkmapproject.database.FirebaseDatabaseHelper;
import com.example.parkmapproject.userrating.UserRating;
import com.example.parkmapproject.userrating.UserRatingAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class ParkingLotActivity extends AppCompatActivity {
    /*
    Use a database view model to find the current parking lot based on the key sent by the intent to
    avoid sending too much information through an intent.
     */
    private ArrayList<ParkingLot> parkinglotList;
    private String key;
    private ParkingLot item;
    private DatabaseViewModel databaseViewModel;

    /*
    Collapsing Toolbar
     */
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

    // ParkingLot information
    String mName;
    String mAddress;
    float mRating = 0;
    String mPrice;
    ArrayList<UserRating> mUserRating = new ArrayList<>();
    private InputStream uploadStream;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_lot);
        databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        databaseViewModel.getDatabase().observe(this, parkingLots -> {
            parkinglotList = new ArrayList<>(parkingLots);
            key = getIntent().getStringExtra("key");
            if (parkinglotList != null && parkinglotList.size() > 0) {
                for (ParkingLot pl : parkinglotList)
                    if (pl.getKey().equals(key)) {
                        item = pl;
                        break;
                    }
                fetchParkingLotInfo();
                fetchToolbarView();
                fetchInfoView();
                setInfoToView();
            }
        });
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
        placePhotos = item.getPlacePhotos();
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
        viewPager = findViewById(R.id.gallery_swipe);
        imageAdapter = new ViewPagerAdapter(this, placePhotos);
        viewPager.setAdapter(imageAdapter);
        addImageButton = findViewById(R.id.add_image);
        addImageButton.setOnClickListener(v -> {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
            startActivityForResult(chooserIntent, PICK_IMAGE);
        });
    }

    /*
    Upload the image that users choose from storage to Firebase.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null)
                return;
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                uploadStream = stream;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uploadStream != null) {
            Dialog dialog = new Dialog(this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
            dialog.setContentView(R.layout.upload_progress_dialog);
            TextView uploading = dialog.findViewById(R.id.uploading);
            TextView uploadFinish = dialog.findViewById(R.id.finish);
            Button cancelButton = dialog.findViewById(R.id.cancel_button);
            ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);
            dialog.show();
            Bitmap bitmap = BitmapFactory.decodeStream(uploadStream);
            bitmap = getResizedBitmap(bitmap, 1000);
            new FirebaseDatabaseHelper().updateBitmap(bitmap, item.getKey());
            new Handler().postDelayed((Runnable) () -> {
                progressBar.setVisibility(View.GONE);
                uploading.setVisibility(View.GONE);
                uploadFinish.setVisibility(View.VISIBLE);
                cancelButton.setBackground(getDrawable(R.drawable.button_background_selector));
                cancelButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    this.finish();
                });
            }, 4000);
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