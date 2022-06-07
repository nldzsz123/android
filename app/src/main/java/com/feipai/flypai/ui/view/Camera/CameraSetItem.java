package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.beans.HeaderFileBean;
import com.feipai.flypai.ui.view.FPButton;
import com.feipai.flypai.ui.view.FPImageButton;

import java.util.ArrayList;
import java.util.List;

public abstract class CameraSetItem {
    // 左边标题 右边文字(可隐藏) 然后带>箭头Item
    public final static int CameraSetItemType_Accessory = 1;
    // 两个button居中的cell
    public final static int CameraSetItemType_MiddleTwoButton = 2;
    // 左边标题 两个button的cell
    public final static int CameraSetItemType_TwoButton = 3;
    // 左边标题 带三个button的cell 按钮有文字
    public final static int CameraSetItemType_ThreeButton = 4;
    // 左边标题 带有一个Switch的cell
    public final static int CameraSetItemType_Switch = 5;
    // 视频分辨率
    public final static int CameraSetItemType_Dimension = 6;
    // 作为命令的子选项 最多三个子选项
    public final static int CameraSetItemType_Buttons = 7;
    // 左边标题的，右边值
    public final static int CameraSetItemType_Title_value = 8;
    // 中间是文字的带可滑动的
    public final static int CameraSetItemType_Scroll = 9;

    public static class FPSettingBaseCellItem<T> extends HeaderFileBean<T> {
        public int keyId;
        public int cellType;
        public boolean canSelect;

        public String[] commands;
        public String curCommand;
        public int level = 0;

        public FPSettingBaseCellItem(int cellType) {
            this.cellType = cellType;
        }

        @Override
        public int getItemType() {
            return cellType;
        }

        @Override
        public void setSubItems(List<T> list) {
            super.setSubItems(list);
        }

        @Override
        public List<T> getSubItems() {
            return super.getSubItems();
        }

        public void setLevel(int level) {
            this.level = level;
        }

        @Override
        public int getLevel() {
            return level;
        }
    }

    // 左边标题 右边文字(可隐藏) 然后带>箭头的cell 的基类
    public static class FPAccessryBaseCellItem<T> extends FPSettingBaseCellItem<T> {
        public String leftTitle;
        public String rightTitle;
        public boolean expand;      //是否展开

        public FPAccessryBaseCellItem() {
            super(CameraSetItemType_Accessory);
        }
    }

    // 左边标题，中间两个按钮
    static public class FPMiddleTwoButtonCellItem extends FPSettingBaseCellItem {
        public String[] titles;

        public @DrawableRes
        int normalMipmap;
        public @DrawableRes
        int selectMipmap;
        public int selectButtonIndex;

        public FPMiddleTwoButtonCellItem() {
            super(CameraSetItemType_MiddleTwoButton);
        }

        /**
         * 根据选中下标获取first button的北京
         */
        public int getFirstBgRes() {
            return selectButtonIndex == 0 ? selectMipmap : normalMipmap;
        }

        /**
         * 根据选中下标获取second button的北京
         */
        public int getSecondBgRes() {
            return selectButtonIndex == 1 ? selectMipmap : normalMipmap;
        }
    }

    // 左边标题 两个button的cell
    static public class FPTwoButtonCellItem extends FPSettingBaseCellItem implements MultiItemEntity {

        public String leftTitle;
        public String[] titles;

        public @DrawableRes
        int normalMipmap;
        public @DrawableRes
        int selectMipmap;
        public int selectButtonIndex;

        public FPTwoButtonCellItem() {
            super(CameraSetItemType_TwoButton);
        }

        /**
         * 根据选中下标获取first button的北京
         */
        public int getFirstBgRes() {
            return selectButtonIndex == 0 ? selectMipmap : normalMipmap;
        }

        /**
         * 根据选中下标获取second button的北京
         */
        public int getSecondBgRes() {
            return selectButtonIndex == 1 ? selectMipmap : normalMipmap;
        }

    }

    // 左边标题 带有一个Switch的cell
    static public class FPSwitchCellItem extends FPSettingBaseCellItem implements MultiItemEntity {
        public String leftTitle;
        public boolean switchOn;

        public FPSwitchCellItem() {
            super(CameraSetItemType_Switch);
        }
    }

    // 左边标题 右边三个按钮
    static public class FPThreeButtonItem extends FPSettingBaseCellItem implements MultiItemEntity {
        public String leftTitle;
        public String[] buttontitles;   // 按钮的文字

        public @DrawableRes
        int normalMipmaps[];
        public @DrawableRes
        int selectMipmaps[];

        public int selectButtonIndex;

        public FPThreeButtonItem() {
            super(CameraSetItemType_ThreeButton);
        }

        /**
         * 根据选中下标获取first button的北京
         */
        public int getFirstBgRes() {
            if (selectButtonIndex == 0) {
                return selectMipmaps[0];
            } else {
                return normalMipmaps[0];
            }
        }

        /**
         * 根据选中下标获取second button的北京
         */
        public int getSecondBgRes() {
            if (selectButtonIndex == 1) {
                return selectMipmaps[1];
            } else {
                return normalMipmaps[1];
            }
        }


        public int getThirdBgRes() {
            if (selectButtonIndex == 2) {
                return selectMipmaps[2];
            } else {
                return normalMipmaps[2];
            }
        }
    }

    // 中间标题 带有一个Scroll的cell
    public static class FPScrollItem extends FPSettingBaseCellItem {
        public String itemTitle;
        public String[] values;
        public int curIndex;
        public int visibleSize;

        public FPScrollItem() {
            super(CameraSetItemType_Scroll);
        }
    }

    // 左边标题 右边文字的cell
    static public class FPTitleValueItem extends FPSettingBaseCellItem implements MultiItemEntity {
        public String leftTitle;
        public String rightTitle;

        public FPTitleValueItem() {
            super(CameraSetItemType_Title_value);
        }
    }

    // 展开都是按钮的
    public static class FPDimenssionItem extends FPSettingBaseCellItem<FPButtonsItem> {
        public String leftTitle;
        public String valueTitle;
        public String dimension;
        public String currentfps;
        public String defaultfps;
        public boolean checked;


        public FPDimenssionItem() {
            super(CameraSetItemType_Dimension);
        }

        @Override
        public int getLevel() {
            return 1;
        }
    }

    // 全部是按钮的
    static public class FPButtonsItem extends FPSettingBaseCellItem implements MultiItemEntity {
        public String buttonTitles[];
        public @ColorRes
        int normalColor;
        public @ColorRes
        int selectColor;
        public int selectIndex;

        public FPButtonsItem() {
            super(CameraSetItemType_Buttons);
        }

        @Override
        public int getLevel() {
            return 2;
        }
    }
}
