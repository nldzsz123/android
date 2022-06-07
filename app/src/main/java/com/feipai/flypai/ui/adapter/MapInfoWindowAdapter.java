package com.feipai.flypai.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.feipai.flypai.R;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;

public class MapInfoWindowAdapter implements AMap.InfoWindowAdapter, View.OnClickListener {
    private LatLng latLng;
    private TextView name;
    private ImageView delectIcon;
    String titleName;
    ArrayList<BitmapDescriptor> icon;
    DelectWaypointListener listener;
    private Marker marker;
    private boolean isDelectIconVisibility = true;
    private Context mContext;

    public MapInfoWindowAdapter(Context context, DelectWaypointListener delectWaypointListener) {
        this.mContext=context;
        this.listener = delectWaypointListener;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        if (marker.getTitle() == null || marker.getTitle().equals("")) {
            return null;
        }
        initData(marker);
        View view = initView();
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (marker.getTitle() == null || marker.getTitle().equals("")) {
            return null;
        }
        initData(marker);
        View view = initView();
        return view;
    }

    private void initData(Marker marker) {
        this.marker = marker;
        latLng = marker.getPosition();
        titleName = marker.getTitle();
        isDelectIconVisibility = marker.isFlat();
    }

    @NonNull
    private View initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.map_info_window_layout, null);
        name = (TextView) view.findViewById(R.id.inforwindow_text);
        delectIcon = (ImageView) view.findViewById(R.id.inforwindow_btn);
        delectIcon.setVisibility(isDelectIconVisibility ? View.VISIBLE : View.GONE);
        if (titleName.contains("距离")) {
            name.setTextSize(30);
        } else {
            name.setTextSize(50);
        }
        name.setText(titleName);
        delectIcon.setOnClickListener(this);
        AutoUtils.auto(view);
        return view;
    }


    public void setDelectIconVisibility(boolean isDelectIconVisibility, Marker marker) {
        this.isDelectIconVisibility = isDelectIconVisibility;
        getInfoWindow(marker);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.inforwindow_btn:  //删除航点
                if (listener != null)
                    listener.delectWaypoinit(marker);
//                NavigationUtils.Navigation(latLng);
                break;

        }
    }

    public interface DelectWaypointListener {
        void delectWaypoinit(Marker marker);
    }
}
