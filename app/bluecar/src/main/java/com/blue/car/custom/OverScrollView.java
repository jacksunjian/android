package com.blue.car.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.ScrollView;

import java.lang.reflect.Field;

/**
 * Naive ScrollView subclass that allows you to change the over scroll value and hook
 * a listener that will tell you how far you have over scrolled. It also disables
 * the glow effect at the top edge. The disabling is done by reflection, so it may
 * at some point stop working if the field name changes, but a a global has been set
 * for quick modification if need be.
 * <p>
 * <p>
 * Created by Dejan Ristic on 1/5/15.
 */
public class OverScrollView extends ScrollView {

    private static final String TAG = "OverScrollListView";

    private static final String TOP_EDGE_EFFECT_FIELD = "mEdgeGlowTop"; // Variable to change if field changes.
    private static final String BOTTOM_EDGE_EFFECT_FIELD = "mEdgeGlowBottom"; // Variable to change if field changes.

    private static final int DEFAULT_MAX_Y = 130;

    private int mMaxOverScrollY = DEFAULT_MAX_Y;

    private int mBottomEdge;
    private int mScrollRangeY;

    private boolean isClamped;
    private boolean isOverScrollTop;
    private boolean didStartOverScroll;
    private boolean didFinishOverScroll;

    private View mHeaderView;
    private View mFooterView;

    private Rect mHeaderViewRect;
    private Rect mFooterViewRect;

    private Drawable mHeaderDrawable;
    private Drawable mFooterDrawable;

    private EdgeEffect mTopEdgeEffect;
    private EdgeEffect mBottomEdgeEffect;

    private OnScrollListener scrollListener;
    private OverScrolledListener overScrolledListener = new OverScrolledListener() {
        @Override
        public void overScrolledTop(int scrollY, int maxY, boolean clampedY, boolean didFinishOverScroll) {
        }

        @Override
        public void overScrolledBottom(int scrollY, int maxY, boolean clampedY, boolean didFinishOverScroll) {
        }
    };

    public interface OverScrolledListener {
        void overScrolledTop(int scrollY, int maxY, boolean clampedY, boolean didFinishOverScroll);

        void overScrolledBottom(int scrollY, int maxY, boolean clampedY, boolean didFinishOverScroll);
    }

    public OverScrollView(Context context) {
        this(context, null);
    }

    public OverScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setFadingEdgeLength(0);
        setVerticalFadingEdgeEnabled(false);
        getPrivateFieldMembers();
        initMaxOverScrollY();
    }

    private void initMaxOverScrollY() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float density = metrics.density;
        mMaxOverScrollY = (int) (density * DEFAULT_MAX_Y);
    }

    public void setOverScrollHeader(Drawable drawable) {
        mHeaderDrawable = drawable;
    }

    public void setOverScrollFooter(Drawable drawable) {
        mFooterDrawable = drawable;
    }

    public void setOverScrollListener(OverScrolledListener listener) {
        overScrolledListener = listener;
    }

    public void setScrollViewListener(OnScrollListener listener) {
        scrollListener = listener;
    }

    public void setOverScrollOffsetY(int offset) {
        mMaxOverScrollY = offset;
        updateBounds();
        updateCustomViews();
    }

    public void setOverScrollHeaderView(View view) {
        mHeaderView = view;
    }

    public void setOverScrollFooterView(View view) {
        mFooterView = view;
    }

    private void getPrivateFieldMembers() {
        try {
            mTopEdgeEffect = getTopEdgeEffect();
            mBottomEdgeEffect = getBottomEdgeEffect();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "The Reflection Failed! Check if the field name changed in AbsListView.java inside the AOSP!");
        }
    }

    private EdgeEffect getTopEdgeEffect() throws NoSuchFieldException, IllegalAccessException {
        Field f = ScrollView.class.getDeclaredField(TOP_EDGE_EFFECT_FIELD);
        if (f != null) {
            f.setAccessible(true);
            return (EdgeEffect) f.get(this);
        }
        return null;
    }

    private EdgeEffect getBottomEdgeEffect() throws NoSuchFieldException, IllegalAccessException {
        Field f = ScrollView.class.getDeclaredField(BOTTOM_EDGE_EFFECT_FIELD);
        if (f != null) {
            f.setAccessible(true);
            return (EdgeEffect) f.get(this);
        }
        return null;
    }

    private void reset() {
        didFinishOverScroll = true;
        didStartOverScroll = false;
        if (isOverScrollTop) {
            smoothScrollTo(0, 0);
            overScrolledListener.overScrolledTop(0, mMaxOverScrollY, false, true);
        } else {
            smoothScrollTo(0, mBottomEdge);
            overScrolledListener.overScrolledBottom(0, mMaxOverScrollY, false, true);
        }
    }

    private void update() {
        mBottomEdge = getBottomEdge();
        updateBounds();
        updateCustomViews();
    }

    private void updateCustomViews() {
        updateFooterView();
        updateHeaderView();
    }

    private void updateBounds() {
        if (mHeaderDrawable != null) {
            mHeaderDrawable.setBounds(0, -mMaxOverScrollY, getRight(), 0);
        }
        if (mFooterDrawable != null) {
            mFooterDrawable.setBounds(0, getBottom(), getRight(), getBottom() + mMaxOverScrollY);
        }
    }


    private void updateHeaderView() {
        if (mHeaderView != null) {
            mHeaderViewRect = new Rect();
            mHeaderViewRect.set(0, -mMaxOverScrollY, getRight(), 0);

            int widthSpec = View.MeasureSpec.makeMeasureSpec(mHeaderViewRect.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(mHeaderViewRect.height(), View.MeasureSpec.EXACTLY);

            mHeaderView.measure(widthSpec, heightSpec);
            mHeaderView.layout(0, 0, mHeaderViewRect.width(), mHeaderViewRect.height());
        }
    }

    private void updateFooterView() {
        if (mFooterView != null) {
            mFooterViewRect = new Rect();
            mFooterViewRect.set(0, mBottomEdge, getRight(), mBottomEdge + mMaxOverScrollY);

            int widthSpec = View.MeasureSpec.makeMeasureSpec(mFooterViewRect.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(mFooterViewRect.height(), View.MeasureSpec.EXACTLY);

            mFooterView.measure(widthSpec, heightSpec);
            mFooterView.layout(0, 0, mFooterViewRect.width(), mFooterViewRect.height());
        }
    }

    private void drawHeader(Canvas canvas) {
        if (mHeaderDrawable != null) {
            mHeaderDrawable.draw(canvas);
        }
        if (mHeaderView != null && mHeaderViewRect != null) {
            canvas.save();
            canvas.translate(mHeaderViewRect.left, mHeaderViewRect.top);
            mHeaderView.draw(canvas);
            canvas.restore();
        }
    }

    private int getBottomEdge() {
        View view = null;
        for (int i = 0; i < getChildCount(); i++) {
            if (i == getChildCount() - 1) {
                view = getChildAt(i);
            }
        }
        if (view != null) {
            Rect rect = new Rect();
            view.getDrawingRect(rect);
            return rect.bottom;
        } else {
            return -1;
        }
    }

    private void drawFooter(Canvas canvas) {
        if (mFooterDrawable != null) {
            mFooterDrawable.draw(canvas);
        }
        if (mFooterView != null && mFooterViewRect != null) {
            canvas.save();
            canvas.translate(mFooterViewRect.left, mFooterViewRect.top);
            mFooterView.draw(canvas);
            canvas.restore();
        }
    }

    private void finishEdgeEffects() {
        if (mTopEdgeEffect != null) {
            mTopEdgeEffect.finish();
        }
        if (mBottomEdgeEffect != null) {
            mBottomEdgeEffect.finish();
        }
    }

    private void startOverScroll(int scrollY) {
        if ((scrollY < 0 || scrollY > mScrollRangeY) && !didStartOverScroll) {
            isOverScrollTop = scrollY < 0;
            didStartOverScroll = true;
            didFinishOverScroll = false;
        }
    }

    private void checkIfFinished(int scrollY) {
        if ((scrollY == 0 || scrollY == mScrollRangeY) && didStartOverScroll) {
            didStartOverScroll = false;
            didFinishOverScroll = true;
        }
    }

    private void dispatchListener(int scrollY, boolean clampedY) {
        if (didStartOverScroll) {
            if (isOverScrollTop) {
                invokeOverScrollTop(scrollY, clampedY);
            } else {
                invokeOverScrollBottom(scrollY, clampedY);
            }
        }
    }

    private void invokeOverScrollTop(int scrollY, boolean clampedY) {
        if (overScrolledListener != null) {
            overScrolledListener.overScrolledTop(Math.abs(scrollY), mMaxOverScrollY, clampedY, didFinishOverScroll);
        }
    }

    private void invokeOverScrollBottom(int scrollY, boolean clampedY) {
        if (overScrolledListener != null) {
            overScrolledListener.overScrolledBottom(Math.abs(scrollY - mScrollRangeY), mMaxOverScrollY, clampedY, didFinishOverScroll);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        update();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (didStartOverScroll) {
                    if (isClamped) {
                        reset();
                        return true;
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (mScrollRangeY == 0) {
            mScrollRangeY = scrollRangeY;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxOverScrollY, isTouchEvent);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollListener != null) {
            scrollListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

        isClamped = clampedY;

        startOverScroll(scrollY);
        checkIfFinished(scrollY);

        if (overScrolledListener != null) {
            dispatchListener(scrollY, clampedY);
        } else {
            Log.v(TAG, "No scroll listener set");
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        finishEdgeEffects();
        drawHeader(canvas);
        drawFooter(canvas);
    }
}
