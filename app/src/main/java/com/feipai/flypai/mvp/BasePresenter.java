package com.feipai.flypai.mvp;


/**
 * Presenter基类
 *
 * @author yanglin
 */
public interface BasePresenter<V extends BaseView> {

    /**
     * 附加
     */
    void attachView(V view);

    /**
     * 分离
     */
    void detachView();


}