package com.blue.car.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.View;

import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.utils.LinearGradientUtil;

import butterknife.Bind;

public class ColorTestActivity extends BaseActivity {

    @Bind(R.id.color_control_view)
    RotationImageView colorControlView;

    @Bind(R.id.color_select_view)
    View colorSelectView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_color_test;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        colorControlView.setRotationSelectListener(new RotationImageView.OnRotationSelectListener() {
            @Override
            public void OnRotation(float rotation) {
            }

            @Override
            public void OnRotationUp(float rotation) {
                colorSelectView.setBackgroundColor(getColor(rotation));
            }
        });
    }

    @Override
    protected void initData() {
    }

    private int getColor(float rotation) {
        int startColor = 0, endColor = 0;
        float tmp = rotation % 360;
        if (tmp <= 60) {
            startColor = Color.RED;
            endColor = Color.YELLOW;
        } else if (tmp <= 120) {
            startColor = Color.YELLOW;
            endColor = Color.GREEN;
        } else if (tmp <= 180) {
            startColor = Color.GREEN;
            endColor = Color.CYAN;
        } else if (tmp < 240) {
            startColor = Color.CYAN;
            endColor = Color.BLUE;
        } else if (tmp <= 300) {
            startColor = Color.BLUE;
            endColor = Color.MAGENTA;
        } else if (tmp <= 360) {
            startColor = Color.MAGENTA;
            endColor = Color.RED;
        }
        float radio = tmp % 60 / 60;
        return LinearGradientUtil.getColor(startColor, endColor, radio);
    }

    @Override
    public void onBackPressed() {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.slide_top_out,
                        R.anim.anim_none_alpha);
        Intent intent = new Intent(this, RemoteTestActivity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }
}
