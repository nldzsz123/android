package com.dou361.ijkplayer.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.SeekBar;

import com.dou361.ijkplayer.R;


/**
 * Created by xiongli on 2016-07-14.
 */
public class MPopWindow extends PopupWindow {
    /**
     * 布局
     */
    private View mMenuView;

    /**
     * ===voice中的seekbar=======
     */
    public static VerticalSeekBar voiceSeekb;


    /**
     * ====muice中的item项==================
     */
    public static RadioButton muiceClassic;
    public static RadioButton muiceLight;
    public static RadioButton muiceScenery;
    public static RadioButton muiceShock;
    public static RadioButton muiceRelieve;
    public static RadioButton muiceOriginal;


    public MPopWindow(Activity context, View.OnClickListener itemOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mMenuView = inflater.inflate(R.layout.muice_layout, null);
                muiceClassic = (RadioButton) mMenuView.findViewById(R.id.music_classic);
                muiceClassic.setOnClickListener(itemOnClick);
                muiceLight = (RadioButton) mMenuView.findViewById(R.id.music_light);
                muiceLight.setOnClickListener(itemOnClick);
                muiceScenery = (RadioButton) mMenuView.findViewById(R.id.music_scenery);
                muiceScenery.setOnClickListener(itemOnClick);
                muiceShock = (RadioButton) mMenuView.findViewById(R.id.music_shock);
                muiceShock.setOnClickListener(itemOnClick);
                muiceRelieve = (RadioButton) mMenuView.findViewById(R.id.music_relieve);
                muiceRelieve.setOnClickListener(itemOnClick);
                muiceOriginal = (RadioButton) mMenuView.findViewById(R.id.music_original);
                muiceOriginal.setOnClickListener(itemOnClick);

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.mypopwindow_anim_style);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        this.setOutsideTouchable(false);
    }
}
