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

import com.example.parkmapproject.database.DataAdapter;
import com.example.parkmapproject.database.DatabaseViewModel;
import com.example.parkmapproject.parkinglot.ParkingLot;
import com.example.parkmapproject.parkinglot.ParkingLotArrayAdapter;
import com.example.parkmapproject.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    // Parking Lot Database
    private DatabaseViewModel databaseViewModel;

    // Search bar
    private EditText mSearchBar;
    private ImageView clearSearch;

    // Parking lot list
    private RecyclerView mParkingLotRecycler;
    private ParkingLotArrayAdapter mAdapter;

    // Database
    private FirebaseDatabase firebaseDatabase;
    private DataAdapter mDbHelper;
    private ArrayList<ParkingLot> mParkingLotList;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        databaseViewModel = new ViewModelProvider(getActivity()).get(DatabaseViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSearchBar = root.findViewById(R.id.search_input);
        clearSearch = root.findViewById(R.id.search_clear);
        mParkingLotRecycler = root.findViewById(R.id.park_list);

        setUpFilterableSearchBar();
        setupParkingLotDatabase();
        return root;
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
            Log.d("Line112DB", String.valueOf(count));
        });
    }

    /*
    When the user clicks the clear button, the input is emptied. As the user types in, the parking
    lots in the recycler view will be filtered correspondingly.
    */
    private void setUpFilterableSearchBar() {
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
