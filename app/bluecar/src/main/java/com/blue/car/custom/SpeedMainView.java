package com.blue.car.custom;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SpeedMainView extends View {

    private int strokeWidthBorder;
    private RectF speedScaleCircle;
    private Paint baseSpeedPaint;
    private Paint actualSpeedPaint;

    private ValueAnimator speedProgressValueAnimator;
    private ValueAnimator batteryProgressValueAnimator;
    private int valueAnimatorDuration = 800;

    private float speed=0;
    private float speedLimit = 30;
    private int startAngle = 160;
    private int speedSweepMaxAngle = 220;
    private float currentSweepAngle = 0.1f;

    private Paint batteryProgressPaint;
    private TextPaint batteryTextPaint;
    private int batteryProgressColor = Color.parseColor("#001323");
    private int batteryProgressWidth = 24;

    private TextPaint speedValuePaint;
    private TextPaint speedUnitPaint;
    private int speedValueTextRectHeight = -1;

    private Paint mileageRectPaint;
    private TextPaint mileageTextPaint;
    private RectF mileageTextRect;
    private float mileageTextDiff = -1;

    private int[] batteryProgressColors = {
            Color.parseColor("#00B5FD"), Color.parseColor("#00E3D9")};

    public SpeedMainView(Context context) {
        this(context, null);
    }

    public SpeedMainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedMainView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SpeedMainView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initSize();
        initPaint();
        initValueAnimator();
    }

    private void initSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        strokeWidthBorder = 24;
    }

    private float batteryProgressMax = 4000;
    private float batteryProgress = 3600;
    private RectF batteryProgressRectF;

    private void initPaint() {
        initSpeedProgressPaint();
        initSpeedValuePaint();
        initBatteryProgressPaint();
        initMileagePaint();
    }

    private void initSpeedProgressPaint() {
        baseSpeedPaint = new Paint();
        baseSpeedPaint.setAntiAlias(true);
        baseSpeedPaint.setColor(Color.WHITE);
        baseSpeedPaint.setStrokeWidth(strokeWidthBorder);
        baseSpeedPaint.setStyle(Paint.Style.STROKE);
        baseSpeedPaint.setPathEffect(new DashPathEffect(new float[]{10, 14}, 0));

        actualSpeedPaint = new Paint(baseSpeedPaint);
        actualSpeedPaint.setColor(Color.RED);
    }

    private void initSpeedValuePaint() {
        speedValuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        speedValuePaint.setColor(Color.WHITE);
        speedValuePaint.setTextAlign(Paint.Align.CENTER);
        speedValuePaint.setTextSkewX(-0.28f);
        speedValuePaint.setTextSize(190);

        speedUnitPaint = new TextPaint(speedValuePaint);
        speedUnitPaint.setTextSize(50);
        speedUnitPaint.setTextSkewX(0f);

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/zaozigongfang.otf");
        setSpeedTextTypeface(tf);
    }

    private void initBatteryProgressPaint() {
        batteryProgressPaint = new Paint();
        batteryProgressPaint.setAntiAlias(true);
        batteryProgressPaint.setStrokeWidth(batteryProgressWidth);
        batteryProgressPaint.setColor(batteryProgressColor);
        batteryProgressPaint.setStyle(Paint.Style.STROKE);
        batteryProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        batteryTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        batteryTextPaint.setColor(batteryProgressColors[0]);
        batteryTextPaint.setTextSize(48);
        batteryTextPaint.setStrokeCap(Paint.Cap.ROUND);
        batteryTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initMileagePaint() {
        mileageRectPaint = new Paint();
        mileageRectPaint.setAntiAlias(true);
        mileageRectPaint.setColor(Color.WHITE);
        mileageRectPaint.setStrokeWidth(2);
        mileageRectPaint.setStyle(Paint.Style.STROKE);
        mileageRectPaint.setStrokeCap(Paint.Cap.ROUND);

        mileageTextPaint = new TextPaint(Paint.LINEAR_TEXT_FLAG);
        mileageTextPaint.setAntiAlias(true);
        mileageTextPaint.setTextAlign(Paint.Align.CENTER);
        mileageTextPaint.setColor(Color.WHITE);
        mileageTextPaint.setTextSize(42);
    }

    public void setSpeedTextTypeface(Typeface typeface) {
        if (typeface == null) {
            return;
        }
        speedValuePaint.setTypeface(typeface);
        invalidate();
    }

    private void initValueAnimator() {
        speedProgressValueAnimator = new ValueAnimator();
        speedProgressValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        speedProgressValueAnimator.addUpdateListener(new SpeedProgressAnimatorListenerImp());

        batteryProgressValueAnimator = new ValueAnimator();
        batteryProgressValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        batteryProgressValueAnimator.addUpdateListener(new BatteryProgressAnimatorListenerImp());
    }

    private class SpeedProgressAnimatorListenerImp implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            Float value = (Float) valueAnimator.getAnimatedValue();
            updateValueScale(value);
            invalidate();
        }
    }

    private class BatteryProgressAnimatorListenerImp implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            Float value = (Float) valueAnimator.getAnimatedValue();
            updateBatteryValueScale(value);
            invalidate();
        }
    }

    private void updateValueScale(float value) {
        if (value < 0) {
            this.speed = 0;
        } else if (value > speedLimit) {
            this.speed = speedLimit;
        } else {
            this.speed = value;
        }
        currentSweepAngle = (speed / speedLimit * speedSweepMaxAngle);
    }

    private void updateBatteryValueScale(float value) {
        if (value < 0) {
            this.batteryProgress = 0;
        } else if (value > batteryProgressMax) {
            this.batteryProgress = batteryProgressMax;
        } else {
            this.batteryProgress = value;
        }
    }

    public void setSpeed(float speedValue) {
        if (speedProgressValueAnimator != null) {
            speedProgressValueAnimator.setFloatValues(speed, speedValue);
            speedProgressValueAnimator.setDuration(valueAnimatorDuration);
            speedProgressValueAnimator.start();
        }
    }

    public void setSpeedLimit(float limitValue) {
        this.speedLimit = limitValue;
        setSpeed(speed);
    }

    public void setValueAnimatorDuration(int duration) {
        this.valueAnimatorDuration = duration;
    }

    public void setBatteryProgressMax(int max) {
        this.batteryProgressMax = max;
        setBatteryProgress(batteryProgress);
    }

    public void setBatteryProgress(float value) {
        if (batteryProgressValueAnimator != null) {
            batteryProgressValueAnimator.setFloatValues(batteryProgress, value);
            batteryProgressValueAnimator.setDuration(valueAnimatorDuration);
            batteryProgressValueAnimator.start();
        }
    }

    // percent must be less than 1
    public void setBatteryPercent(float percent) {
        setBatteryProgress(batteryProgressMax * percent);
    }

    private float perMileage = 0;

    public void setPerMileage(float mileage) {
        this.perMileage = mileage;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width > height) {
            size = height;
        } else {
            size = width;
        }
        updateScaleCircle(size);
        setMeasuredDimension(size, size);
    }

    private void updateScaleCircle(int squareSize) {
        int strokeMargin = strokeWidthBorder / 2;
        speedScaleCircle = new RectF(strokeMargin, strokeMargin, squareSize - strokeMargin, squareSize - strokeMargin);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSpeedProgress(canvas);
        drawBatteryProgress(canvas);
        drawPerMileage(canvas);
        drawSpeedText(canvas);
    }

    private void drawSpeedProgress(Canvas canvas) {
        canvas.drawArc(speedScaleCircle, startAngle, speedSweepMaxAngle, false, baseSpeedPaint);
        canvas.drawArc(speedScaleCircle, startAngle, currentSweepAngle, false, actualSpeedPaint);
    }

    @SuppressLint("DefaultLocale")
    private void drawBatteryProgress(Canvas canvas) {
        int centre = getWidth() / 2;
        int radius = centre - batteryProgressWidth / 2;
        if (batteryProgressRectF == null) {
            batteryProgressRectF = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);
            SweepGradient sweepGradient = new SweepGradient(centre, centre, batteryProgressColors, new float[]{0, 0.3055f});
            batteryProgressPaint.setShader(sweepGradient);
        }
        float progressPercent = 1.0f * batteryProgress / batteryProgressMax;
        canvas.drawArc(batteryProgressRectF, 145, -1f * 110 * progressPercent, false, batteryProgressPaint);
        canvas.drawText(String.format("%.0f%%", progressPercent * 100), getWidth() / 2, getHeight() - batteryProgressWidth * 2, batteryTextPaint);
    }

    @SuppressLint("DefaultLocale")
    private void drawPerMileage(Canvas canvas) {
        int centre = getWidth() / 2;
        int marginTop = 74;
        int height = 74;

        if (mileageTextRect == null) {
            mileageTextRect = new RectF(centre - centre * 2 / 3, centre + marginTop, centre + centre * 2 / 3, centre + marginTop + height);
            Paint.FontMetrics fontMetrics = mileageTextPaint.getFontMetrics();
            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            mileageTextDiff = top / 2 + bottom / 2;
        }
        canvas.drawRoundRect(mileageTextRect, 40, 40, mileageRectPaint);
        canvas.drawText(String.format("本次里程 %.1fkm", perMileage), centre, centre + marginTop + height * 1.0f / 2 - mileageTextDiff, mileageTextPaint);
    }

    private int getSpeedValueTextHeight() {
        if (speedValueTextRectHeight > 0) {
            return speedValueTextRectHeight;
        }
        Rect rect = new Rect();
        speedValuePaint.getTextBounds("00", 0, 1, rect);
        return speedValueTextRectHeight = rect.height();
    }

    @SuppressLint("DefaultLocale")
    private void drawSpeedText(Canvas canvas) {
        canvas.drawText(String.format("%.1f", speed), getWidth() / 2, getHeight() / 2, speedValuePaint);
        canvas.drawText("km/h", getWidth() / 2, getHeight() / 2 - getSpeedValueTextHeight() - 33, speedUnitPaint);
    }
}
