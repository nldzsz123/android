package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.feipai.flypai.R;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.VerticalSeekBar;
import com.feipai.flypai.utils.global.LogUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Function;

public class PlaneControlLayout extends AutoRelativeLayout {

    private BaseView mbv;


    public PlaneControlLayout(Context context) {
        super(context);
        initView(context);
    }

    public PlaneControlLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.camera_adjust_layout, this, true);
        AutoUtils.auto(view);
        ButterKnife.bind(this, view);
    }


}
