package com.feipai.flypai.ui.view.mapbox;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapboxLinesManager {

    private MapboxMap mapboxMap;
    private Map<String, MapboxLine> lines = new HashMap<>();
    private String lineTag;

    public MapboxLinesManager(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    public MapboxLine addLineToMap(String lineTag, boolean isDotted, LatLng oldLatlng, LatLng newLatlng) {
        this.lineTag = lineTag;
        MapboxLine line = new MapboxLine(mapboxMap, lineTag);
        line.setLineColor(isDotted ? R.color.color_f34235 : R.color.color_4097e1)
                .setLineWidth(3)
                .setLineDotted(isDotted)
                .createLine(oldLatlng, newLatlng);
        lines.put(lineTag, line);
        return line;
    }

    /**
     * 更新line坐标
     */
    public MapboxLine refreshLine(String lineTag, boolean isDotted, LatLng oldLatlng, LatLng newLatlng) {
        GeoJsonSource geoJsonSource = mapboxMap.getStyle().getSourceAs(lineTag);
        MapboxLine line = lines.get(lineTag);
        if (geoJsonSource != null && line != null) {
            line.setLineDotted(isDotted);
            line.createLine(oldLatlng, newLatlng);
            lines.put(lineTag, line);
        }
        return line;
    }

    public boolean isLineAdded(String tag) {
        MapboxLine line = lines.get(tag);
        if (mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded()) {
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(tag);
            return line != null && source != null;
        }
        return false;
    }

    /**
     * 从地图移除Line
     *
     * @param lineTag 唯一标识
     */
    public void removeLineByTagFormMap(String lineTag) {
        for (Iterator<Map.Entry<String, MapboxLine>> it = lines.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MapboxLine> item = it.next();
            if (StringUtils.equals(lineTag, item.getKey())) {
                item.getValue().removeByTag(lineTag);
                it.remove();
            }
        }
        if (mapboxMap.getStyle() != null)
            mapboxMap.getStyle().removeSource(lineTag);
    }

    public void changeMapStyle() {
        lines.clear();
    }
}
