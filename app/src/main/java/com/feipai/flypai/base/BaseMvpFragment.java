package com.feipai.flypai.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.di.module.FragmentModule;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;

import javax.inject.Inject;

/**
 * MVP模式下的Fragment的抽取
 *
 * @author yanglin
 */
public abstract class BaseMvpFragment<P extends BasePresenter> extends BaseSimpleFragment implements BaseView, View.OnClickListener {

    @Inject
    protected P mPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initInject();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    @Override
    public Activity getPageActivity() {
        return getActivity();
    }

    /**
     * 创建FragmentModule
     */
    protected FragmentModule getFragmentModule() {
        return new FragmentModule(this);
    }

    /**
     * 初始化注入
     */
    protected abstract void initInject();

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        hideSoftInput();
    }

}
