package com.blue.car.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.blue.car.utils.ScreenUtils;

public class RotationImageView extends ImageView {
    private static final String TAG = RotationImageView.class.getSimpleName();

    private float nowRotation = 0;
    private int rotationDiff = 0;

    private float borderXPos, borderYPos;
    private float xPos, yPos;

    private Matrix bitmapMatrix = new Matrix();
    private Matrix tempMatrix = new Matrix();
    private Bitmap rotationBitmap;

    private Bitmap borderBitmap;

    private boolean constantlyRotationSelect = false;
    private OnRotationSelectListener rotationSelectListener;
    private OnMoveScaleChangedListener onMoveScaleChangedListener;

    private Region region = new Region();
    private float borderScaleLimitLength;

    public interface OnRotationSelectListener {
        void OnRotation(float rotation);

        void OnRotationUp(float rotation);
    }

    public interface OnMoveScaleChangedListener {
        void onScaleChanged(float xScale, float yScale);

        void onLongScaleChanged(float xScale, float yScale);
    }

    public RotationImageView(Context context) {
        this(context, null);
    }

    public RotationImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RotationImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initBitmapAttribute(attrs);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Matrix matrix = new Matrix();
                matrix.postScale(getWidth() * 1.0f / rotationBitmap.getWidth(),
                        getHeight() * 1.0f / rotationBitmap.getHeight());
                rotationBitmap = Bitmap.createBitmap(rotationBitmap, 0, 0, rotationBitmap.getWidth(),
                        rotationBitmap.getHeight(), matrix, true);
                if (borderBitmap != null) {
                    resetToOriginalRotation();
                    initBorderScaleLimitLength();
                    initCircleRegion();
                }
            }
        });
    }

    private void initBorderScaleLimitLength() {
        borderScaleLimitLength = getWidth() / 2 -
                ScreenUtils.dip2px(getContext(), 12) - borderBitmap.getWidth() / 2;
    }

    private void initCircleRegion() {
        Path path = new Path();
        path.addCircle(getWidth() / 2, getHeight() / 2, borderScaleLimitLength, Path.Direction.CCW);
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        region = new Region();
        region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));
    }

    private void initBitmapAttribute(AttributeSet attrs) {
        int srcId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
        rotationBitmap = BitmapFactory.decodeStream(getResources().openRawResource(srcId));
    }

    private void setBorderPos(MotionEvent event) {
        if (borderBitmap == null) {
            return;
        }
        float x = event.getX();
        float y = event.getY();
        if (region.contains((int) x, (int) y)) {
            borderXPos = event.getX();
            borderYPos = event.getY();
        } else {
            float limitLength = borderScaleLimitLength;
            float xLength = Math.abs(x - getWidth() / 2);
            float yLength = Math.abs(y - getHeight() / 2);
            float third = (float) Math.sqrt(xLength * xLength + yLength * yLength);
            if (x <= getWidth() / 2) {
                borderXPos = xLength - limitLength / third * xLength + event.getX();
            } else {
                borderXPos = getWidth() / 2 + limitLength / third * xLength;
            }
            if (y <= getHeight() / 2) {
                borderYPos = (third - limitLength) / third * yLength + event.getY();
            } else {
                borderYPos = limitLength / third * yLength + getHeight() / 2;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                tempMatrix.set(bitmapMatrix);
                break;
            case MotionEvent.ACTION_MOVE:
                setBorderPos(event);
                invalidateRotation(getRotation(event) + rotationDiff);
                invokeMoveScaleChangeListener();
                invokeConstantlyRotationListener();
                break;
            case MotionEvent.ACTION_UP:
                invokeRotationListener();
                break;
        }
        return true;
    }

    private float getRotation(MotionEvent event) {
        xPos = event.getX();
        yPos = event.getY();
        float xDiff, yDiff;
        double radians;
        float degrees;
        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;

        if (xPos > halfWidth) {
            xDiff = xPos - halfWidth;
            if (yPos >= halfHeight) {
                yDiff = yPos - halfHeight;
                radians = Math.atan2(yDiff, xDiff);
                degrees = (float) Math.toDegrees(radians);
            } else {
                yDiff = halfHeight - yPos;
                radians = Math.atan2(yDiff, xDiff);
                degrees = 360 - (float) Math.toDegrees(radians);
            }
        } else {
            xDiff = halfWidth - xPos;
            if (yPos >= halfHeight) {
                yDiff = yPos - halfHeight;
                radians = Math.atan2(yDiff, xDiff);
                degrees = 180 - (float) Math.toDegrees(radians);
            } else {
                yDiff = halfHeight - yPos;
                radians = Math.atan2(yDiff, xDiff);
                degrees = 180 + (float) Math.toDegrees(radians);
            }
        }
        return degrees;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(rotationBitmap, bitmapMatrix, null);
        canvas.restore();
        drawBorderDrawable(canvas);
    }

    private void drawBorderDrawable(Canvas canvas) {
        if (borderBitmap == null) {
            return;
        }
        canvas.drawBitmap(borderBitmap, borderXPos - borderBitmap.getWidth() / 2,
                borderYPos - borderBitmap.getHeight() / 2, null);
    }

    private void invokeRotationListener() {
        if (rotationSelectListener != null) {
            rotationSelectListener.OnRotationUp(nowRotation);
        }
    }

    private void invokeConstantlyRotationListener() {
        if (constantlyRotationSelect && rotationSelectListener != null) {
            rotationSelectListener.OnRotation(nowRotation);
        }
    }

    private void invokeMoveScaleChangeListener() {
        if (onMoveScaleChangedListener != null) {
            int width = getWidth();
            int height = getHeight();
            float xTempPos = xPos;
            float yTempPos = yPos;
            if (xPos > width) {
                xTempPos = width;
            }
            if (yPos > height) {
                yTempPos = height;
            }
            onMoveScaleChangedListener.onScaleChanged((xTempPos - width / 2) / (width / 2), (height / 2 - yTempPos) / (height / 2));
            onMoveScaleChangedListener.onLongScaleChanged(xTempPos / width, yTempPos / height);
        }
    }

    public float getNowRotation() {
        return nowRotation;
    }

    public OnRotationSelectListener getRotationSelectListener() {
        return rotationSelectListener;
    }

    public void setRotationSelectListener(OnRotationSelectListener listener) {
        this.rotationSelectListener = listener;
    }

    public void setOnMoveScaleChangedListener(OnMoveScaleChangedListener listener) {
        this.onMoveScaleChangedListener = listener;
    }

    public OnMoveScaleChangedListener getOnMoveScaleChangedListener() {
        return this.onMoveScaleChangedListener;
    }

    public int getRotationDiff() {
        return rotationDiff;
    }

    public void setRotationDiff(int rotationDiff) {
        this.rotationDiff = rotationDiff;
    }

    public void setConstantlyRotationSelect(boolean constantly) {
        this.constantlyRotationSelect = constantly;
    }

    public boolean isConstantlyRotationSelect() {
        return this.constantlyRotationSelect;
    }

    public void resetToOriginalRotation() {
        borderXPos = getWidth() / 2;
        borderYPos = getHeight() / 2;
        invalidateRotation(0);
    }

    private int getBorderBitmapWidth() {
        return borderBitmap == null ? 0 : borderBitmap.getWidth();
    }

    private int getBorderBitmapHeight() {
        return borderBitmap == null ? 0 : borderBitmap.getHeight();
    }

    public void invalidateRotation(float rotation) {
        tempMatrix.setRotate(nowRotation = rotation, getWidth() / 2, getHeight() / 2);
        bitmapMatrix.set(tempMatrix);
        invalidate();
    }

    public void invalidateRotationWithDiff(float rotation) {
        tempMatrix.setRotate(nowRotation = (rotation + rotationDiff), getWidth() / 2, getHeight() / 2);
        bitmapMatrix.set(tempMatrix);
        invalidate();
    }

    public void setBorderBitmap(int drawableRes) {
        setBorderBitmap(drawableRes, -1, -1);
    }

    public void setBorderBitmap(int drawableRes, int targetWidth, int targetHeight) {
        if (drawableRes <= 0) {
            Log.w(TAG, "detect the border drawable res is invalid:" + drawableRes);
            return;
        }
        try {
            borderBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
            boolean useScaleTargetSize = (targetHeight == -1 && targetWidth == -1);
            if (useScaleTargetSize) {
                targetWidth = borderBitmap.getWidth();
                targetHeight = borderBitmap.getHeight();
            }
            borderBitmap = Bitmap.createScaledBitmap(borderBitmap, targetWidth,
                    targetHeight, true);
            resetToOriginalRotation();
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }
}
