package com.feipai.flypai.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.feipai.flypai.R;

import java.util.List;

public class BottomDialog extends Dialog implements OnClickListener {
    private Activity mActivity;
    private Button mBtnCancel;
    private Button mBtnTakePhoto;
    private Button mBtnPhotoAlbum;
    private List<String> mName;
    private boolean mUseCustomColor = false;
    private int mFirstItemColor;
    private int mOtherItemColor;


    private SelectDialogListener mListener;

    public interface SelectDialogListener {
        public void onClickTakePhoto();

        public void onClickPhotoAlbum();
    }

    public BottomDialog(Activity activity, int theme,
                        List<String> names) {
        super(activity, theme);
        mActivity = activity;
        this.mName = names;

        // 设置是否点击外围解散
        setCanceledOnTouchOutside(true);
    }

    /**
     * @param activity 调用弹出菜单的activity
     * @param listener 菜单项单击事件
     * @param names    菜单项名称
     */
    public BottomDialog(Activity activity, SelectDialogListener listener, List<String> names) {
        super(activity);
        mActivity = activity;
        mListener = listener;
        this.mName = names;

        // 设置是否点击外围不解散
        setCanceledOnTouchOutside(false);
    }

    /**
     * @param activity 调用弹出菜单的activity
     * @param theme    主题
     * @param names    菜单项名称
     * @param title    菜单标题文字
     */
    public BottomDialog(Activity activity, int theme, List<String> names, String title) {
        super(activity, theme);
        mActivity = activity;
        this.mName = names;

        // 设置是否点击外围可解散
        setCanceledOnTouchOutside(true);
    }

    public BottomDialog(Activity activity, int theme, SelectDialogListener listener, List<String> names, String title) {
        super(activity, theme);
        mActivity = activity;
        mListener = listener;
        this.mName = names;

        // 设置是否点击外围可解散
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.bottom_dialog_layout,
                null);
        setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        Window window = getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.dialog_from_bottom_anim);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = LayoutParams.MATCH_PARENT;
        wl.height = LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        onWindowAttributesChanged(wl);

        //setCanceledOnTouchOutside(false);
        initViews();
    }

    private void initViews() {
        mBtnCancel = findViewById(R.id.bottom_dialog_cancel);
        mBtnCancel.setOnClickListener(this);
        mBtnTakePhoto = findViewById(R.id.bottom_dialog_take_photo);
        mBtnTakePhoto.setOnClickListener(this);
        mBtnPhotoAlbum = findViewById(R.id.bottom_dialog_photo_album);
        mBtnPhotoAlbum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.bottom_dialog_take_photo:
                if (mListener != null) {
                    mListener.onClickTakePhoto();
                }
                break;
            case R.id.bottom_dialog_photo_album:
                if (mListener != null) {
                    mListener.onClickPhotoAlbum();
                }
                break;
        }

    }


    /**
     * 设置列表项的文本颜色
     */
    public void setItemColor(int firstItemColor, int otherItemColor) {
        mFirstItemColor = firstItemColor;
        mOtherItemColor = otherItemColor;
        mUseCustomColor = true;
    }

}
