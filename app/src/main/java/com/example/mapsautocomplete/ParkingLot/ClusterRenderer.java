package com.example.mapsautocomplete.ParkingLot;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mapsautocomplete.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class ClusterRenderer extends DefaultClusterRenderer<ParkingLot> {
    private final IconGenerator iconGenerator;
    private final ImageView parkingIcon;
    private final int markerWidth, markerHeight;
    private Context mContext;

    public ClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);

        iconGenerator = new IconGenerator(context);
        parkingIcon = new ImageView(context);
        mContext = context;

        markerWidth = (int)context.getResources().getDimension(R.dimen.marker_dimension);
        markerHeight = markerWidth;
        parkingIcon.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int pads = (int)context.getResources().getDimension(R.dimen.marker_padding);
        parkingIcon.setPadding(pads, pads, pads, pads);

        iconGenerator.setContentView(parkingIcon);
    }

    @Override
    protected void onBeforeClusterItemRendered(ParkingLot item, MarkerOptions markerOptions) {
        parkingIcon.setImageResource(R.drawable.ic_parkinglocation);
        //parkingIcon.setBackgroundColor(Integer.parseInt("#00ff0000"));

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
                .title(item.getTitle())
                .snippet(item.getSnippet());
    }
}
