package com.feipai.flypai.ui.view.Camera;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.R;
import com.feipai.flypai.ui.view.CursorAtLastEditText;
import com.feipai.flypai.ui.view.VirtualKeyboardView;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ViewUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReturnBackLayout extends RelativeLayout {


    @BindView(R.id.message_edittext)
    CursorAtLastEditText messageEdittext;
    @BindView(R.id.return_back_dg_cancel)
    TextView returnBackDgCancel;
    @BindView(R.id.retrun_back_dg_confirm)
    TextView retrunBackDgConfirm;
    @BindView(R.id.return_back_dg_keyboard_view)
    VirtualKeyboardView mKeyboard;
    @BindView(R.id.download_dlg_bottom_ly_canter_view)
    View downloadDlgBottomLyCanterView;
    @BindView(R.id.confirm_action_bottom_ly)
    AutoLinearLayout confirmActionBottomLy;
    @BindView(R.id.return_back_dg_container)
    AutoRelativeLayout mRootLy;

    private int returnAlt;//当前返航高度
    private int altFance;//最大高度围栏

    private final static int MIN_RETURN = 30;
    private final static int MAX_RETURN = 200;

    private ConfirmCallback mCallback;


    public ReturnBackLayout(Context context) {
        super(context);
        initView(context);
    }

    public ReturnBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.return_back_layout, this, true);
        /**如果真是使用注解,这个在Activity注册比较好吧？*/
        AutoUtils.auto(view);
        ButterKnife.bind(this);
    }

    public void setCallback(ConfirmCallback callback) {
        this.mCallback = callback;
    }


    @OnClick({R.id.message_edittext, R.id.return_back_dg_cancel, R.id.retrun_back_dg_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.message_edittext:
                showBoard(messageEdittext, true);
                break;
            case R.id.return_back_dg_cancel:
                this.setVisibility(GONE);
                break;
            case R.id.retrun_back_dg_confirm:
                this.setVisibility(GONE);
                if (mCallback != null) mCallback.onConfirm();
                break;
        }
    }

    public void initKeyboardListener(Activity activity) {
        ViewUtils.banSystemSoft(activity, this, messageEdittext);
        mKeyboard.setKeyboardListener(new VirtualKeyboardView.OnKeyboardListener() {
            @Override
            public void onTextAppend(EditText ed, String text) {
                String edText = ed.getText().toString().trim();
                edText = edText + text;
                ed.setText(edText);
                ed.setSelection(ed.getText().length());
                mKeyboard.getEditQuery().setText(edText);
            }

            @Override
            public void onCloseCallback(EditText ed) {
                ed.setCursorVisible(false);
                if (altFance >= MAX_RETURN) {
                    ViewUtils.edittextLimit(ed, MIN_RETURN, MAX_RETURN);
                } else {
                    ViewUtils.edittextLimit(ed, MIN_RETURN, altFance);
                }
                PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.RTL_ALT,
                        Integer.parseInt(ed.getText().toString().trim()) * 100);
                showBoard(ed, false);
            }

            @Override
            public void onDelectAppend(EditText ed) {
                String text = ed.getText().toString().trim();
                if (text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                    ed.setText(text);
                    ed.setSelection(ed.getText().length());
                    mKeyboard.getEditQuery().setText(text);
                }
            }
        });
    }

    /**
     * 数字键盘显示与隐藏
     */
    public void showBoard(EditText editText, boolean isShow) {
        mKeyboard.setFocusable(isShow);
        mKeyboard.bindEidttext(editText);
        mKeyboard.setFocusableInTouchMode(isShow);
        mKeyboard.bringToFront();
        mKeyboard.getEditQuery().setText("");
        if (isShow) {
            IAnimationUtils.performShowViewAnim(getContext(), mKeyboard, R.anim.enter_bottom_anim);
        } else {
            IAnimationUtils.performHideViewAnim(getContext(), mKeyboard, R.anim.exit_bottom_anim);
        }
    }

    public void setShow(boolean isShow) {
        this.setVisibility(isShow ? VISIBLE : GONE);
        messageEdittext.setText(String.valueOf(returnAlt));

    }


    public void setReturnHeight(int returnAlt, int altFance) {
        this.returnAlt = returnAlt;
        this.altFance = altFance;
    }

    public interface ConfirmCallback {
        void onConfirm();
    }

}
