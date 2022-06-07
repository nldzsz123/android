package com.feipai.flypai.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by YangLin on 2017-09-04.
 */

public class CursorAtLastEditText extends android.support.v7.widget.AppCompatEditText {
    public CursorAtLastEditText(Context context) {
        super(context);
    }

    public CursorAtLastEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //保证光标始终在最后面
        if (selStart == selEnd) {//防止不能多选
            setSelection(getText().length());
        }

    }
}
