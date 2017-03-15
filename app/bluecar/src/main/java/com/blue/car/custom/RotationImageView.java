package com.blue.car.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class RotationImageView extends ImageView {

    private float nowRotation = 0;
    private int rotationDiff = 0;

    private Matrix bitmapMatrix = new Matrix();
    private Matrix tempMatrix = new Matrix();
    private Bitmap rotationBitmap;

    private OnRotationSelectListener rotationSelectListener;
    private OnMoveScaleChangedListener onMoveScaleChangedListener;

    public interface OnRotationSelectListener {
        void OnRotation(float rotation);
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
    }

    private void initBitmapAttribute(AttributeSet attrs) {
        int srcId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
        rotationBitmap = BitmapFactory.decodeStream(getResources().openRawResource(srcId));
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
                nowRotation = getRotation(event) + rotationDiff;
                tempMatrix.setRotate(nowRotation, getWidth() / 2, getHeight() / 2);
                bitmapMatrix.set(tempMatrix);
                invalidate();
                invokeMoveScaleChangeListener();
                break;
            case MotionEvent.ACTION_UP:
                invokeRotationListener();
                break;
        }
        return true;
    }

    private float xPos, yPos;

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
    }

    private void invokeRotationListener() {
        if (rotationSelectListener != null) {
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
}
