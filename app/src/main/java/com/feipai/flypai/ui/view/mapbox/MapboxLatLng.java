package com.feipai.flypai.ui.view.mapbox;

import android.location.Location;
import android.os.Parcel;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class MapboxLatLng extends LatLng {

    private double latitude = 0;
    private double longitude = 0;

    public MapboxLatLng() {
        super();
    }

    public MapboxLatLng(double latitude, double longitude) {
        super(latitude, longitude);
    }


    public MapboxLatLng(double latitude, double longitude, double altitude) {
        super(latitude, longitude, altitude);
    }

    public MapboxLatLng(Location location) {
        super(location);
    }

    public MapboxLatLng(LatLng latLng) {
        super(latLng);
    }

    protected MapboxLatLng(Parcel in) {
        super(in);
    }
}
