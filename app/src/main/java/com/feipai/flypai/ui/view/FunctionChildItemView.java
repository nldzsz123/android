package com.feipai.flypai.ui.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.beans.FunctionChildBean;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zaihuishou.expandablerecycleradapter.viewholder.AbstractExpandableAdapterItem;

public class FunctionChildItemView extends AbstractExpandableAdapterItem {

    private TextView mName;
    private RelativeLayout childLy;
    private TextView mUpdateTv;
    private FunctionChildBean childBean;

    @Override
    public int getLayoutResId() {
        return R.layout.function_child_item_layout;
    }

    @Override
    public void onBindViews(View root) {
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyClick(childBean);
            }
        });
        mName = root.findViewById(R.id.tv_name);
        childLy = root.findViewById(R.id.child_item);
        mUpdateTv = root.findViewById(R.id.function_upgrade_img);
    }


    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(Object model, int position) {
        super.onUpdateViews(model, position);
        if (model instanceof FunctionChildBean) {
            childBean = (FunctionChildBean) model;
            mName.setText(childBean.name);
            if (position != 1) {
                childLy.setBackgroundResource(R.drawable.top_line_onedp_bg);
            }
            if (childBean.version != null && !childBean.version.equals("1")) {
                if (childBean.name.equals(ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM)[0])) {
                    mUpdateTv.setText(childBean.isNeedUpgrade ? FlyPieApplication.getInstance().
                            getResources().getString(R.string.found_update)
                            : childBean.version);
                } else if (childBean.name.equals(ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM)[2])) {
                    if (childBean.isNeedUpgrade) {
                        mUpdateTv.setText(FlyPieApplication.getInstance().
                                getResources().getString(R.string.can_installed_fw));
                    } else {
                        if (childBean.version.indexOf("-V") != -1) {
                            String ver = childBean.version.substring(childBean.version.indexOf("-V")).replace("-", "");
                            mUpdateTv.setText(ver);
                        }else {
                            mUpdateTv.setText(childBean.version);
                        }
                    }
                } else {
                    mUpdateTv.setText(childBean.isNeedUpgrade ? FlyPieApplication.getInstance().
                            getResources().getString(R.string.can_installed_fw)
                            : childBean.version);
                }
                mUpdateTv.setTextColor(FlyPieApplication.getInstance().
                        getResources().getColor(childBean.isNeedUpgrade ? R.color.color_f34235 : R.color.color_b8b8b8));
                mUpdateTv.setVisibility(View.VISIBLE);
            } else {
                mUpdateTv.setVisibility(View.GONE);
            }


        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {

    }


}
