package com.feipai.flypai.ui.view.mapbox;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.turf.TurfTransformation;

import static com.mapbox.turf.TurfConstants.UNIT_METERS;

public class MapBoxUtils {

    public static MapboxLatLng convertToLatLng(Feature feature) {
        Point symbolPoint = (Point) feature.geometry();
        return new MapboxLatLng(symbolPoint.latitude(), symbolPoint.longitude());
    }

    public static Polygon getTurfPolygon(@NonNull double radius, LatLng centerLatLng) {
        Point centerPoint = Point.fromLngLat(centerLatLng.getLongitude(), centerLatLng.getLatitude());
        return TurfTransformation.circle(centerPoint, radius, 360, UNIT_METERS);
    }


    /**
     * Generate a Bitmap from an Android SDK View.
     *
     * @param view the View to be drawn to a Bitmap
     * @return the generated bitmap
     */
    public static Bitmap generate(@NonNull View view) {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(measureSpec, measureSpec);

        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();

        view.layout(0, 0, measuredWidth, measuredHeight);
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static String distanceBetweenMarkers(MapboxMarker startMarker, MapboxMarker endMarker) {
        if (startMarker == null || endMarker == null || startMarker.getLatLng() == null || endMarker.getLatLng() == null)
            return null;
        return String.format("%.1f m", startMarker.getLatLng().distanceTo(endMarker.getLatLng()));
    }


    public static String distanceBetweenLatLngs(MapboxLatLng startLatLng, MapboxLatLng endLatLng) {
        if (startLatLng == null || endLatLng == null)
            return null;
        return String.format("%.1f m", startLatLng.distanceTo(endLatLng));
    }

    public static String distanceBetweenMarkerLatLng(MapboxMarker startMarker, MapboxLatLng endLatLng) {
        if (startMarker == null || startMarker.getLatLng() == null || endLatLng == null)
            return null;
        return String.format("%.1f m", startMarker.getLatLng().distanceTo(endLatLng));
    }


}
