package com.example.mapsautocomplete.ui.maps;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapsautocomplete.Database.DataAdapter;
import com.example.mapsautocomplete.ParkingLot.ParkingLot;

import java.util.ArrayList;

public class MapsViewModel extends ViewModel {

    private MutableLiveData<ArrayList<ParkingLot>> mParkingLot;

    public MapsViewModel() {
        if (mParkingLot == null) {
            mParkingLot = new MutableLiveData<>();
            loadParkingLotDatabase();
        }
    }

    private void loadParkingLotDatabase() {

    }

    public LiveData<ArrayList<ParkingLot>> getParkingLotDatabase() {
        return mParkingLot;
    }
}