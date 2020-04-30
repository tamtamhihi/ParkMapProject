package com.example.parkmapproject.main_ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parkmapproject.database.DatabaseViewModel;
import com.example.parkmapproject.database.FirebaseDatabaseHelper;
import com.example.parkmapproject.parkinglot.ParkingLot;
import com.example.parkmapproject.parkinglot.ParkingLotArrayAdapter;
import com.example.parkmapproject.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    // Parking Lot Database
    private DatabaseViewModel databaseViewModel;

    // Search bar
    private EditText mSearchBar;
    private ImageView clearSearch;
    private SwipeRefreshLayout refreshLayout;

    // Parking lot list
    private RecyclerView mParkingLotRecycler;
    private ParkingLotArrayAdapter mAdapter;

    // Database
    private ArrayList<ParkingLot> mParkingLotList;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        databaseViewModel = new ViewModelProvider(getActivity()).get(DatabaseViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSearchBar = root.findViewById(R.id.search_input);
        clearSearch = root.findViewById(R.id.search_clear);
        refreshLayout = root.findViewById(R.id.refresh);
        mParkingLotRecycler = root.findViewById(R.id.park_list);

        setupFilterableSearchBar();
        setupRefreshLayout();
        setupParkingLotDatabase();
        return root;
    }

    @Override
    public void onResume() {
        new FirebaseDatabaseHelper().readParkingLots(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys) {
                mParkingLotList.clear();
                mParkingLotList.addAll(parkingLots);
                mAdapter.notifyDataSetChanged();
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
        super.onResume();
    }

    /*
    Get the parking lots database from the DatabaseViewModel.
    */
    private void setupParkingLotDatabase() {
        databaseViewModel.getDatabase().observe(getViewLifecycleOwner(), parkingLots -> {
            mParkingLotList = new ArrayList<>(parkingLots);
            if (mAdapter == null) {
                mAdapter = new ParkingLotArrayAdapter(getContext(), mParkingLotList);
                mParkingLotRecycler.setAdapter(mAdapter);
                mParkingLotRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
            }
            else
                mAdapter.notifyDataSetChanged();
            int count = 0;
            for (ParkingLot parkingLot : mParkingLotList) {
                if (parkingLot.getPlacePhotos() != null && parkingLot.getPlacePhotos().size() > 0)
                    count++;
            }
        });
    }

    private void setupRefreshLayout() {
        refreshLayout.setOnRefreshListener(() -> {
            new FirebaseDatabaseHelper().readParkingLots(new FirebaseDatabaseHelper.DataStatus() {
                @Override
                public void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys) {
                    mParkingLotList.clear();
                    mParkingLotList.addAll(parkingLots);
                    mAdapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);
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
        });

    }

    /*
    When the user clicks the clear button, the input is emptied. As the user types in, the parking
    lots in the recycler view will be filtered correspondingly.
    */
    private void setupFilterableSearchBar() {
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchBar.setText("");
            }
        });
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
    }
}
