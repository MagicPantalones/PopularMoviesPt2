package io.magics.popularmovies.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.magics.popularmovies.R;


public class ShadowView extends View {

    private Paint mPaint;
    private int mColor;

    public ShadowView(Context context) {
        super(context);
    }

    public ShadowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext()
                .getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ShadowView, 0, 0);
        try {
            mColor = a.getColor(R.styleable.ShadowView_shadowColor, Color.BLACK);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init(){

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(35);
        mPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
    }

    public void setShadowColor(int color){
        mColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
    }
}
