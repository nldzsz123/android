package com.feipai.flypai.utils.global;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.languageutils.LanguageUtil;

/**
 * Created by YangLin on 2017-09-07.
 */

public class MaxLengthWatcher implements TextWatcher {

    private int maxLen = 0;
    private EditText editText = null;


    public MaxLengthWatcher(EditText editText, int maxLen) {
        this.maxLen = maxLen;
        this.editText = editText;
    }

    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        // TODO Auto-generated method stub

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        if (s.length() == 0 || count == 0) {
            return;
        }
        int maxL = maxLen;
        if (s.toString().contains(" ") && !LanguageUtil.isEnglish()) {
            String[] str = s.toString().split(" ");
            String str1 = "";
            for (int i = 0; i < str.length; i++) {
                str1 += str[i];
            }
            editText.setText(str1);
            // TODO: 2019/10/11 此处出现有越界问题 
            if (editText.getText() != null && editText.getText().toString().length() <= start) {
                editText.setSelection(start);
            }
        }

        Editable editable = editText.getText();
        int len = editable.length();
        if (maxL < 20) {
            maxL = calculateLength(editable.toString(), maxL);
        }

        if (len > maxL) {
            int selEndIndex = Selection.getSelectionEnd(editable);
            String str = editable.toString();
            //截取新字符串
            String newStr = str.substring(0, maxL);
            editText.setText(newStr);
            editable = editText.getText();

            //新字符串的长度
            int newLen = editable.length();
            //旧光标位置超过字符串长度
            if (selEndIndex > newLen) {
                selEndIndex = editable.length() - 1;
            }
            //设置新光标所在的位置 // TODO: 2019/10/11 此处出现有越界问题
            if (editText.getText() != null && editText.getText().toString().length() <= selEndIndex) {
                editText.setSelection(selEndIndex);
            }
//            UIUtils.post(new Runnable() {
//                @Override
//                public void run() {
//                    ToastUtils.showShort(context, context.getResources().getString(R.string.word_count_reached_upper_limit));
//                }
//            });
        }

    }

    public void setEditTextInputSpace(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ") || source.toString().contentEquals("\n")) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }


    /**
     * 英文一个字符  中文两个字符
     *
     * @return 返回滿足限定长度需要截取的长度
     */
    private int calculateLength(String string, int maxLen) {
        char[] ch = string.toCharArray();

        int varlength = 0;
        int len = 0;
        for (char c : ch) {
            // changed by zyf 0825 , bug 6918，加入中文标点范围 ， TODO 标点范围有待具体化
            if ((c >= 0x2E80 && c <= 0xFE4F) || (c >= 0xA13F && c <= 0xAA40) || c >= 0x80) { // 中文字符范围0x4e00 0x9fbb
//                    if (c >= 0x4E00 && c <= 0x9FBB) { // 中文字符范围 0x4E00-0x9FA5 + 0x9FA6-0x9FBB
                varlength = varlength + 2;
            } else {
                varlength++;
            }
            len++;
            if (varlength >= maxLen) {
                break;
            }
            MLog.log("length : " + varlength + " l: " + string.length());
        }
        return len;
    }


}