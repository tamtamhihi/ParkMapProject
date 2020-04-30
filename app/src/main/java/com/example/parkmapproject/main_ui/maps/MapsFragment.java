package com.example.parkmapproject.main_ui.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkmapproject.R;
import com.example.parkmapproject.database.DatabaseViewModel;
import com.example.parkmapproject.parkinglot.ClusterRenderer;
import com.example.parkmapproject.parkinglot.ParkingLot;
import com.example.parkmapproject.parkinglot.ParkingLotActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private DatabaseViewModel databaseViewModel;

    // Location
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private Marker currentMarker;
    private LatLng myLocation;
    private Polyline showPath = null;

    // Additional widgets
    private FloatingActionButton gpsIcon;
    private AutocompleteSupportFragment autocompleteSupportFragment;

    // Cluster Item
    private ClusterManager<ParkingLot> mClusterManager;
    private ClusterRenderer mClusterRenderer;

    // Database
    private ArrayList<ParkingLot> mParkingLot = new ArrayList<>();
    private ArrayList<ParkingLot> sortingParkingLot = new ArrayList<>();

    // Fragment view
    private View root;

    // Values
    private boolean mLocationPermissionGranted = false;
    private String ERROR_TAG = this.getClass().getSimpleName();
    private String CAM_POS_KEY = "CAMERA_POSITION";
    private static final int DEFAULT_ZOOM = 16;
    private static final int SPECIAL_ZOOM = 18;
    private static final int MY_PERMISSIONS_REQUEST_FIND_LOCATION = 1;
    private static final int DEFAULT_BOUNDARY = 2;

    private LinearLayout layoutBottomSheet;
    private BottomSheetBehavior suggestionBottomSheet;
    private ImageView dismissButton;
    private RelativeLayout header;
    private TextView headerText;
    private RecyclerView suggestionRecyclerView;
    private SuggestionArrayAdapter suggestionAdapter;
    private LatLng startLocation = null;
    private ParkingLot suggestedParkingLot;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        databaseViewModel = new ViewModelProvider(getActivity()).get(DatabaseViewModel.class);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        root = inflater.inflate(R.layout.fragment_maps, container, false);
        setupMap();
        setupGPS();
        setUpSuggestionBottomSheet();
        setupAutocomplete();
        setupParkingLotDatabase();
        if (savedInstanceState != null && mMap != null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition((CameraPosition)
                    savedInstanceState.getParcelable(CAM_POS_KEY)));
        return root;
    }
    private void setupMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);
        }
    }

    /*
    Fetch views from suggestion bottom sheet, include dismiss button, header text, header and a
    recycler view for suggested parking lots.
     */
    private void setUpSuggestionBottomSheet() {
        layoutBottomSheet = root.findViewById(R.id.suggestion_bottomsheet);
        suggestionBottomSheet = BottomSheetBehavior.from(layoutBottomSheet);
        dismissButton = root.findViewById(R.id.dismiss_bottomsheet);
        header = root.findViewById(R.id.suggestion_header);
        headerText = root.findViewById(R.id.suggestion_header_text);
        suggestionRecyclerView = root.findViewById(R.id.suggested_parkinglots);
    }

    /*
    Get the parking lots database from the DatabaseViewModel.
     */
    private void setupParkingLotDatabase() {
        databaseViewModel.getDatabase().observe(getViewLifecycleOwner(), parkingLots -> {
            mParkingLot = new ArrayList<>(parkingLots);
            sortingParkingLot = new ArrayList<>(parkingLots);
        });
    }

    /*
    When the user select a place from Autocomplete, the camera is moved to the selected location,
    the nearest parking lots within 2km (maximum 10) will be shown on the map as cluster items and
    in a bottom sheet, and a polyline appears showing the route to the nearest parking lot. As you
    click on a new parking lot option in the bottom sheet or click on the cluster item on the map,
    the map will show the route to the chosen parking lot.

    When the clear button of Autocomplete is clicked, the camera is moved to the current location of
    user and all cluster items, polylines and bottom sheet will disappear.
     */
    private void setupAutocomplete() {
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.autocomplete);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS));
        autocompleteSupportFragment.setHint(getString(R.string.autocomplete_hint));
        EditText autocompleteEdit = (EditText)autocompleteSupportFragment.getView()
                .findViewById(R.id.places_autocomplete_search_input);
        autocompleteEdit.setTextAppearance(R.style.textCaption);
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                closeSuggestion();
                mClusterManager.clearItems();
                LatLng selectedLatLng = place.getLatLng();
                startLocation = selectedLatLng;
                moveCamera(selectedLatLng, SPECIAL_ZOOM);
                removeOldMarker();
                addNewMarker(selectedLatLng);
                closestParkingLots(selectedLatLng, DEFAULT_BOUNDARY);
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.e(ERROR_TAG, "Autocomplete error");
            }
        });
        autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_clear_button)
                .setOnClickListener(view -> {
                    autocompleteEdit.setText("");
                    view.setVisibility(View.GONE);
                    mClusterManager.clearItems();
                    removeOldMarker();
                    suggestionAdapter.removeCurrentPath();
                    closeSuggestion();
                    if (myLocation != null)
                        moveCamera(myLocation, DEFAULT_ZOOM);
                });
    }

    /*
    When the GPS icon is clicked, the camera is moved to the current location of user and the nearest
    parking lots will be shown as if the place was selected in Autocomplete.
    */
    private void setupGPS() {
        // Add listener for gps icon
        gpsIcon = root.findViewById(R.id.gps);
        gpsIcon.setOnClickListener(v -> {
            if (myLocation == null) {
                AlertDialog noLocation = new AlertDialog.Builder(getContext()).create();
                noLocation.setTitle(getString(R.string.no_location_title));
                noLocation.setMessage(getString(R.string.no_location_message));
                noLocation.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.no_location_button),
                        (dialog, which) -> noLocation.dismiss());
                noLocation.show();
                return;
            }
            clearSearchInput();
            startLocation = myLocation;
            if (suggestionAdapter != null)
                suggestionAdapter.removeCurrentPath();
            removeOldMarker();
            mClusterManager.clearItems();
            mClusterManager.cluster();
            closestParkingLots(myLocation, DEFAULT_BOUNDARY);
        });
    }

    /*
    After the application is started and the Google Map is ready, it is assigned to the variable
    mMap. Cluster Manager is set up to manage the cluster items that will appear on the map. The
    application will also check for location permission and move the camera to the current position
    of the user.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setIndoorEnabled(false);
        setUpClusterManager();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    /*
    This function sets up the Cluster Manager and attach it to the Google Map. The cluster items
    will only appear when the user 1 - select a place from Autocomplete or 2 - click the GPS button.
    When the user clicks on the cluster item, the map will show a new route polyline to the
    corresponding parking lot and an infowindow will appear. When the user clicks that infowindow,
    a new parking lot activity will appear.
     */
    private void setUpClusterManager() {
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        mClusterRenderer = new ClusterRenderer(getContext(), mMap, mClusterManager);
        mClusterManager.setRenderer(mClusterRenderer);
        mClusterManager.setOnClusterItemClickListener(parkingLot -> {
            suggestionAdapter.changeSuggestion(parkingLot);
            suggestedParkingLot = parkingLot;
            showPath(startLocation, suggestedParkingLot.getPosition());
            suggestionAdapter.setCurrentPath(showPath);
            suggestionBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return false;
        });
        mClusterManager.setOnClusterItemInfoWindowClickListener(parkingLot -> {
            Intent intent = new Intent(getContext(), ParkingLotActivity.class);
            intent.putExtra("key", parkingLot.getKey());
            startActivity(intent);
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
        });
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    /*
    This function encodes bitmaps to strings to send to the ParkingLotActivity.
     */
    private String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmap.recycle();
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /*
    This function checks if the location permission has been granted. If not, it will show a request
    to the user.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext())
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FIND_LOCATION);
        } else {
            mLocationPermissionGranted = true;
        }
    }

    /*
    Response after requesting location permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FIND_LOCATION:
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionGranted = true;
        }
        updateLocationUI();
    }

    /*
    This function update the UI related to locations.
     */
    private void updateLocationUI() {
        if (mMap == null)
            return;
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /*
    This function runs a task to get the user's current location and animate camera to that location.
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null) {
                                myLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                moveCamera(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
                                removeOldMarker();
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    /*
    Animate camera from the current camera position to the chosen LatLng.
     */
    private void moveCamera(LatLng latLng, int zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /*
    Animate camera from the current camera position to the new LatLng Bounds.
     */
    private void moveCameraToBound(LatLng boundNorthEast, LatLng boundSouthWest, int zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder()
                .include(boundNorthEast).include(boundSouthWest).build(), zoom));
    }

    /*
    Clear the Autocomplete Input.
     */
    private void clearSearchInput() {
        autocompleteSupportFragment.setText("");
    }

    /*
    Remove the current active marker.
     */
    private void removeOldMarker() {
        if (currentMarker != null) {
            currentMarker.remove();
            currentMarker = null;
        }
    }

    /*
    Add new marker at the given LatLng.
     */
    private void addNewMarker(LatLng latLng) {
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
    }

    /*
    This function clears all items on map (including parking lot cluster items, polylines, marker)
    and the former suggestion bottom sheet, search through parking lots database to find the nearest
    parking lots to given location, show a new bottom sheet with those suggestions and show the route
    from given location to the nearest one. If there are no parking lots within 2km to given location,
    an alert dialog will be shown.
    When the suggestion bottom sheet is cancel, all items on map and the bottom sheet will be cleared
    and the camera is moved back to the current location.
     */
    private void closestParkingLots(LatLng yourLocation, int boundary) {
        closeSuggestion();
        for (ParkingLot parkingLot : sortingParkingLot) {
            parkingLot.setDistanceFromSelected(yourLocation);
        }
        Collections.sort(sortingParkingLot);
        int MAX_ENTRY = 10;
        int availableParkingLots = 0;
        for (int i = 0; i < MAX_ENTRY; ++i) {
            if (sortingParkingLot.get(i).getDistanceFromSelected() > boundary)
                break;
            sortingParkingLot.get(i).setSuggested(false);
            mClusterManager.addItem(sortingParkingLot.get(i));
            availableParkingLots++;
        }
        if (availableParkingLots == 0) {
            AlertDialog noSuggestion = new AlertDialog.Builder(getContext()).create();
            noSuggestion.setTitle(getString(R.string.no_suggestion_title));
            noSuggestion.setMessage(getString(R.string.no_suggestion_message));
            noSuggestion.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.no_suggestion_button),
                    (dialog, which) -> noSuggestion.dismiss());
            noSuggestion.show();
            if (myLocation != null)
                moveCamera(myLocation, DEFAULT_ZOOM);
            return;
        }
        mClusterManager.cluster();
        sortingParkingLot.get(0).setSuggested(true);
        showPath(yourLocation, sortingParkingLot.get(0).getPosition());
        // Suggestion Bottom Sheet
        headerText.setText(String.format("%d search results", availableParkingLots));
        suggestionAdapter = new SuggestionArrayAdapter(getContext(), new ArrayList<>(sortingParkingLot
                .subList(0, availableParkingLots)), showPath, yourLocation, mMap,
                suggestionBottomSheet, getString(R.string.google_maps_key), mClusterManager);
        suggestionRecyclerView.setAdapter(suggestionAdapter);
        suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutBottomSheet.setVisibility(View.VISIBLE);
        suggestionBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        dismissButton.setOnClickListener(v -> {
            mClusterManager.clearItems();
            removeOldMarker();
            if (showPath != null)
                showPath.remove();
            if (suggestionAdapter != null)
                suggestionAdapter.removeCurrentPath();
            closeSuggestion();
            clearSearchInput();
            if (myLocation != null)
                moveCamera(myLocation, DEFAULT_ZOOM);
        });
        header.setOnClickListener(v -> {
            if (suggestionBottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                suggestionBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                suggestionBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    /*
    This function shows the route polyline from starting to ending LatLng on the map.
     */
    private void showPath(LatLng start, LatLng end) {
        if (showPath != null)
            showPath.remove();
        List<LatLng> path = new ArrayList<>();
        LatLng boundNorthEast = new LatLng(0, 0), boundSouthWest = new LatLng(0, 0);
        String origin = String.valueOf(start.latitude) + ","
                +String.valueOf(start.longitude);
        String destination = String.valueOf(end.latitude) + ","
                +String.valueOf(end.longitude);
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
        DirectionsApiRequest request = DirectionsApi.getDirections(
                context, origin, destination);
        try {
            DirectionsResult result = request.await();
            if (result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                if (route.bounds != null) {
                    boundNorthEast = new LatLng(route.bounds.northeast.lat, route.bounds.northeast.lng);
                    boundSouthWest = new LatLng(route.bounds.southwest.lat, route.bounds.southwest.lng);
                }
                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; ++i) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; ++j) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; ++k) {
                                        DirectionsStep finalStep = step.steps[k];
                                        EncodedPolyline point = finalStep.polyline;
                                        if (point != null) {
                                            List<com.google.maps.model.LatLng> coords = point.decodePath();
                                            for (com.google.maps.model.LatLng coord : coords)
                                                path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                                else {
                                    EncodedPolyline point = step.polyline;
                                    if (point != null) {
                                        List<com.google.maps.model.LatLng> coords = point.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords)
                                            path.add(new LatLng(coord.lat, coord.lng));
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (InterruptedException | IOException | ApiException e) {
            e.printStackTrace();
        }
        if (path.size() > 0) {
            PolylineOptions options = new PolylineOptions().addAll(path);
            showPath = mMap.addPolyline(options);
            moveCameraToBound(boundNorthEast, boundSouthWest, DEFAULT_ZOOM);
        }
    }

    /*
    This function closes the suggestion bottom sheet.
     */
    private void closeSuggestion() {
        layoutBottomSheet.setVisibility(View.GONE);
    }
}