package com.example.parkmapproject.main_ui.maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parkmapproject.R;
import com.example.parkmapproject.parkinglot.ParkingLot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SuggestionArrayAdapter extends
        RecyclerView.Adapter<SuggestionArrayAdapter.SuggestionViewHolder> {
    private final Context context;
    private ArrayList<ParkingLot> suggestedParkingLot;
    private Polyline currentPath = null;
    private LatLng yourLocation;
    private GoogleMap map;
    private int selectedPosition;
    private String apiKey;
    private BottomSheetBehavior suggestionBottomSheet;
    private ClusterManager<ParkingLot> clusterManager;

    public SuggestionArrayAdapter(@NonNull Context context, ArrayList<ParkingLot> list, Polyline currentPath,
                                  LatLng yourLocation, GoogleMap map, BottomSheetBehavior bottomSheet,
                                  String apiKey, ClusterManager<ParkingLot> clusterManager) {
        this.context = context;
        this.suggestedParkingLot = list;
        this.currentPath = currentPath;
        this.yourLocation = yourLocation;
        this.map = map;
        this.suggestionBottomSheet = bottomSheet;
        this.apiKey = apiKey;
        this.clusterManager = clusterManager;
        selectedPosition = 0;
    }

    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.suggested_parkinglot, parent, false);
        return new SuggestionViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        holder.thumbnail.setImageDrawable(context.getDrawable(R.drawable.no_image));
        ParkingLot bindedParkingLot = suggestedParkingLot.get(position);
        String name = bindedParkingLot.getName();
        String address = bindedParkingLot.getAddress();
        int distance = (int)(bindedParkingLot.getDistanceFromSelected() * 1000);
        holder.name.setText(name);
        holder.address.setText(address);
        holder.distance.setText(distance + "m");
        if (bindedParkingLot.getPlacePhotos() != null && bindedParkingLot.getPlacePhotos().size() > 0) {
            Glide.with(context).load(bindedParkingLot.getPlacePhotos().get(0)).centerCrop().into(holder.thumbnail);
        }
        if (bindedParkingLot.isSuggested())
            holder.isSelected.setVisibility(View.VISIBLE);
        else
            holder.isSelected.setVisibility(View.GONE);
    }
    @Override
    public int getItemCount() {
        return suggestedParkingLot.size();
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        SuggestionArrayAdapter adapter;
        TextView name;
        TextView address;
        ImageView thumbnail;
        ImageView isSelected;
        TextView distance;
        public SuggestionViewHolder(@NonNull View itemView, SuggestionArrayAdapter adapter) {
            super(itemView);
            name = itemView.findViewById(R.id.suggested_name);
            address = itemView.findViewById(R.id.suggested_address);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            isSelected = itemView.findViewById(R.id.is_selected);
            distance = itemView.findViewById(R.id.distance);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }
        /*
        When a new suggestion is clicked, the "suggested" properties of the former and the new
        parking lot will be changed, a new route polyline is shown and the bottom sheet is set
        to collapse.
         */
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            if (clickedPosition == selectedPosition)
                return;
            suggestedParkingLot.get(selectedPosition).setSuggested(false);
            selectedPosition = clickedPosition;
            suggestedParkingLot.get(selectedPosition).setSuggested(true);
            notifyDataSetChanged();
            showPath(yourLocation, suggestedParkingLot.get(clickedPosition).getPosition());
            suggestionBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /*
    This function clears the former route and shows the new route polyline from starting to ending
    LatLng on the map.
    */
    private void showPath(LatLng start, LatLng end) {
        if (currentPath != null)
            currentPath.remove();
        List<LatLng> path = new ArrayList<>();
        LatLng boundNorthEast = new LatLng(0, 0), boundSouthWest = new LatLng(0, 0);
        String origin = String.valueOf(start.latitude) + ","
                +String.valueOf(start.longitude);
        String destination = String.valueOf(end.latitude) + ","
                +String.valueOf(end.longitude);
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
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
            currentPath = map.addPolyline(options);
            moveCameraToBound(boundNorthEast, boundSouthWest, 15);
        }
    }

    /*
    This function moves to camera to the LatLng Bounds of the route.
     */
    private void moveCameraToBound(LatLng boundNorthEast, LatLng boundSouthWest, int zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder()
                .include(boundNorthEast).include(boundSouthWest).build(), zoom));
    }

    /*
    This function changes the "suggested" property of 2 parking lots in case the user clicks a new
    cluster item. The former suggested parking lot's "suggested" boolean will be set to false when
    the clicked parking lot's will be set to true.
     */
    public void changeSuggestion(ParkingLot newSuggestion) {
        for (int i = 0; i < suggestedParkingLot.size(); ++i) {
            if (suggestedParkingLot.get(i) == newSuggestion) {
                suggestedParkingLot.get(selectedPosition).setSuggested(false);
                if (currentPath != null)
                    currentPath.remove();
                suggestedParkingLot.get(i).setSuggested(true);
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    /*
    This function changes the current route for easy tracking later in case the user clicks another
    cluster item that will show a different route.
     */
    public void setCurrentPath(Polyline newPath) {
        currentPath = newPath;
    }

    /*
    This function is used to delete the current route in case the user clicks the clear button in
    Autocomplete or clicks the GPS button, which both make the camera move to user's current location.
     */
    public void removeCurrentPath() {
        if (currentPath != null)
            currentPath.remove();
    }
}

