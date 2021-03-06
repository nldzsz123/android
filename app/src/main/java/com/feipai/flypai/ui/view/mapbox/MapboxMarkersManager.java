package com.feipai.flypai.ui.view.mapbox;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.feipai.flypai.R;
import com.feipai.flypai.api.MapChangeListener;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.ui.view.Camera.MapViewHelper;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.turf.TurfMeasurement;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.AROUND_LINE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.AROUND_MARKER;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.FIND_PLANE_LINE_FOR_PHONE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.FIND_PLANE_LINE_FOR_TAKE_OFF;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.LOCATION_MARKER;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.MARKER_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PLANE_ICON;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PLANE_LINE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PROPERTY_NAME;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PROPERTY_SELECTED;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.TAKE_OFF;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.WAYPOINT_LINE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.WAYPOINT_MARRKER;
import static com.mapbox.mapboxsdk.location.LocationComponentConstants.FOREGROUND_LAYER;
import static com.mapbox.turf.TurfConstants.UNIT_METERS;

/**
 * mapbox ??????marker???????????????
 * ???????????????EPSG:3857
 */
public class MapboxMarkersManager {

    private MapboxMap mapboxMap;
    private Map<String, MapboxMarker> markers = new HashMap<>();
    //??????marker??????
    private List<SymbolLayer> markerLayers = new ArrayList<>();
    //    private List<Feature> markerFeatureList = new ArrayList<>();
    private Map<String, FeatureCollection> featureCollectionsMap = new HashMap<>();
    private List<String> markerTags = new ArrayList<>();

    private MapboxLatLng planeNewLatlng;//???????????????????????????
    private MapboxLatLng planeOldLatlng;//??????????????????????????????

    private MapboxMarker mLocationMarker;
    private boolean isFindPlaneMode;
    private float distancFromLocToPlane = 0.0f;//??????????????????????????????
    private float mLocHeaderBearing = -1;

    /**
     * ????????????
     */

    private MapboxLinesManager linesManager;
    private MapboxCircleManager circleManager;
    private MapboxCircle hinddenCircle;
    private float aroundRadius = 0;

    private MapboxWayPointModel mWaypointModel;
    private View wayPointView;
    private View locationView;
    private MapboxInfoWindowForPhone mInfoForPhone;
    private MapChangeListener mapChangeListener;

    public MapboxMarkersManager(MapboxMap mapboxMap, MapChangeListener mapChangeListener) {
        this.mapboxMap = mapboxMap;
        this.mapChangeListener = mapChangeListener;
        this.linesManager = new MapboxLinesManager(mapboxMap);
        this.circleManager = new MapboxCircleManager(mapboxMap);
    }

    /**
     * ??????marker?????????
     * ??????info window
     */
    public MapboxMarker addMarkerWithWindowToMap(String markerTag, Bitmap icon, String name, MapboxLatLng latLng) {
        LogUtils.d("??????Marker???Mapbox,markerTag=" + markerTag);
        MapboxMarker marker = new MapboxMarker(mapboxMap, markerTag, true);
        marker.bindIcon(icon)
                .setWindowName(name)
                .createMarker(latLng);
        addMarkerToList(marker);
        return marker;
    }

    /**
     * ??????marker?????????
     * ??????info window
     */
    public MapboxMarker addMarkerWithWindowToMap(String markerTag, int iconId, String name, MapboxLatLng latLng) {
        LogUtils.d("??????Marker???Mapbox,markerTag=" + markerTag);
        MapboxMarker marker = new MapboxMarker(mapboxMap, markerTag, true);
        marker.bindIcon(iconId)
                .setWindowName(name)
                .createMarker(latLng);
        addMarkerToList(marker);
        return marker;
    }

    public MapboxLine addLineToMap(String lineTag, MapboxLatLng startLatLng, MapboxLatLng endLatLng) {
        if (linesManager.isLineAdded(lineTag)) {
            return linesManager.refreshLine(lineTag, false, startLatLng, endLatLng);
        } else {
            return linesManager.addLineToMap(lineTag, false, startLatLng, endLatLng);
        }
    }

    /**
     * ??????marker?????????
     * ?????????info window
     */
    public void addMarkerToMap(String markerTag, int icon, MapboxLatLng latLng) {
        MapboxMarker marker = new MapboxMarker(mapboxMap, markerTag, false);
        marker.bindIcon(icon).createMarker(latLng);
        addMarkerToList(marker);
    }

    public void addMarkerToMap(String markerTag, Bitmap bitmap, MapboxLatLng latLng) {
        MapboxMarker marker = new MapboxMarker(mapboxMap, markerTag, false);
        marker.bindIcon(bitmap).createMarker(latLng);
        addMarkerToList(marker);
    }

    public void addMarkerToMap(String markerTag, Bitmap bitmap, MapboxLatLng latLng, float yaw) {
        MapboxMarker marker = new MapboxMarker(mapboxMap, markerTag, false);
        marker.bindIcon(bitmap).setMarkerYaw(yaw).createMarker(latLng);
        addMarkerToList(marker);
    }

    public void addMarkerToMap(String markerTag, int iconId, MapboxLatLng latLng, float yaw) {
        MapboxMarker marker = new MapboxMarker(mapboxMap, markerTag, false);
        marker.bindIcon(iconId).setMarkerYaw(yaw).createMarker(latLng);
        addMarkerToList(marker);
    }

    private void addMarkerToList(MapboxMarker mapboxMarker) {
        markers.put(mapboxMarker.getMarkerTag(), mapboxMarker);
        markerLayers.add(mapboxMarker.getMarkerLayer());
        featureCollectionsMap.put(mapboxMarker.getMarkerTag(), mapboxMarker.getFeatureCollection());
        String tag = /*mapboxMarker.isBindInfoWindow() ? MARKER_LAYER_TAG : */MARKER_LAYER_TAG + mapboxMarker.getMarkerTag();
        if (!markerTags.contains(tag))
            markerTags.add(tag);
    }

    /**
     * ??????tag??????marker
     * ?????????layer?????????????????????????????????source???image????????????
     */
    public void removeMakerByTag(String tag) {
        if (markers.size() > 0) {
            Iterator<Map.Entry<String, MapboxMarker>> it = markers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, MapboxMarker> markerEntry = it.next();
                String markerTag = markerEntry.getKey();
                if (StringUtils.equals(markerTag, tag)) {
                    it.remove();
                    removeFeatureCollectionFormMap(tag);
                    markerEntry.getValue().removeByTag(tag);
                }
            }
        }
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeSource(tag);
            }
        });
    }

    public void removeLineByTag(String tag) {
        linesManager.removeLineByTagFormMap(tag);
    }

    public void removeCircleByTag(String tag) {
        circleManager.removeCircleByTagFormMap(tag);
    }

    private void removeFeatureCollectionFormMap(String tag) {
        for (Iterator<Map.Entry<String, FeatureCollection>> it = featureCollectionsMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, FeatureCollection> item = it.next();
            if (tag.equals(item.getKey())) {
                it.remove();
                break;
            }
        }
        removeTagFromList(tag);
    }

    private void removeTagFromList(String tag) {
        Iterator it = markerTags.iterator();
        while (it.hasNext()) {
            if (StringUtils.equals((String) it.next(), tag)) {
                it.remove();
                break;
            }
        }
    }

    public void smoothMovePlaneMarker(MapboxLatLng newLat, float planeYaw, float aroundR) {
        planeNewLatlng = newLat;
        this.aroundRadius = aroundR;
        List<MapboxLatLng> latLngs = new ArrayList<>();
        //??????????????????marker??????????????????????????????????????????????????????????????????
        if (mapboxMap == null || mapboxMap.getStyle() == null || !mapboxMap.getStyle().isFullyLoaded())
            return;
        if (planeOldLatlng == null && isMarkerAdded(TAKE_OFF)) {
            MapboxMarker takeOffMarker = markers.get(TAKE_OFF);
            List<Feature> takeOffFeatures = takeOffMarker.getFeatureCollection().features();
            if (!takeOffFeatures.isEmpty()) {
                planeOldLatlng = MapBoxUtils.convertToLatLng(takeOffFeatures.get(0));
            }
        }
        //???????????????????????????????????????????????????????????????
        if (planeOldLatlng == null) {
            planeOldLatlng = planeNewLatlng;
        }
        //???????????????marker???????????????
        if (!isMarkerAdded(PLANE_ICON)) {
            //???????????????????????????????????????????????????
            LogUtils.d("??????????????????=====");
            addMarkerToMap(PLANE_ICON, R.mipmap.plane_map_icon, planeOldLatlng, planeYaw);
        }
        // ??????????????????
        MapboxMarker planMark = markers.get(PLANE_ICON);
        planMark.setRotateAngle(planeYaw);

        //????????????????????????,?????????????????????????????????
//        drawPlaneLine(isFindPlaneMode, newLat);

        //????????????????????????????????????
        planMark.startMarkerMoveAnimator(newLat, null, false, (animatedPosition, showInfoWindow) -> {
            if (mapboxMap.getStyle() == null || !mapboxMap.getStyle().isFullyLoaded())
                return;
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(PLANE_ICON);
            if (source != null) {
                MapboxMarker planeMr = markers.get(PLANE_ICON);
                if (planeMr != null) {
                    //????????????????????????,?????????????????????????????????
                    drawPlaneLine(isFindPlaneMode, animatedPosition);
//                    planeMr.refreshGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
//                            Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude())
//                    )));
                    planeOldLatlng = animatedPosition;

                }
                if (isMarkerAdded(AROUND_MARKER)) {
                    MapboxMarker aroundMarker = markers.get(AROUND_MARKER);
                    if (aroundRadius == 0) {
                        aroundRadius = (float) animatedPosition.distanceTo(aroundMarker.getLatLng());
                    }
                    drawAroundCircle(aroundMarker, planMark, aroundRadius);
                }
            }
        });


        //?????????????????????
        UserBean bean = DBClient.findObjById(UserBean.class, 0);
        if (bean != null) {
            bean.setPlaneLat(newLat.getLatitude());
            bean.setPlaneLng(newLat.getLongitude());
            DBClient.updateObject(bean);
        } else {
            bean = new UserBean();
            bean.setPlaneLat(newLat.getLatitude());
            bean.setPlaneLng(newLat.getLongitude());
            bean.setUserId(0);
            DBClient.addObject(bean);
        }
    }

    public void stopPlaneMove() {
        if (mLocationMarker != null) {
            LogUtils.d("planeMoveAnimator stop");
            mLocationMarker.stopMarkerMoveAnimator();
        }
    }

    public void stopLocationMarkerMove() {
        if (mLocationMarker != null) {
            mLocationMarker.stopMarkerMoveAnimator();
        }
    }

    /**
     * ?????????????????????
     */
    public void addTakeOffMarkerToMap(MapboxLatLng latLng) {
        if (!isMarkerAdded(TAKE_OFF)) {
            addMarkerToMap(TAKE_OFF, R.mipmap.start_fly_point_img, latLng);
        } else {
            MapboxMarker takeOffMarker = markers.get(TAKE_OFF);
//            LogUtils.d("??????????????????====>"+latLng.getLatitude()+"||"+latLng.getLongitude());
            takeOffMarker.updatePosition(latLng.getLatitude(), latLng.getLongitude());
        }

    }

    // ?????????????????????
    public void addAroundCentorMarkerToMap(MapboxLatLng latLng) {
        if (!isMarkerAdded(AROUND_MARKER)) {
            LogUtils.d("?????????????????????");
            addMarkerToMap(AROUND_MARKER, R.mipmap.around_center_icon, latLng);
        } else {
            LogUtils.d("?????????????????????");
            MapboxMarker aroundMarker = markers.get(AROUND_MARKER);
            aroundMarker.updatePosition(latLng.getLatitude(), latLng.getLongitude());
        }
    }

    public MapboxWayPointModel addWayPointToMap(Activity ac, MapboxLatLng latLng, float fanceRadius) {
//        CameraPosition position = new CameraPosition.Builder()
//                .target(latLng) // Sets the new camera position
//                .zoom(17) // Sets the zoom
//                .bearing(180) // Rotate the camera
//                .tilt(30) // Set the camera tilt
//                .build(); // Creates a CameraPosition from the builder
//
//        mapboxMap.animateCamera(CameraUpdateFactory
//                .newCameraPosition(position), 7000);
        if (mWaypointModel == null)
            mWaypointModel = new MapboxWayPointModel();
        String toastStr = "";
        if (mWaypointModel.getWaypointMarkers().size() > 0) {
            if (onMapClickCallBack(mapboxMap.getProjection().toScreenLocation(latLng),
                    mWaypointModel.getWaypointMarkerWindowInfoLayerTags())) {
                return mWaypointModel;
            }
        }
        if (mWaypointModel.getWaypointMarkers().size() > 9) {
            ToastUtils.showLongToast(ResourceUtils.getString(R.string.the_number_of_waypoint_cannot_be_more_than_10));
            return mWaypointModel;
        }
        MapboxLatLng takeOffLatLng = getMarkerLatLngByTag(TAKE_OFF);
        if (takeOffLatLng != null) {
            if (fanceRadius <= 300) {
                toastStr = ResourceUtils.getString(R.string.waypoint_max_limit);
            } else {
                fanceRadius = 300;
                toastStr = ResourceUtils.getString(R.string.waypoint_max_limit_300);
            }
        } else {
            ToastUtils.showLongToast(ResourceUtils.getString(R.string.plane_not_positioned));
            return mWaypointModel;
        }
        double distanceBetweenTakeoffAndTarget = TurfMeasurement.distance(
                Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()),
                Point.fromLngLat(takeOffLatLng.getLongitude(), takeOffLatLng.getLatitude()),
                UNIT_METERS);
        if (distanceBetweenTakeoffAndTarget > fanceRadius) {
            ToastUtils.showLongToast(toastStr);
            return mWaypointModel;
        }
        if (wayPointView == null) {
            wayPointView = ac.getLayoutInflater().inflate(R.layout.waypoint_layout,
                    null);
            AutoUtils.auto(wayPointView);
        }
        if (mWaypointModel.getWaypointMarkers().size() >= 1) {
            //?????????????????????????????????????????????????????????
            List<MapboxLatLng> latLngs = new ArrayList<>();
            //?????????????????????
            MapboxLatLng lastLatlng = mWaypointModel.getWaypointMarkers().get(mWaypointModel.getWaypointMarkers().size() - 1).getLatLng();
            if (mWaypointModel.getTotalMileages() + lastLatlng.distanceTo(latLng) <= 2000) {
                mWaypointModel.setMileages((float) lastLatlng.distanceTo(latLng));
                //????????????marker
                MapboxMarker waypointMarker = addMarkerWithWindowToMap(
                        WAYPOINT_MARRKER + (mWaypointModel.getLastIndex() + 1),
                        MapBoxUtils.generate(wayPointView),
                        ResourceUtils.getString(R.string.waypoint) + ":" + (mWaypointModel.getWaypointMarkers().size() + 1),
                        latLng
                );
                mWaypointModel.addWaypointMarker(waypointMarker);
                //?????????????????????
                MapboxLine line = addLineToMap(WAYPOINT_LINE + (mWaypointModel.getWaypointMarkers().size() + 1),
                        lastLatlng, latLng);
                mWaypointModel.addLines(line);
            } else {
                ToastUtils.showLongToast(ResourceUtils.getString(R.string.total_mileage_shall_not_exceed_2km));
            }

        } else {
            LogUtils.d("????????????===>");
            MapboxMarker waypointMarker = addMarkerWithWindowToMap(
                    WAYPOINT_MARRKER + (mWaypointModel.getLastIndex() + 1),
                    MapBoxUtils.generate(wayPointView),
                    ResourceUtils.getString(R.string.waypoint) + ":" + (mWaypointModel.getWaypointMarkers().size() + 1),
                    latLng
            );
            mWaypointModel.addWaypointMarker(waypointMarker);
        }
        return mWaypointModel;
    }

    public void clearWaypoint() {
        if (mWaypointModel != null) {
            clearWayPointLines();
            if (mWaypointModel.getWaypointMarkers().size() > 0) {
                for (MapboxMarker marker : mWaypointModel.getWaypointMarkers()) {
                    removeMakerByTag(marker.getMarkerTag());
                }
                mWaypointModel.removeAllMarker();
            }
            mWaypointModel.setTotalMileages(0);
            mWaypointModel = null;
        }
    }

    /***????????????*/
    public void clearWayPointLines() {
        if (mWaypointModel.getWaypointPolylines().size() > 0) {
            for (MapboxLine line : mWaypointModel.getWaypointPolylines()) {
                removeLineByTag(line.getLineTag());
            }
            mWaypointModel.removeAllLine();
        }
    }

    private void drawAroundCircle(MapboxMarker aroundMarker, MapboxMarker planeMarker, float aroundR) {
        if (aroundMarker != null && aroundMarker.getLatLng() != null) {
            circleManager.addAroundRingToMap(aroundMarker.getLatLng(), aroundR, 0.1f);
            //?????????????????????????????????????????????
            if (planeMarker != null && planeMarker.getLatLng() != null) {
                drawAroundAndPlaneLine(false, aroundMarker.getLatLng(), planeMarker.getLatLng());
            }
            //??????????????????????????? to dialog
            if (mapChangeListener != null) mapChangeListener.onAroundChange(aroundR);
        }
    }

    private MapboxCircle drawCircle(MapboxLatLng latLng, double radius) {
        return circleManager.addHiddenRangeCircle(latLng, radius);
    }

    /**
     * ????????????????????????????????????
     *
     * @param isDotted true ??????
     */
    public void drawPlaneLine(boolean isDotted, MapboxLatLng planeLatLng) {
        MapboxMarker takeOffMarker = markers.get(TAKE_OFF);
        if (isMarkerAdded(TAKE_OFF) && takeOffMarker != null && takeOffMarker.getLatLng() != null) {
            MapboxLatLng takeOffLatLng = takeOffMarker.getLatLng();
            if (linesManager.isLineAdded(PLANE_LINE)) {
                linesManager.refreshLine(PLANE_LINE, isDotted, takeOffLatLng, planeLatLng);
            } else {
                linesManager.addLineToMap(PLANE_LINE, isDotted, takeOffLatLng, planeLatLng);
            }
        }
    }

    private void drawAroundAndPlaneLine(boolean isDotted, MapboxLatLng aroundLatLng, MapboxLatLng planeLatLng) {
        if (linesManager.isLineAdded(AROUND_LINE)) {
            linesManager.refreshLine(AROUND_LINE, isDotted, aroundLatLng, planeLatLng);
        } else {
            linesManager.addLineToMap(AROUND_LINE, isDotted, aroundLatLng, planeLatLng);
        }
    }

    /**
     * ??????marker???????????????
     */
    private boolean isMarkerAdded(String tag) {
        MapboxMarker marker = markers.get(tag);
        if (mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded()) {
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(tag);
            return marker != null && source != null;
        } else {
            if (marker != null)
                removeMakerByTag(marker.getMarkerTag());
        }
        return false;
//        if (tag == AROUND_MARKER)
//            LogUtils.d("=====>marker??????=" + (marker != null) + "||????????????=" + (source != null));

    }

    public MapboxLatLng getMarkerLatLngByTag(String tag) {
        MapboxMarker marker = markers.get(tag);
        if (mapboxMap.getStyle().isFullyLoaded()) {
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(tag);
            if (marker != null && source != null) {
                return marker.getLatLng();
            }
        }
        return null;
    }


    public boolean onMapClickCallBack(PointF clickPointF, String[] tags) {
        LogUtils.d("??????====>" + Arrays.toString(tags));
        List<Feature> features = mapboxMap.queryRenderedFeatures(clickPointF, tags);
        if (!features.isEmpty()) {//?????????????????????info window???
            Feature feature = features.get(0);
            MapboxLatLng latLng = MapBoxUtils.convertToLatLng(feature);
            PointF symbolScreenPoint = mapboxMap.getProjection().toScreenLocation(latLng);
            boolean isInfoClick = handleClickCallout(feature, clickPointF, symbolScreenPoint);
            LogUtils.d("window info?????????xxx" + isInfoClick);
            return isInfoClick;
        } else {
            return handleClickIcon(clickPointF);
        }
    }

    /**
     * window info????????????
     */
    private boolean handleClickCallout(Feature feature, PointF clickPointF, PointF
            symbolScreenPoint) {
        MapboxMarker windowMarker = null;
        View view = null;
        if (mWaypointModel != null && mWaypointModel.getWaypointMarkers().size() > 0) {
            for (MapboxMarker marker : mWaypointModel.getWaypointMarkers()) {
                if (marker.isWindowInfoShow()) {
                    windowMarker = marker;
                    view = windowMarker.getView(feature);
                }
            }
            if (view != null) {
                View textContainer = view.findViewById(R.id.window_info_container);
                // create hitbox for textView
                Rect hitRectText = new Rect();
                textContainer.getHitRect(hitRectText);
                // move hitbox to location of symbol
                hitRectText.offset((int) symbolScreenPoint.x - 2, (int) symbolScreenPoint.y - 28);
                // offset vertically to match anchor behaviour
                hitRectText.offset(-view.getMeasuredWidth() / 2, -view.getMeasuredHeight());
                //?????????true????????????????????????????????????info window?????????marker
                if (!hitRectText.contains((int) clickPointF.x, (int) clickPointF.y)) {
                    removeSigleWaypointFormUser(windowMarker);
                    return true;
                } else {
                    //???????????????????????????
                    removeSigleWaypointFormUser(windowMarker);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * marker????????????window info??????????????????
     */
    private boolean handleClickIcon(PointF screenPoint) {
        String[] tags = new String[markerTags.size()];
        markerTags.toArray(tags);
        if (tags.length > 0) {
            List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, tags);
            if (!features.isEmpty()) {
                String name = features.get(0).getStringProperty(PROPERTY_NAME);
                for (Map.Entry<String, FeatureCollection> entry : featureCollectionsMap.entrySet()) {
                    FeatureCollection featureColl = entry.getValue();
                    List<Feature> featureList = featureColl.features();
                    for (int i = 0; i < featureList.size(); i++) {
//                        LogUtils.d("yanglin", "window info ??????" + featureList.get(i).getStringProperty(PROPERTY_NAME));
                        String proName = featureList.get(i).getStringProperty(PROPERTY_NAME);
                        if (proName != null && proName.equals(name)) {
                            if (featureSelectStatus(featureColl, i)) {
                                //???????????????????????????
                                setFeatureSelectState(featureColl, entry.getKey(), featureList.get(i), false);
                            } else {
                                //???????????????????????????
                                setFeatureSelectState(featureColl, entry.getKey(), featureList.get(i), true);
                            }
                        } else {
                            setFeatureSelectState(featureColl, entry.getKey(), featureList.get(i), false);
                        }
                    }

                }
                return true;
            }
        }
        return false;
    }


    /**
     * ????????????
     */
    private boolean featureSelectStatus(FeatureCollection featureColl, int index) {
        return featureColl.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }

    private void setFeatureSelectState(FeatureCollection featureColl, String sourceTag, Feature
            feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            refreshSource(featureColl, sourceTag);
        }
    }

    public MapboxWayPointModel removeSigleWaypointFormUser(MapboxMarker marker) {
        mWaypointModel.removeWaypointMarker(marker);
        removeMakerByTag(marker.getMarkerTag());
        clearWayPointLines();
        return notifyWayPointData();
    }

    public MapboxWayPointModel notifyWayPointData() {
        if (mWaypointModel != null && mWaypointModel.getWaypointMarkers().size() > 0) {
            mWaypointModel.setTotalMileages(0);
            for (int i = 0; i < mWaypointModel.getWaypointMarkers().size(); i++) {
                MapboxMarker marker = mWaypointModel.getWaypointMarkers().get(i);
                //????????????????????????
                marker.updateWindowInfoName(ResourceUtils.getString(R.string.waypoint) + ":" + (i + 1));
                if (i > 0) {
                    LogUtils.d("yanglin", "????????????===>" + i);
                    MapboxLatLng lastLatLng = mWaypointModel.getWaypointMarkers().get(i - 1).getLatLng();
                    MapboxLine line = addLineToMap(WAYPOINT_LINE + (i + 1),
                            lastLatLng,
                            marker.getLatLng());
                    mWaypointModel.addLines(line);
                    mWaypointModel.setMileages((float) lastLatLng.distanceTo(marker.getLatLng()));
                }
            }
            return mWaypointModel;
        }
        return null;
    }

    /**
     * ????????????
     */
    private void refreshSource(FeatureCollection featureColl, String sourceTag) {
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs(sourceTag);
        if (source != null && featureColl != null) {
            source.setGeoJson(featureColl);
        }
    }


    /**
     * ????????????????????????info window??????
     */
    public void addLocMarker(boolean isFindPlane, Location location) {
        if (mapboxMap == null || !mapboxMap.getStyle().isFullyLoaded())
            return;
        mLocationMarker = markers.get(LOCATION_MARKER);
        MapboxLatLng locationLatLng = new MapboxLatLng(location.getLatitude(), location.getLongitude());
        MapboxMarker planeMarker = markers.get(PLANE_ICON);
        if (mLocationMarker == null) {
            LogUtils.d("???????????????==>" + location.getLatitude() + "||" + location.getLongitude());
            addMarkerWithWindowToMap(
                    LOCATION_MARKER,
                    R.mipmap.phone_ponit_img,
                    isFindPlane ? MapBoxUtils.distanceBetweenMarkerLatLng(planeMarker, locationLatLng) : null,
                    locationLatLng
            );
            if (isFindPlane && planeMarker != null && planeMarker.getLatLng() != null) {
                drawLineFromLocToPlane(planeMarker.getLatLng(), locationLatLng);
            }
        } else {
            //?????????????????????????????????
            LogUtils.d("?????????????????????==>" + location.getAccuracy());
            mLocationMarker.startMarkerMoveAnimator(locationLatLng,
                    isFindPlane ? MapBoxUtils.distanceBetweenMarkerLatLng(planeMarker, locationLatLng) : null,
                    isFindPlane,
                    (animatedPosition, showInfoWindow) -> {
                        //?????????????????????????????????????????????
                        if (isFindPlane && showInfoWindow) {
                            if (planeMarker != null && planeMarker.getLatLng() != null) {
                                LogUtils.d("?????????====>??????????????????");
                                drawLineFromLocToPlane(planeMarker.getLatLng(), animatedPosition);
                            }
                        } else {
                            if (mapboxMap != null && mapboxMap.getStyle().isFullyLoaded() && mapboxMap.getStyle().getSourceAs(FIND_PLANE_LINE_FOR_PHONE) != null) {
                                LogUtils.d("?????????====>??????????????????");
                                removeLineByTag(FIND_PLANE_LINE_FOR_PHONE);
                            }
                        }
                    });
        }
    }

    public void changeLocaHeader(float angle) {
        if (mLocationMarker != null) {
            if (mLocationMarker != null && mapboxMap != null) {
                mLocHeaderBearing = mLocationMarker.getRotateAngle();
//                LogUtils.d("????????????????????????==>" + angle);
                if (mLocHeaderBearing < 0) {
                    mLocHeaderBearing = angle;
                }
                mLocationMarker.setRotateAngle(angle);
//                float bearing = (mLocHeaderBearing - angle);
//                if (bearing<0){
//
//                }
//                mLocationMarker.setRotateAngle(( float) );
            }
        }
    }

    public void drawLineFromLocToPlane(MapboxLatLng startLatLng, MapboxLatLng endLatLng) {
        //???????????????
        if (linesManager.isLineAdded(FIND_PLANE_LINE_FOR_PHONE)) {
            LogUtils.d("?????????====>??????????????????");
            linesManager.refreshLine(FIND_PLANE_LINE_FOR_PHONE, true, startLatLng, endLatLng);
        } else {
            LogUtils.d("?????????====>??????????????????");
            linesManager.addLineToMap(FIND_PLANE_LINE_FOR_PHONE, true, startLatLng, endLatLng);
        }
    }

    public void enterFindPlaneMode() {
        isFindPlaneMode = false;
        removeLineByTag(FIND_PLANE_LINE_FOR_PHONE);
//        removeLineByTag(FIND_PLANE_LINE_FOR_TAKE_OFF);
        //hintlocation marker's info window
        if (mLocationMarker != null && mLocationMarker.getLatLng() != null) {
//            LogUtils.d("?????????====>???????????????");
            mLocationMarker.moveMarkerWithWindow(mLocationMarker.getLatLng(), null, false);
        }

    }


    public void startFindPlaneMode() {
        this.isFindPlaneMode = true;
        if (mLocationMarker != null) {
            MapboxLatLng locLatLng = mLocationMarker.getLatLng();
            MapboxMarker planeMarker = markers.get(PLANE_ICON);
            if (locLatLng != null && planeMarker != null && planeMarker.getLatLng() != null) {
                mLocationMarker.moveMarkerWithWindow(locLatLng,
                        MapBoxUtils.distanceBetweenMarkerLatLng(planeMarker, locLatLng),
                        true);
                drawLineFromLocToPlane(locLatLng, planeMarker.getLatLng());
            }
        }
    }

    public void changeMapStyle() {
        markers.clear();
        markerLayers.clear();
        circleManager.changeMapStyle();
        linesManager.changeMapStyle();
        featureCollectionsMap.clear();
        markerTags.clear();
        planeNewLatlng = null;
        planeOldLatlng = null;
        mLocationMarker = null;
    }
}
