package com.feipai.flypai.ui.view;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.beans.FunctionBean;
import com.feipai.flypai.utils.global.LogUtils;
import com.zaihuishou.expandablerecycleradapter.viewholder.AbstractExpandableAdapterItem;


public class FunctionItemView extends AbstractExpandableAdapterItem {

    private TextView mFunctionName;
    private ImageView mFunctionArrow;
    private ImageView mFunctionImg;
    private ImageView mFunctionHintImg;

    private FunctionBean mFunctionBean;

    @Override
    public int getLayoutResId() {
        return R.layout.function_item_layout;
    }

    @Override
    public void onBindViews(View root) {
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doExpandOrUnexpand(mFunctionBean);
            }
        });
        mFunctionName = root.findViewById(R.id.function_name_tv);
        mFunctionArrow = root.findViewById(R.id.function_arrow_img);
        mFunctionImg = root.findViewById(R.id.function_img);
        mFunctionHintImg = root.findViewById(R.id.function_hint_img);

    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        LogUtils.d("想要展开的--->" + expanded + "||" + mFunctionBean.getItemName());
        if (mFunctionBean != null && mFunctionBean.getChildItemList() != null) {
            float start, target;
            if (expanded) {
                start = 0f;
                target = 90f;
            } else {
                start = 90f;
                target = 0f;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFunctionArrow, View.ROTATION, start, target);
            objectAnimator.setDuration(300);
            objectAnimator.start();
        }
    }

    @Override
    public void onSetViews() {
        mFunctionArrow.setImageResource(0);
        mFunctionArrow.setImageResource(R.mipmap.goto_verification_code_img_sele);
    }

    @Override
    public void onUpdateViews(Object model, int position) {
        super.onUpdateViews(model, position);
        onSetViews();
//        onExpansionToggled(getExpandableListItem().isExpanded());
        mFunctionBean = (FunctionBean) model;
        mFunctionName.setText(mFunctionBean.name);
        if (mFunctionBean.img != -1) {
            mFunctionImg.setImageResource(mFunctionBean.img);
            mFunctionImg.setVisibility(View.VISIBLE);
        } else {
            mFunctionImg.setVisibility(View.GONE);
        }
        mFunctionHintImg.setVisibility(mFunctionBean.isHintImgShow ? View.VISIBLE : View.GONE);
    }
}

