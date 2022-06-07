package com.feipai.flypai.ui.view.mapbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.CALLOUT_LAYER_TAG;

public class MapboxWayPointModel {

    /**
     * 总里程
     */
    private float totalMileages;

    /**
     * 航点
     */
    private List<MapboxMarker> waypointMarkers = new ArrayList<>();

    /**
     * 航点线
     */
    private List<MapboxLine> waypointPolylines = new ArrayList<>();//航点线段绘制


    private int tagIndex = 0;

    public int getLastIndex() {
        return tagIndex;
    }

    public void addWaypointMarker(MapboxMarker marker) {
        tagIndex++;
        waypointMarkers.add(marker);
    }

    public void removeWaypointMarker(MapboxMarker marker) {
        if (waypointMarkers.size() > 0) {
            Iterator it = waypointMarkers.iterator();
            while (it.hasNext()) {
                MapboxMarker ma = (MapboxMarker) it.next();
                if (marker.getLatLng().equals(ma.getLatLng())) {
                    it.remove();
                    break;
                }
            }
        }
    }

    public void addLines(MapboxLine polyline) {
        waypointPolylines.add(polyline);
    }

    public void removeAllLine() {
        if (waypointPolylines.size() > 0) {
            for (MapboxLine line : waypointPolylines) {
//                line.remove();
            }
            waypointPolylines.clear();
        }
    }

    public void removeAllMarker() {
        if (waypointMarkers.size() > 0) {
            waypointMarkers.clear();
        }
        tagIndex = 0;
    }

    public void setMileages(float mileages) {
        totalMileages += mileages;
    }


    public float getTotalMileages() {
        return totalMileages;
    }

    public void setTotalMileages(float totalMileages) {
        this.totalMileages = totalMileages;
    }

    public List<MapboxMarker> getWaypointMarkers() {
        return waypointMarkers;
    }

    public void setWaypointMarkers(List<MapboxMarker> waypointMarkers) {
        this.waypointMarkers = waypointMarkers;
    }

    public List<MapboxLine> getWaypointPolylines() {
        return waypointPolylines;
    }

    public void setWaypointPolylines(List<MapboxLine> waypointPolylines) {
        this.waypointPolylines = waypointPolylines;
    }

    public String[] getWaypointMarkerWindowInfoLayerTags() {
        String[] layerTags = new String[waypointMarkers.size()];
        for (int i = 0; i < waypointMarkers.size(); i++) {
            layerTags[i] = CALLOUT_LAYER_TAG + waypointMarkers.get(i).getMarkerTag();
        }
        return layerTags;
    }
}
