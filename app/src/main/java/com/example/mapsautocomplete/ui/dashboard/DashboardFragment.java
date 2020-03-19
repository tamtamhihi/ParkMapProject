package com.example.mapsautocomplete.ui.dashboard;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapsautocomplete.Database.DataAdapter;
import com.example.mapsautocomplete.Database.FirebaseDatabaseHelper;
import com.example.mapsautocomplete.ParkingLot.ParkingLot;
import com.example.mapsautocomplete.ParkingLot.ParkingLotArrayAdapter;
import com.example.mapsautocomplete.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private DashboardViewModel dashboardViewModel;

    // Search bar
    private EditText mSearchBar;
    private ImageView clearSearch;

    // Parking lot list
    private RecyclerView mParkingLotRecycler;
    private ParkingLotArrayAdapter mAdapter;

    // Database
    private FirebaseDatabase firebaseDatabase;
    private DataAdapter mDbHelper;
    private ArrayList<ParkingLot> mParkingLotList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(getActivity()).get(DashboardViewModel.class);

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSearchBar = root.findViewById(R.id.search_input);
        clearSearch = root.findViewById(R.id.search_clear);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchBar.setText("");
            }
        });
        setupParkingLotDatabase();

        // Setup RecyclerView
        mParkingLotRecycler = root.findViewById(R.id.park_list);
        mAdapter = new ParkingLotArrayAdapter(getContext(), mParkingLotList);
        mParkingLotRecycler.setAdapter(mAdapter);
        mParkingLotRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up filter search
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                mAdapter.getFilter().filter(query);
            }
        });

        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mAdapter.getFilter().filter(mSearchBar.getText());
                }
                return false;
            }
        });

        return root;
    }
//    Todo: Turn SQLite Database into Firebase dependencies
    private void setupParkingLotDatabase() {

        new FirebaseDatabaseHelper().readParkingLots(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys) {
                mParkingLotList = parkingLots;
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

        /*

        mDbHelper = new DataAdapter(getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        Cursor data = mDbHelper.getTestData();

        data.moveToFirst();
        do {
            // NEED EDITING
            String parkingTitle = data.getString(5);
            double parkingLat = data.getDouble(2);
            double parkingLng = data.getDouble(3);
            //double parkingPrice = data.getDouble(4);
            String parkingSnippet = data.getString(1);
            ParkingLot item = new ParkingLot(parkingLat, parkingLng, parkingTitle, parkingSnippet);
            mParkingLotList.add(item);
        } while (data.moveToNext());
        data.close();

        mDbHelper.close();

         */
    }
}
