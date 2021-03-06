package com.feipai.flypai.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.feipai.flypai.R;

public class FPImageButton extends AppCompatImageButton {
    /**
     * 按钮的背景色
     */
    private int backColor = 0;
    /**
     * 按钮被按下时的背景色
     */
    private int backColorPress = 0;
    /**
     * 按钮的背景图片
     */
    private Drawable backGroundDrawable = null;
    /**
     * 按钮被按下时显示的背景图片
     */
    private Drawable backGroundDrawablePress = null;
    /**
     * 按钮的图片
     */
    private Drawable imageDrawable = null;
    /**
     * 按钮被按下时显示的图片
     */
    private Drawable imageDrawablePress = null;

    private GradientDrawable gradientDrawable = null;
    /**
     * 是否设置圆角或者圆形等样式
     */
    private boolean fillet = false;
    /**
     * 标示onTouch方法的返回值，用来解决onClick和onTouch冲突问题
     */
    private boolean isCost = true;

    public FPImageButton(Context context) {
        super(context, null);
    }

    public FPImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FPImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FPButton, defStyle, 0);
        if (a != null) {
            //设置背景色
            ColorStateList colorList = a.getColorStateList(R.styleable.FPButton_backColor);
            if (colorList != null) {
                backColor = colorList.getColorForState(getDrawableState(), 0);
                if (backColor != 0) {
                    setBackgroundColor(backColor);
                }
            }
            //记录按钮被按下时的背景色
            ColorStateList colorListPress = a.getColorStateList(R.styleable.FPButton_backColorPress);
            if (colorListPress != null) {
                backColorPress = colorListPress.getColorForState(getDrawableState(), 0);
            }
            //设置背景图片，若backColor与backGroundDrawable同时存在，则backGroundDrawable将覆盖backColor
            backGroundDrawable = a.getDrawable(R.styleable.FPButton_backGroundImage);
            if (backGroundDrawable != null) {
                setBackgroundDrawable(backGroundDrawable);
            }
            //记录按钮被按下时的背景图片
            backGroundDrawablePress = a.getDrawable(R.styleable.FPButton_backGroundImagePress);
            //设置图片
            imageDrawable = a.getDrawable(R.styleable.FPButton_imageNormal);
            if (imageDrawable != null) {
                setImageDrawable(imageDrawable);
            }
            //记录按钮被按下时的图片
            imageDrawablePress = a.getDrawable(R.styleable.FPButton_imagePress);

            //设置圆角或圆形等样式的背景色
            fillet = a.getBoolean(R.styleable.FPButton_fillet, false);
            if (fillet) {
                getGradientDrawable();
                if (backColor != 0) {
                    gradientDrawable.setColor(backColor);
                    setBackgroundDrawable(gradientDrawable);
                }
            }
            //设置圆角矩形的角度，fillet为true时才生效
            float radius = a.getFloat(R.styleable.FPButton_radius, 0);
            if (fillet && radius != 0) {
                setRadius(radius);
            }
            //设置按钮形状，fillet为true时才生效
            int shape = a.getInteger(R.styleable.FPButton_shape, 0);
            if (fillet && shape != 0) {
                setShape(shape);
            }
            a.recycle();
        }
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                //根据touch事件设置按下抬起的样式
                return setTouchStyle(event.getAction());
            }
        });
    }

    /**
     * 根据按下或者抬起来改变背景和文字样式
     *
     * @param state
     * @return isCost
     * 为解决onTouch和onClick冲突的问题
     * 根据事件分发机制，如果onTouch返回true，则不响应onClick事件
     * 因此采用isCost标识位，当用户设置了onClickListener则onTouch返回false
     */
    private boolean setTouchStyle(int state) {
        if (state == MotionEvent.ACTION_DOWN) {
            if (backColorPress != 0) {
                if (fillet) {
                    gradientDrawable.setColor(backColorPress);
                    setBackgroundDrawable(gradientDrawable);
                } else {
                    setBackgroundColor(backColorPress);
                }
            }
            if (backGroundDrawablePress != null) {
                setBackgroundDrawable(backGroundDrawablePress);
            }
            if (imageDrawablePress != null) {
                setImageDrawable(imageDrawablePress);
            }
        }
        if (state == MotionEvent.ACTION_UP) {
            if (backColor != 0) {
                if (fillet) {
                    gradientDrawable.setColor(backColor);
                    setBackgroundDrawable(gradientDrawable);
                } else {
                    setBackgroundColor(backColor);
                }
            }
            if (backGroundDrawable != null) {
                setBackgroundDrawable(backGroundDrawable);
            }
            if (imageDrawable != null) {
                setImageDrawable(imageDrawable);
            }
        }
        return isCost;
    }

    /**
     * 重写setOnClickListener方法，解决onTouch和onClick冲突问题
     *
     * @param l
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        isCost = false;
    }

    /**
     * 设置按钮的背景色
     *
     * @param backColor
     */
    public void setBackColor(int backColor) {
        this.backColor = backColor;
        if (fillet) {
            gradientDrawable.setColor(backColor);
            setBackgroundDrawable(gradientDrawable);
        } else {
            setBackgroundColor(backColor);
        }
    }

    /**
     * 设置按钮被按下时的背景色
     *
     * @param backColorPress
     */
    public void setBackColorPress(int backColorPress) {
        this.backColorPress = backColorPress;
    }

    /**
     * 设置按钮的背景图片
     *
     * @param backGroundDrawable
     */
    public void setBackGroundDrawable(Drawable backGroundDrawable) {
        this.backGroundDrawable = backGroundDrawable;
        super.setBackgroundDrawable(backGroundDrawable);
    }

    /**
     * 设置按钮被按下时的背景图片
     *
     * @param backGroundDrawablePress
     */
    public void setBackGroundDrawablePress(Drawable backGroundDrawablePress) {
        this.backGroundDrawablePress = backGroundDrawablePress;
    }

    /**
     * 设置图片
     */
    public void setImageDrawable(Drawable drawable) {
        this.imageDrawable = drawable;
        super.setImageDrawable(drawable);
    }

    /**
     * 设置按钮被按下时的图片
     */
    public void setImageDrawablePress(Drawable drawable) {
        this.imageDrawablePress = drawable;
        setImageDrawable(drawable);
    }

    /**
     * 设置按钮是否设置圆角或者圆形等样式
     *
     * @param fillet
     */
    public void setFillet(boolean fillet) {
        this.fillet = fillet;
        getGradientDrawable();
    }

    /**
     * 设置圆角按钮的角度
     *
     * @param radius
     */
    public void setRadius(float radius) {
        if (!fillet) return;
        getGradientDrawable();
        gradientDrawable.setCornerRadius(radius);
        super.setBackgroundDrawable(gradientDrawable);
    }

    /**
     * 设置按钮的形状
     *
     * @param shape
     */
    public void setShape(int shape) {
        if (!fillet) return;
        getGradientDrawable();
        gradientDrawable.setShape(shape);
        setBackgroundDrawable(gradientDrawable);
    }

    private void getGradientDrawable() {
        if (gradientDrawable == null) {
            gradientDrawable = new GradientDrawable();
        }
    }
}
