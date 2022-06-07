
package com.feipai.flypai.ui.view.Camera;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.feipai.flypai.R;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.beans.HintItemBean;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.VerticalSeekBar;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Function;

import static com.feipai.flypai.app.ConstantFields.HINT_LAYOUT_TYPE.LOW_BATTERY;
import static com.feipai.flypai.app.ConstantFields.HINT_LAYOUT_TYPE.REMAINING_FLIGHT_TIME;

public class HintLayout extends LinearLayout {
    private Context mContext;
    private List<HintItemBean> list = new ArrayList<>();
    private boolean isRight;
    private LayoutTransition mTransitioner;

    public HintLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public HintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mTransitioner = new LayoutTransition();
//        setLayoutTransition(mTransitioner);
//        setTransition();
    }

    /**
     * view出现时 view自身的动画效果
     */
    private void setTransition() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(null, "translationX",-getLeft(), getWidth(), 0).
                setDuration(mTransitioner.getDuration(LayoutTransition.APPEARING));
        mTransitioner.setAnimator(LayoutTransition.APPEARING, animator1);
    }


    public void addItem(HintItemBean item) {
//        item.setTextColor(R.color.color_ffffff);
        if (list.contains(item)) {
            int index = list.indexOf(item);
            if (!StringUtils.equals(list.get(index).getHintText(), item.getHintText())) {
                /**不是相同文案的时候，刷新数据*/
                list.set(index, item);
                HintItemLayout layout = (HintItemLayout) getChildAt(index);
                layout.bindViewStatus(item);
                invalidate();
            }
        } else {
            HintItemLayout itemLayout = new HintItemLayout(mContext);
            itemLayout.bindViewStatus(item);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.topMargin = AutoUtils.getPercentHeightSize(16);
            lp.gravity = !isRight ? Gravity.LEFT : Gravity.RIGHT;
            itemLayout.setLayoutParams(lp);
            if (item.getType() == LOW_BATTERY)
                itemLayout.startFlickerAnimation();
            addView(itemLayout);
            list.add(item);
            changeToRight(false);
        }
    }

    public void removeItem(int itemType) {
        if (list.size() > 0) {
            for (HintItemBean bean : list) {
                if (bean.getType() == itemType) {
                    int index = list.indexOf(bean);
                    removeView(getChildAt(index));
                    list.remove(bean);
                    break;
                }
            }
        }
    }

    public void changeToRight(boolean isRight) {
        this.isRight = isRight;
        if (list.size() > 0) {
            for (HintItemBean bean : list) {
                int index = list.indexOf(bean);
                if (getChildAt(index) instanceof HintItemLayout) {
                    HintItemLayout itemLayout = (HintItemLayout) getChildAt(index);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    lp.topMargin = AutoUtils.getPercentHeightSize(16);
                    lp.gravity = !isRight ? Gravity.LEFT : Gravity.RIGHT;
                    itemLayout.setLayoutParams(lp);
                    itemLayout.changeToRight(isRight);
                }
            }
        }
    }
}
