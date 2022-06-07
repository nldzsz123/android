package com.feipai.flypai.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.ui.adapter.KeyBoardAdapter;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YangLin on 2017-08-02.
 * 虚拟数字键盘
 */
public class VirtualKeyboardView extends AutoRelativeLayout {

    Context context;

    //因为就6个输入框不会变了，用数组内存申请固定空间，比List省空间（自己认为）
    private GridView gridView;    //用GrideView布局键盘，其实并不是真正的键盘，只是模拟键盘的功能

    private ArrayList<Map<String, String>> valueList;    //有人可能有疑问，为何这里不用数组了？
    //因为要用Adapter中适配，用数组不能往adapter中填充

    private RelativeLayout layoutExit;

    private TextView editQuery;

    private EditText mEd;

    private OnKeyboardListener mListener;

    public VirtualKeyboardView(Context context) {
        this(context, null);
    }

    public VirtualKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        View view = View.inflate(context, R.layout.virtual_keyboard_layout, null);

        valueList = new ArrayList<>();

        layoutExit = (RelativeLayout) view.findViewById(R.id.layoutBack);
        layoutExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onCloseCallback(mEd);
            }
        });

        editQuery = (TextView) view.findViewById(R.id.edit_query);

        gridView = (GridView) view.findViewById(R.id.gv_keybord);

        initValueList();

        setupView();
        AutoUtils.auto(view);
        addView(view);      //必须要，不然不显示控件
    }

    public RelativeLayout getLayoutExit() {
        return layoutExit;
    }

    public TextView getEditQuery() {
        return editQuery;
    }

    public ArrayList<Map<String, String>> getValueList() {
        return valueList;
    }

    private void initValueList() {

        // 初始化按钮上应该显示的数字
        for (int i = 1; i < 13; i++) {
            Map<String, String> map = new HashMap<>();
            if (i < 10) {
                map.put("name", String.valueOf(i));
            } else if (i == 10) {
                map.put("name", ResourceUtils.getString(R.string.complete));
            } else if (i == 11) {
                map.put("name", String.valueOf(0));
            } else if (i == 12) {
                map.put("name", "");
            }
            valueList.add(map);
        }
    }

    public GridView getGridView() {
        return gridView;
    }

    public void bindEidttext(EditText ed) {
        this.mEd = ed;
    }

    private void setupView() {
        KeyBoardAdapter keyBoardAdapter = new KeyBoardAdapter(context, valueList);
        gridView.setAdapter(keyBoardAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = keyBoardAdapter.getItem(position).get("name");
                if (text.equals(ResourceUtils.getString(R.string.complete))) {
                    if (mListener != null) mListener.onCloseCallback(mEd);
                } else if (text.equals("")) {
                    if (mListener != null) mListener.onDelectAppend(mEd);
                } else {
                    if (mListener != null) mListener.onTextAppend(mEd, text);
                }
            }
        });
    }

    public void setKeyboardListener(OnKeyboardListener listener) {
        this.mListener = listener;
    }

    public interface OnKeyboardListener {
        void onTextAppend(EditText ed, String text);

        void onCloseCallback(EditText ed);

        /**
         * 回退删除
         */
        void onDelectAppend(EditText ed);
    }
}
