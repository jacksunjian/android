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

    private float speed = 0;
    private float speedLimit = 0.1f;
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

    private boolean kmUnit = true;
    private float perMileage = 0;

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
    private float batteryProgress = 0.1f;
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
            this.batteryProgress = 0.1f;
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

    public void setPerMileage(float mileage) {
        this.perMileage = mileage;
    }

    public void setKmUnit(boolean unit) {
        this.kmUnit = unit;
    }

    public String getUnit() {
        if (kmUnit) {
            return "km";
        } else {
            return "mp";
        }
    }

    public String getPerMeterUnit() {
        if (kmUnit) {
            return "km";
        } else {
            return "ml";
        }
    }

    public String getUnitWithTime() {
        String value = getUnit();
        String time = "/h";
        if (!kmUnit) {
            time = "h";
        }
        return value + time;
    }

    public float getResultByUnit(float origin) {
        if (kmUnit) {
            return origin;
        } else {
            return origin * 0.62f;
        }
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
        updateSomeDimen(size);
        setMeasuredDimension(size, size);
    }

    private void updateScaleCircle(int squareSize) {
        strokeWidthBorder = squareSize / 20;
        int strokeMargin = strokeWidthBorder / 2;
        speedScaleCircle = new RectF(strokeMargin, strokeMargin, squareSize - strokeMargin, squareSize - strokeMargin);
        baseSpeedPaint.setStrokeWidth(strokeWidthBorder);
        actualSpeedPaint.setStrokeWidth(strokeWidthBorder);
        batteryProgressPaint.setStrokeWidth(batteryProgressWidth = strokeWidthBorder);
    }

    private void updateSomeDimen(int squareSize) {
        updateScaleCircle(squareSize);
        mileageTextPaint.setTextSize(squareSize / 13.92f);
        speedValuePaint.setTextSize(squareSize / 3.05f);
        speedUnitPaint.setTextSize(squareSize / 12.0f);
        batteryTextPaint.setTextSize(squareSize / 13f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSpeedProgress(canvas);
        drawBatteryProgress(canvas);
        //drawPerMileage(canvas);
        drawSpeedLimitText(canvas);
        drawSpeedText(canvas);
    }

    private void drawSpeedProgress(Canvas canvas) {
        if (Build.VERSION.SDK_INT == 23) {
            canvas.drawArc(speedScaleCircle, startAngle + currentSweepAngle, speedSweepMaxAngle - currentSweepAngle, false, baseSpeedPaint);
            canvas.drawArc(speedScaleCircle, startAngle, currentSweepAngle, false, actualSpeedPaint);
        } else {
            if (currentSweepAngle <= 0) {
                currentSweepAngle = 0.1f;
            }
            canvas.drawArc(speedScaleCircle, startAngle, speedSweepMaxAngle, false, baseSpeedPaint);
            canvas.drawArc(speedScaleCircle, startAngle, currentSweepAngle, false, actualSpeedPaint);
        }
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

    private int dp2px(Context context, int dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @SuppressLint("DefaultLocale")
    private void drawPerMileage(Canvas canvas) {
        int centre = getWidth() / 2;
        int marginTop = (int) (centre * 1.0f / 4);
        int height = (int) (centre * 1.0f / 4);

        String showText = String.format("本次里程 %.1f" + getPerMeterUnit(), perMileage);
        if (mileageTextRect == null) {
            Rect bounds = new Rect();
            mileageTextPaint.getTextBounds(showText, 0, showText.length(), bounds);
            int width = bounds.centerX() + dp2px(getContext(), 8);
            mileageTextRect = new RectF(centre - width, centre + marginTop, centre + width, centre + marginTop + height);
            Paint.FontMetrics fontMetrics = mileageTextPaint.getFontMetrics();
            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            mileageTextDiff = top / 2 + bottom / 2;
        }
        canvas.drawRoundRect(mileageTextRect, 40, 40, mileageRectPaint);
        canvas.drawText(showText, centre, centre + marginTop + height * 1.0f / 2 - mileageTextDiff, mileageTextPaint);
    }

    @SuppressLint("DefaultLocale")
    private void drawSpeedLimitText(Canvas canvas) {
        int centre = getWidth() / 2;
        int marginTop = (int) (centre * 1.0f / 4);
        int height = (int) (centre * 1.0f / 4);
        String showText = String.format("当前限速 %.1f", speedLimit <= 0.1f ? 0 : speedLimit);
        if (mileageTextRect == null) {
            Rect bounds = new Rect();
            mileageTextPaint.getTextBounds(showText, 0, showText.length(), bounds);
            int width = bounds.centerX() + dp2px(getContext(), 8);
            mileageTextRect = new RectF(centre - width, centre + marginTop, centre + width, centre + marginTop + height);
            Paint.FontMetrics fontMetrics = mileageTextPaint.getFontMetrics();
            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            mileageTextDiff = top / 2 + bottom / 2;
        }
        canvas.drawRoundRect(mileageTextRect, 40, 40, mileageRectPaint);
        canvas.drawText(showText, centre, centre + marginTop + height * 1.0f / 2 - mileageTextDiff, mileageTextPaint);
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
        canvas.drawText(String.format("%.1f", getResultByUnit(speed)), getWidth() / 2 - speedValuePaint.getTextSize() / 9, getHeight() / 2, speedValuePaint);
        canvas.drawText(getUnitWithTime(), getWidth() / 2, getHeight() / 2 - getSpeedValueTextHeight() - getHeight() / 18, speedUnitPaint);
    }
}
