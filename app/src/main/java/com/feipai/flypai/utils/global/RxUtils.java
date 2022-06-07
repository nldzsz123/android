package com.feipai.flypai.utils.global;

import com.feipai.flypai.base.BaseSimpleActivity;
import com.feipai.flypai.base.BaseSimpleFragment;
import com.feipai.flypai.mvp.BaseView;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;

import io.reactivex.subjects.BehaviorSubject;

public class RxUtils {


    /**
     * RxLifecycle管理
     */
    public static <T> LifecycleTransformer<T> bindUntilEventActivity(BaseView view, ActivityEvent event) {
        if (view instanceof BaseSimpleActivity) {
            return ((BaseSimpleActivity) view).<T>bindUntilEvent(event);
        } else if (view instanceof BaseSimpleFragment) {
            return ((BaseSimpleActivity) view.getPageActivity()).<T>bindUntilEvent(event);
        } else {
            if (Utils.isDebug) {
                ToastUtils.showLongToast("view isn't activity");
            }
            return RxLifecycle.bindUntilEvent(BehaviorSubject.create(), event);
        }
    }

}
