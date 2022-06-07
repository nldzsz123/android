package com.feipai.flypai.beans;

import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaypointModel  {
    /**
     * 总里程
     */
    private float totalMileages;

    /**
     * 航点
     */
    private List<Marker> waypointMarkers = new ArrayList<>();

    /**
     * 航点线
     */
    private List<Polyline> waypointPolylines = new ArrayList<>();//航点线段绘制

    public void addWaypointMarker(Marker marker) {
        waypointMarkers.add(marker);
    }

    public void removeWaypointMarker(Marker marker) {
        if (waypointMarkers.size() > 0) {
            Iterator it = waypointMarkers.iterator();
            while (it.hasNext()) {
                Marker ma = (Marker) it.next();
                if (marker.getTitle().equals(ma.getTitle())) {
                    it.remove();
                    marker.remove();
                    break;
                }
            }
        }
    }

    public void addLines(Polyline polyline) {
        waypointPolylines.add(polyline);
    }

    public void removeAllLine() {
        if (waypointPolylines.size() > 0) {
            for (Polyline line : waypointPolylines) {
                line.remove();
            }
            waypointPolylines.clear();
        }
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

    public List<Marker> getWaypointMarkers() {
        return waypointMarkers;
    }

    public void setWaypointMarkers(List<Marker> waypointMarkers) {
        this.waypointMarkers = waypointMarkers;
    }

    public List<Polyline> getWaypointPolylines() {
        return waypointPolylines;
    }

    public void setWaypointPolylines(List<Polyline> waypointPolylines) {
        this.waypointPolylines = waypointPolylines;
    }
}
