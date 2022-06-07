package com.feipai.flypai.ui.fragments.planesettingfragments;

import android.view.View;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.RxBusUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SearchPlaneFragment extends BaseMvpFragment<BasePresenter> implements BaseView {
    @BindView(R.id.search_plane_button)
    TextView searchPlaneButton;


    @Override
    protected boolean isUseRxBus() {
        return true;
    }

    @Override
    protected boolean isUseButterKnife() {
        return true;
    }

    @Override
    protected void initInject() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected int initLayout() {
        return R.layout.find_plane_fragment;
    }


    @OnClick(R.id.search_plane_button)
    public void onViewClicked() {
        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.BusEventType.FIND_PLANE, 0));
    }
}
