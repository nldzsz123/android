package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.ui.view.AutoScrollView;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraSetView extends AutoRelativeLayout {
    private Context mContext;

    @BindView(R.id.camera_set_head_textview)
    TextView headTextView;

    @BindView(R.id.camera_set_effects)
    ImageButton effectsButton;

    @BindView(R.id.camera_set_options)
    ImageButton optionsButton;

    @BindView(R.id.camera_set_genera)
    ImageButton generaButton;

    @BindView(R.id.camera_set_recycleview)
    public RecyclerView setView;

    private int selectIndex;

    public CameraSetView(Context context) {
        super(context);
        initView(context);
    }

    public CameraSetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.camera_set, this, true);
        AutoUtils.auto(view);
        ButterKnife.bind(this);
        setView.setLayoutManager(new LinearLayoutManager(mContext));
        setView.setHasFixedSize(true);

        // 默认选择中间的选项
        selectIndex = 1;
    }

    @OnClick(R.id.camera_set_effects)
    void onClickeffectsButton() {
        effectsButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_top_right_sel));
        optionsButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_normal));
        generaButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_bot_right));
        if (selectIndex == 0) {
            return;
        }
        selectIndex = 0;
        if (mCameraSetViewOnClick != null) {
            mCameraSetViewOnClick.onClick(0);
        }
    }

    @OnClick(R.id.camera_set_options)
    void onClickoptionsButton() {
        effectsButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_top_right));
        optionsButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_sel));
        generaButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_bot_right));
        if (selectIndex == 1) {
            return;
        }
        selectIndex = 1;
        if (mCameraSetViewOnClick != null) {
            mCameraSetViewOnClick.onClick(1);
        }
    }

    @OnClick(R.id.camera_set_genera)
    void onClickgeneraButton() {
        effectsButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_top_right));
        optionsButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_normal));
        generaButton.setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_bot_right_sel));
        if (selectIndex == 2) {
            return;
        }
        selectIndex = 2;
        if (mCameraSetViewOnClick != null) {
            mCameraSetViewOnClick.onClick(2);
        }
    }

    public void setCameraSetViewOnClick(CameraSetViewOnClick mCameraSetViewOnClick) {
        this.mCameraSetViewOnClick = mCameraSetViewOnClick;
    }

    public interface CameraSetViewOnClick {
        void onClick(int index);
    }

    private CameraSetViewOnClick mCameraSetViewOnClick;


    public int getSelectIndex() {
        return selectIndex;
    }

    public void setHeadTextViewText(String string) {
        headTextView.setText(string);
    }
}
