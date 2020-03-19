package com.example.mapsautocomplete;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mapsautocomplete.ParkingLot.ParkingLot;
import com.example.mapsautocomplete.UserRating.UserRating;
import com.example.mapsautocomplete.UserRating.UserRatingAdapter;

import java.util.ArrayList;

public class ParkingLotActivity extends AppCompatActivity {

    private ParkingLot item;

    // Collapsing toolbar
    private Toolbar toolbar;
    private ViewPager viewPager;
    private String[] imageUrls = new String[]{
            "https://cdn.pixabay.com/photo/2016/11/11/23/34/cat-1817970_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/12/21/12/26/glowworm-3031704_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/12/24/09/09/road-3036620_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/11/07/00/07/fantasy-2925250_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/10/10/15/28/butterfly-2837589_960_720.jpg"
    };

    // Layout Views
    private TextView name;
    private TextView address;
    private RatingBar rating;
    private TextView openingHours;
    private TextView price;
    private TextView notes;
    private RecyclerView userRating;
    private TextView mRatingEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_lot);

        item = (ParkingLot) getIntent().getSerializableExtra("selectedLot");

        // Get all information
        String mName = item.getTitle();
        String mAddress = item.getAddress();
        float mRating = item.getAverageRating();
        String mPrice = item.getPrice();
        String mOpeningHours = item.getOpeningHours();
        String mNotes = item.getNotes();
        ArrayList<UserRating> mUserRating = item.getUserRating();

        // Get all views in layout
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mName);
        viewPager = findViewById(R.id.gallery_swipe);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

        name = findViewById(R.id.title);
        address = findViewById(R.id.address);
        rating = findViewById(R.id.rating);
        openingHours = findViewById(R.id.opening_hours);;
        price = findViewById(R.id.price);;
        notes = findViewById(R.id.note);;
        userRating = findViewById(R.id.user_rating_list);
        mRatingEmpty = findViewById(R.id.comment_not_available);

        // Set information to views
        toolbar.setTitle(mName);
        toolbar.setSubtitle(mAddress);
        name.setText(mName);
        address.setText(mAddress);
        rating.setRating(mRating);
        if (mPrice != null) price.setText(mPrice);
        else price.setText(R.string.not_available_field);
        if (mOpeningHours != null) openingHours.setText(mOpeningHours);
        else openingHours.setText(R.string.not_available_field);
        if (mNotes != null) notes.setText(mNotes);
        else notes.setText(R.string.not_available_field);

        if (mUserRating == null) {
            mRatingEmpty.setVisibility(View.VISIBLE);
        } else {
            rating.setRating(mRating);

            // Set up rating recycler view
            UserRatingAdapter mAdapter = new UserRatingAdapter(mUserRating);
            userRating.setAdapter(mAdapter);
            userRating.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home || item.getItemId() == R.id.homeAsUp) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
