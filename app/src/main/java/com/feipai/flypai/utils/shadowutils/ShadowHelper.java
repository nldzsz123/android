package com.feipai.flypai.utils.shadowutils;

import android.support.v4.view.ViewCompat;
import android.view.View;


/**
 * 设置阴影入口工具方法
 */

public class ShadowHelper {

    public static void setShadowBgForView(View view, ShadowConfig.Builder config) {
        //关闭硬件加速
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ViewCompat.setBackground(view, config.builder());
    }
}
