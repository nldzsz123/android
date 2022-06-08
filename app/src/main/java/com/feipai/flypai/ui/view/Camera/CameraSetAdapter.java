package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.IExpandable;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.R;
import com.feipai.flypai.ui.HoriScrollView;
import com.feipai.flypai.ui.view.ScrollPickerView;
import com.feipai.flypai.ui.view.SwitchView;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import static com.feipai.flypai.ui.view.Camera.CameraSetItem.*;

public class CameraSetAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private Context mContext;
    private CameraSetListioner mCameraSetLisioner;
    private boolean isEnabled = true;

    public CameraSetAdapter(Context context, List<MultiItemEntity> data) {
        super(data);
        mContext = context;
        addItemType(CameraSetItemType_TwoButton, R.layout.camera_set_twobutton);
        addItemType(CameraSetItemType_Accessory, R.layout.camera_set_accessory);//展开项
        addItemType(CameraSetItemType_Switch, R.layout.camera_set_switch);
        addItemType(CameraSetItemType_ThreeButton, R.layout.camera_set_threebutton);
        addItemType(CameraSetItemType_MiddleTwoButton, R.layout.camera_set_middle_twobutton);
        addItemType(CameraSetItemType_Scroll, R.layout.camera_set_scrollview);
        addItemType(CameraSetItemType_Title_value, R.layout.camera_set_title_value);
        addItemType(CameraSetItemType_Dimension, R.layout.camera_set_title_value);
        addItemType(CameraSetItemType_Buttons, R.layout.camera_set_buttons);

        initEventListioners();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AutoUtils.auto(parent);
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void convert(BaseViewHolder holder, MultiItemEntity item) {
        switch (item.getItemType()) {
            case CameraSetItemType_TwoButton:
                bindTwoButton(holder, item);
                break;
            case CameraSetItemType_Accessory:
                bindAccessory(holder, item);
                break;
            case CameraSetItemType_Switch:
                bindSwitch(holder, item);
                break;
            case CameraSetItemType_ThreeButton:
                bindThreeButton(holder, item);
                break;
            case CameraSetItemType_MiddleTwoButton:
                bindMiddleTwoButton(holder, item);
                break;
            case CameraSetItemType_Scroll:
                bindScroll(holder, item);
                break;
            case CameraSetItemType_Title_value:
                bindTitleValue(holder, item);
                break;
            case CameraSetItemType_Dimension:
                bindDimensionValue(holder, item);
                break;
            case CameraSetItemType_Buttons:
                bindButtons(holder, item);
                break;

        }
    }

    private void bindAccessory(BaseViewHolder holder, MultiItemEntity item) {
        if (item instanceof FPAccessryBaseCellItem) {
            FPAccessryBaseCellItem accItem = (FPAccessryBaseCellItem) item;
            holder.setText(R.id.accessory_left_tv, accItem.leftTitle)
                    .setImageResource(R.id.accessory_button, accItem.isExpanded() ? R.mipmap.white_composed_img : R.mipmap.white_uncomposed_img);
            holder.setText(R.id.accessory_right_tv, accItem.rightTitle);
            //只有折叠项才有整个item的点击事件，其他的子view事件可以自定义
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (accItem.isExpanded()) {
                        collapse(holder.getAdapterPosition(), true);
                        accItem.expand = false;
                    } else {
                        expand(holder.getAdapterPosition(), true);
                        accItem.expand = true;
                    }
                }
            });
        }
    }

    private void bindTwoButton(BaseViewHolder holder, MultiItemEntity item) {
        if (item instanceof FPTwoButtonCellItem) {
            FPTwoButtonCellItem twoBtnItem = (FPTwoButtonCellItem) item;
            holder.setText(R.id.twobutton_left_tv, twoBtnItem.leftTitle);
            holder.setText(R.id.twobutton_fist_button, twoBtnItem.titles[0]);
            holder.setText(R.id.twobutton_second_button, twoBtnItem.titles[1]);
            holder.setBackgroundRes(R.id.twobutton_fist_button,
                    twoBtnItem.getFirstBgRes());
            holder.setBackgroundRes(R.id.twobutton_second_button,
                    twoBtnItem.getSecondBgRes());
            holder.addOnClickListener(R.id.twobutton_fist_button);
            holder.addOnClickListener(R.id.twobutton_second_button);

//            if (((FPTwoButtonCellItem) item).disable) {
            holder.setAlpha(R.id.twobutton_fist_button, isEnabled ? 1f : 0.5f);
            holder.setAlpha(R.id.twobutton_second_button, isEnabled ? 1f : 0.5f);
//            } else {
//                holder.setAlpha(R.id.twobutton_fist_button, 1.0f);
//                holder.setAlpha(R.id.twobutton_second_button, 1.0f);
//            }
            holder.setEnabled(R.id.twobutton_fist_button, isEnabled);
            holder.setEnabled(R.id.twobutton_second_button, isEnabled);
        }
    }

    private void bindSwitch(BaseViewHolder holder, MultiItemEntity item) {
        FPSwitchCellItem switchItem = (FPSwitchCellItem) item;
        holder.setText(R.id.switch_left_tv, switchItem.leftTitle);
        SwitchView switchView = holder.getView(R.id.camera_set_switch);
        switchView.setOpened(switchItem.switchOn);
        holder.addOnClickListener(R.id.camera_set_switch);

        holder.setAlpha(R.id.camera_set_switch, isEnabled ? 1.0f : 0.5f);
        holder.setEnabled(R.id.camera_set_switch, isEnabled);
    }

    private void bindThreeButton(BaseViewHolder holder, MultiItemEntity item) {
        FPThreeButtonItem cellItem = (FPThreeButtonItem) item;
        holder.setText(R.id.threebutton_left_tv, cellItem.leftTitle);
        holder.setText(R.id.threebutton_fist_button, cellItem.buttontitles[0]);
        holder.setText(R.id.threebutton_second_button, cellItem.buttontitles[1]);
        holder.setText(R.id.threebutton_third_button, cellItem.buttontitles[2]);
        holder.setBackgroundRes(R.id.threebutton_fist_button, cellItem.getFirstBgRes());
        holder.setBackgroundRes(R.id.threebutton_second_button, cellItem.getSecondBgRes());
        holder.setBackgroundRes(R.id.threebutton_third_button, cellItem.getThirdBgRes());
        holder.addOnClickListener(R.id.threebutton_fist_button);
        holder.addOnClickListener(R.id.threebutton_second_button);
        holder.addOnClickListener(R.id.threebutton_third_button);

        holder.setAlpha(R.id.threebutton_fist_button, isEnabled ? 1.0f : 0.5f);
        holder.setAlpha(R.id.threebutton_second_button, isEnabled ? 1.0f : 0.5f);
        holder.setAlpha(R.id.threebutton_third_button, isEnabled ? 1.0f : 0.5f);

        holder.setEnabled(R.id.threebutton_fist_button, isEnabled);
        holder.setEnabled(R.id.threebutton_second_button, isEnabled);
        holder.setEnabled(R.id.threebutton_third_button, isEnabled);
    }

    private void bindMiddleTwoButton(BaseViewHolder holder, MultiItemEntity item) {
        FPMiddleTwoButtonCellItem cellItem = (FPMiddleTwoButtonCellItem) item;
        holder.setText(R.id.middle_twobutton_fist_button, cellItem.titles[0]);
        holder.setText(R.id.middle_twobutton_second_button, cellItem.titles[1]);
        holder.setBackgroundRes(R.id.middle_twobutton_fist_button, cellItem.getFirstBgRes());
        holder.setBackgroundRes(R.id.middle_twobutton_second_button, cellItem.getSecondBgRes());
        holder.addOnClickListener(R.id.middle_twobutton_fist_button);
        holder.addOnClickListener(R.id.middle_twobutton_second_button);

        holder.setAlpha(R.id.middle_twobutton_fist_button, isEnabled ? 1.0f : 0.5f);
        holder.setAlpha(R.id.middle_twobutton_second_button, isEnabled ? 1.0f : 0.5f);
        holder.setEnabled(R.id.middle_twobutton_fist_button, isEnabled);
        holder.setEnabled(R.id.middle_twobutton_second_button, isEnabled);
    }

    private void bindScroll(BaseViewHolder holder, MultiItemEntity item) {
        FPScrollItem contentItem = (FPScrollItem) item;
        holder.setText(R.id.scroll_titleview, contentItem.itemTitle);

        HoriScrollView loopView = holder.getView(R.id.camera_set_scroll_view);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < contentItem.values.length; i++) {
            list.add(contentItem.values[i]);
        }
        loopView.setData(list);
        loopView.setSelectedPosition(contentItem.curIndex);
        loopView.setOnSelectedListener(new ScrollPickerView.OnSelectedListener() {
            @Override
            public void onSelected(ScrollPickerView scrollPickerView, int position, boolean fromUser) {
                if (fromUser) {
                    if (mCameraSetLisioner != null) {
                        mCameraSetLisioner.configFPScrollClick(contentItem, position);
                    }
                }
            }
        });
    }

    private void bindTitleValue(BaseViewHolder holder, MultiItemEntity item) {
        FPTitleValueItem cellItem = (FPTitleValueItem) item;
        holder.setText(R.id.titlevalue_left_tv, cellItem.leftTitle);
        holder.setText(R.id.titlevalue_right_tv, cellItem.rightTitle);
        holder.addOnClickListener(R.id.titlevalue_id_back);
        holder.setBackgroundColor(R.id.dimension_background,
                mContext.getResources().getColor(R.color.color_dimension_normal));
        holder.setVisible(R.id.title_value_indicator, false);
    }

    private void bindDimensionValue(BaseViewHolder holder, MultiItemEntity item) {
        FPDimenssionItem cellItem = (FPDimenssionItem) item;
        holder.setText(R.id.titlevalue_left_tv, cellItem.leftTitle);
        holder.setText(R.id.titlevalue_value_tv, cellItem.valueTitle);
        holder.setBackgroundColor(R.id.dimension_background,
                mContext.getResources().getColor(((FPDimenssionItem) item).checked ? R.color.color_dimension_checked : R.color.color_dimension_normal));
        //只有折叠项才有整个item的点击事件，其他的子view事件可以自定义
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cellItem.isExpanded()) {
                    setEnabled(false);
                    expand(holder.getAdapterPosition());
                    // 展开 回调
                    if (mCameraSetLisioner != null) {
                        mCameraSetLisioner.configFPDimensionClick(cellItem);
                    }
                }
            }
        });
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void updateItem() {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i) instanceof IExpandable) {
                IExpandable it = (IExpandable) mData.get(i);
                if (it.isExpanded()) {
                    collapse(i, true, true);
                }
            }
        }
    }

    private void bindButtons(BaseViewHolder holder, MultiItemEntity item) {
        FPButtonsItem cellItem = (FPButtonsItem) item;
        int left = 4 - cellItem.buttonTitles.length;
        String titles[] = new String[4];
        for (int i = 0; i < cellItem.buttonTitles.length; i++) {
            titles[i] = cellItem.buttonTitles[i];
            int Id = R.id.buttons_first_button;
            if (i == 1) {
                Id = R.id.buttons_second_button;
            } else if (i == 2) {
                Id = R.id.buttons_third_button;
            } else if (i == 3) {
                Id = R.id.buttons_fourth_button;
            }
            holder.addOnClickListener(Id);
        }
        for (int i = 0; i < left; i++) {
            titles[4 - left + i] = "";
        }
        holder.setText(R.id.buttons_first_button, titles[0]);
        holder.setText(R.id.buttons_second_button, titles[1]);
        holder.setText(R.id.buttons_third_button, titles[2]);
        holder.setText(R.id.buttons_fourth_button, titles[3]);
        int ids[] = {R.id.buttons_first_button, R.id.buttons_second_button, R.id.buttons_third_button, R.id.buttons_fourth_button};
        for (int i = 0; i < 4; i++) {
            if (i == cellItem.selectIndex) {
                holder.setTextColor(ids[i], mContext.getResources().getColor(cellItem.selectColor));
            } else {
                holder.setTextColor(ids[i], mContext.getResources().getColor(cellItem.normalColor));
            }
        }

        holder.setAlpha(R.id.buttons_first_button, isEnabled ? 1.0f : 0.5f);
        holder.setAlpha(R.id.buttons_second_button, isEnabled ? 1.0f : 0.5f);
        holder.setAlpha(R.id.buttons_third_button, isEnabled ? 1.0f : 0.5f);
        holder.setAlpha(R.id.buttons_fourth_button, isEnabled ? 1.0f : 0.5f);

        holder.setEnabled(R.id.buttons_first_button, isEnabled);
        holder.setEnabled(R.id.buttons_second_button, isEnabled);
        holder.setEnabled(R.id.buttons_third_button, isEnabled);
        holder.setEnabled(R.id.buttons_fourth_button, isEnabled);

    }

    // 监听子控件的点击事件
    private void initEventListioners() {
        setOnItemChildClickListener((BaseQuickAdapter adapter, View view, int position) -> {
            FPSettingBaseCellItem item = (FPSettingBaseCellItem) getItem(position);
            if (item.getItemType() == CameraSetItem.CameraSetItemType_TwoButton) {
                if (mCameraSetLisioner != null) {
                    mCameraSetLisioner.configFPTwoButtonCellClick((FPTwoButtonCellItem) item, position, view.getId() == R.id.twobutton_fist_button ? 0 : 1);
                }
            } else if (item.getItemType() == CameraSetItem.CameraSetItemType_MiddleTwoButton) {
                if (mCameraSetLisioner != null) {
                    mCameraSetLisioner.configFPMiddleTwoButtonClick((FPMiddleTwoButtonCellItem) item, position, view.getId() == R.id.middle_twobutton_fist_button ? 0 : 1);
                }
            } else if (item.getItemType() == CameraSetItem.CameraSetItemType_ThreeButton) {
                if (mCameraSetLisioner != null) {
                    int clickIndex = 0;
                    if (view.getId() == R.id.threebutton_second_button) {
                        clickIndex = 1;
                    } else if (view.getId() == R.id.threebutton_third_button) {
                        clickIndex = 2;
                    }
                    mCameraSetLisioner.configFPThreeButtonClick((FPThreeButtonItem) item, position, clickIndex);
                }
            } else if (item.getItemType() == CameraSetItem.CameraSetItemType_Switch) {
                if (mCameraSetLisioner != null) {
                    mCameraSetLisioner.configFPSwitchClick((FPSwitchCellItem) item, position, !((FPSwitchCellItem) item).switchOn);
                }
            } else if (item.getItemType() == CameraSetItem.CameraSetItemType_Buttons) {
                if (mCameraSetLisioner != null) {
                    int value = 0;
                    if (view.getId() == R.id.buttons_second_button) {
                        value = 1;
                    } else if (view.getId() == R.id.buttons_third_button) {
                        value = 2;
                    } else if (view.getId() == R.id.buttons_fourth_button) {
                        value = 3;
                    }
                    mCameraSetLisioner.configFPButtonsClick((FPButtonsItem) item, position, value);
                }
            } else if (item.getItemType() == CameraSetItem.CameraSetItemType_Title_value) {
                if (mCameraSetLisioner != null) {
                    mCameraSetLisioner.configFPTitleValueClick((FPTitleValueItem) item, position);
                }
            }
        });
    }

    /**
     * 从指定位置起添加item数据集
     */
    public void addDatasAtPosition(int startPosition, List<MultiItemEntity> items) {
        addData(startPosition, items);
    }

    /**
     * 指定位置添加单个item
     */
    public void addDataAtPosition(int startPosition, MultiItemEntity item) {
        addData(startPosition, item);
    }

    /**
     * 刷新指定位置item
     */
    public void notifyAtPosition(int position) {
        notifyItemChanged(position);
    }

    public interface CameraSetListioner {
        void configFPTwoButtonCellClick(FPTwoButtonCellItem item, int position, int clickIndex);

        void configFPSwitchClick(FPSwitchCellItem item, int position, boolean open);

        void configFPThreeButtonClick(FPThreeButtonItem item, int position, int clickIndex);

        void configFPMiddleTwoButtonClick(FPMiddleTwoButtonCellItem item, int position, int clickIndex);

        void configFPScrollClick(FPScrollItem item, int clickIndex);

        void configFPTitleValueClick(FPTitleValueItem item, int position);

        void configFPDimensionClick(FPDimenssionItem curitem);

        void configFPButtonsClick(FPButtonsItem item, int position, int clickIndex);
    }

    public void setCameraSetLisioner(CameraSetListioner ls) {
        mCameraSetLisioner = ls;
    }
}
