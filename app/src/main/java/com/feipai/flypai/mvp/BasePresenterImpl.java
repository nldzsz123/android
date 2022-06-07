package com.feipai.flypai.mvp;

/**
 * Presenter基类实现
 *
 * @author yanglin
 */
public class BasePresenterImpl<V extends BaseView> implements BasePresenter<V> {

    protected V mView;

    /**
     * 附加
     */
    @Override
    public void attachView(V view) {
        this.mView = view;
    }

    /**
     * 分离
     */
    @Override
    public void detachView() {
        this.mView = null;
    }

}
