package com.feipai.flypai.ui.view.mapbox;

public class MapBoxTag {
    private static final String GEN_SRC_TAG = "GEOJSON_SOURCE_TAG_";
    public static final String IMAGE_TAG = GEN_SRC_TAG + "MARKER_IMAGE_TAG";
    public static final String MARKER_LAYER_TAG = GEN_SRC_TAG + "MARKER_LAYER_TAG";
    public static final String LINE_LAYER_TAG = GEN_SRC_TAG + "LINE_LAYER_TAG";
    public static final String LAYER_TAG = GEN_SRC_TAG + "LAYER_TAG";
    public static final String POLYGON_TAG = GEN_SRC_TAG + "POLYGON_TAG";//圆
    public static final String FILL_LAYER_TAG = GEN_SRC_TAG + "FILL_LAYER_TAG";//填充色
    public static final String CALLOUT_LAYER_TAG = GEN_SRC_TAG + "CALLOUT_LAYER_TAG";

    /**
     * marker图层唯一标识，以经纬度作为过滤器
     */
    public static final String MARKER_FILTER_LATLNG = "MARKER_FILTER_LATLNG";

    public static final String PROPERTY_NAME = GEN_SRC_TAG + "name";
    public static final String PROPERTY_SELECTED = GEN_SRC_TAG + "selected";

    public static final String TAKE_OFF = GEN_SRC_TAG + "TAKE_OFF";
    public static final String PLANE_ICON = GEN_SRC_TAG + "PLANE_ICON";
    public static final String AROUND_MARKER = GEN_SRC_TAG + "AROUND_MARKER";
    public static final String WAYPOINT_MARRKER = GEN_SRC_TAG + "WAYPOINT_MAKRER";
    public static final String LOCATION_MARKER = GEN_SRC_TAG + "LOCATION_MARKER";


    //Line Tag====================================================
    public static final String PLANE_LINE = GEN_SRC_TAG + "PLANE_LINE";
    public static final String WAYPOINT_LINE = GEN_SRC_TAG + "WAYPOINT_LINE";
    public static final String AROUND_LINE = GEN_SRC_TAG + "AROUND_LINE";
    public static final String FIND_PLANE_LINE_FOR_TAKE_OFF = GEN_SRC_TAG + "FIND_PLANE_LINE_FOR_TAKE_OFF";
    public static final String FIND_PLANE_LINE_FOR_PHONE = GEN_SRC_TAG + "FIND_PLANE_LINE_FOR_PHONE";


    //Circle Tag====================================================
    public static final String AROUND_CIRCLE = GEN_SRC_TAG + "AROUND_CIRCLE";
    public static final String HIDDEN_RANGE_CIRCLE = GEN_SRC_TAG + "HIDDEN_RANGE_CIRCLE";
    public static final String HIDDEN_RANGE_FILL_CIRCLE = GEN_SRC_TAG + "HIDDEN_RANGE_FILL_CIRCLE";

    //location marker tag
    public static final String INFO_WINDOW_FOR_LOCATION_TAG = CALLOUT_LAYER_TAG + "INFO_WINDOW_FOR_LOCATION_TAG";


}
