package com.blue.car.activity;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.service.BluetoothConstant;

import butterknife.Bind;
import butterknife.OnClick;

public class RemoteTestActivity extends BaseActivity {

    @Bind(R.id.remote_control_view)
    RotationImageView controlView;

    private int scaledTouchSlop = 0;
    private int scaledMinimumFlingVelocity = 0;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_remote_test;
    }

    @Override
    protected void initConfig() {
        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());
        scaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        scaledMinimumFlingVelocity = ViewConfiguration.get(this).getScaledMinimumFlingVelocity();
    }

    @Override
    protected void initView() {
        controlView.setRotationDiff(90);
        controlView.setOnMoveScaleChangedListener(new RotationImageView.OnMoveScaleChangedListener() {
            @Override
            public void onScaleChanged(float xScale, float yScale) {
                if (BluetoothConstant.USE_DEBUG) {
                    Log.e("AA-xSpeed:" + yScale * 7000, "ySpeed:" + xScale * 3500);
                }
            }

            @Override
            public void onLongScaleChanged(float xScale, float yScale) {
            }
        });
    }

    @Override
    protected void initData() {
    }

    @OnClick(R.id.goto_color_test)
    void onGotoColorTest() {
        Intent intent = new Intent(this, ColorTestActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            float y = e2.getY() - e1.getY();
            if (y < -scaledTouchSlop
                    && Math.abs(velocityX) < Math.abs(velocityY)
                    && Math.abs(velocityY) > scaledMinimumFlingVelocity) {
                actionGestureFlingUp();
                return true;
            }
            return false;
        }
    }

    private void actionGestureFlingUp() {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.slide_bottom_in,
                        R.anim.anim_none_alpha);
        Intent intent = new Intent(this, ColorTestActivity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }
}
