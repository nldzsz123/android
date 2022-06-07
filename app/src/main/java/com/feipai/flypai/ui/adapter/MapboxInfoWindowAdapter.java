package com.feipai.flypai.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.zhy.autolayout.utils.AutoUtils;

public class MapboxInfoWindowAdapter implements MapboxMap.InfoWindowAdapter , View.OnClickListener{

    private TextView name;
    private ImageView delectIcon;
    String titleName;
    private Marker marker;
    private Context mContext;
    private boolean isDelectIconVisibility = true;
    DelectWaypointListener listener;

    public MapboxInfoWindowAdapter(Context context, DelectWaypointListener delectWaypointListener) {
        this.mContext=context;
        this.listener = delectWaypointListener;
    }



    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        this.marker=marker;
        titleName=marker.getTitle();
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
