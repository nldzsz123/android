package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.beans.HintItemBean;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.global.ViewUtils;
import com.zhy.autolayout.AutoLinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HintItemLayout extends LinearLayout {


    @BindView(R.id.left_item_img)
    ImageView leftItemImg;
    @BindView(R.id.hint_item_tv)
    TextView hintItemTv;
    @BindView(R.id.hint_item_layout)
    AutoLinearLayout hintItemLayout;
    @BindView(R.id.right_item_img)
    ImageView rightItemImg;
    private boolean isRight = true;
    private boolean isEnabled = true;

    public HintItemLayout(Context context) {
        super(context);
        initView(context);
    }

    public HintItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.hint_layout, this, true);
        ButterKnife.bind(this);
    }


    public void bindViewStatus(HintItemBean item) {
        hintItemTv.setText(item.getHintText());
        hintItemTv.setTextColor(ResourceUtils.getColor(item.getTextColor()));

    }

    @OnClick({R.id.hint_item_layout, R.id.hint_item_tv, R.id.left_item_img, R.id.right_item_img})
    public void onViewClicked() {
        if (isEnabled) {
            if (hintItemTv.getVisibility() == VISIBLE) {
                IAnimationUtils.performHideViewAnim(Utils.context, hintItemTv, !isRight ? R.anim.exit_left_anim : R.anim.exit_right_anim, false);
            } else {
                IAnimationUtils.performShowViewAnim(Utils.context, hintItemTv, !isRight ? R.anim.enter_left_anim : R.anim.enter_right_anim);
            }
        }
    }

    public void changeToRight(boolean isRight) {
        this.isRight = isRight;
        leftItemImg.setVisibility(!isRight ? VISIBLE : GONE);
        rightItemImg.setVisibility(isRight ? VISIBLE : GONE);
    }

    public void startFlickerAnimation() {
        setEnabled(false);
        ViewUtils.startFlickerAnimation(hintItemTv, 1000);
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }


}
