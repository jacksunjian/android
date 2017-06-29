package com.blue.car.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;


public class MyScrollView extends ScrollView {

    private boolean isOpen = false;

    private int detailLayoutPosition = 0;
    private int threshold = 180;
    private int offsetSum = 0;
    private int lastYPos;

    private OnScrollChangedListener onScrollChangedListener;

    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener;
    }

    public MyScrollView(Context context) {
        this(context, null);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastYPos = (int) event.getY();
                offsetSum = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                y = (int) event.getRawY();
                offsetSum += y - lastYPos;
                lastYPos = y;
                break;
            case MotionEvent.ACTION_UP:
                if (offsetSum > 0) {
                    if (isOpen) {
                        if (offsetSum > threshold) {
                            scrollToTopPosition();
                        } else {
                            scrollToDetailPosition();
                        }
                    } else {
                        scrollToTopPosition();
                    }
                } else {
                    if (!isOpen) {
                        if (offsetSum < -threshold) {
                            scrollToDetailPosition();
                        } else {
                            scrollToTopPosition();
                        }
                    } else {
                        scrollToDetailPosition();
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void scrollToDetailPosition() {
        smoothScrollTo(0, detailLayoutPosition);
        isOpen = true;
    }

    private void scrollToTopPosition() {
        smoothScrollTo(0, 0);
        isOpen = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return super.onInterceptTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public void setDetailLayoutPosition(int position) {
        this.detailLayoutPosition = position;
    }

    public int getDetailLayoutPosition() {
        return detailLayoutPosition;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return this.threshold;
    }
}
