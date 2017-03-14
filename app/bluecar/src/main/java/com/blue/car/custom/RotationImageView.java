package com.blue.car.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.blue.car.R;

public class RotationImageView extends ImageView {

    private float nowRotation = 0;
    private Matrix bitmapMatrix = new Matrix();
    private Matrix tempMatrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private Bitmap rotationBitmap;

    private OnRotationSelectListener listener;

    public interface OnRotationSelectListener {
        void OnRotation(float rotation);
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
                savedMatrix.set(bitmapMatrix);
                break;
            case MotionEvent.ACTION_MOVE:
                tempMatrix.set(savedMatrix);
                nowRotation = getRotation(event);
                tempMatrix.setRotate(nowRotation, getWidth() / 2, getHeight() / 2);
                bitmapMatrix.set(tempMatrix);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                invokeRotationListener();
                break;
        }
        return true;
    }

    private float getRotation(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();
        float xDiff;
        float yDiff;
        double radians;
        float degrees;
        if (xPos > getWidth() / 2) {
            xDiff = xPos - getWidth() / 2;
            if (yPos >= getHeight() / 2) {
                yDiff = yPos - getHeight() / 2;
                radians = Math.atan2(yDiff, xDiff);
                degrees = (float) Math.toDegrees(radians);
            } else {
                yDiff = getHeight() / 2 - yPos;
                radians = Math.atan2(yDiff, xDiff);
                degrees = 360 - (float) Math.toDegrees(radians);
            }
        } else {
            xDiff = getWidth() / 2 - xPos;
            if (yPos >= getHeight() / 2) {
                yDiff = yPos - getHeight() / 2;
                radians = Math.atan2(yDiff, xDiff);
                degrees = 180 - (float) Math.toDegrees(radians);
            } else {
                yDiff = getHeight() / 2 - yPos;
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
        if (listener != null) {
            listener.OnRotation(nowRotation);
        }
    }

    public float getNowRotation() {
        return nowRotation;
    }

    public OnRotationSelectListener getRotationSelectListener() {
        return listener;
    }

    public void setRotationSelectListener(OnRotationSelectListener listener) {
        this.listener = listener;
    }
}
