package com.blue.car.activity;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.utils.LinearGradientUtil;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/5.
 */
public class LightSettingActivity extends BaseActivity {

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;

    @Bind(R.id.color_control_view)
    RotationImageView colorControlView;

    @Bind(R.id.color_select_view)
    View colorSelectView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_light_setting;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("灯光设置");
        colorControlView.setRotationSelectListener(new RotationImageView.OnRotationSelectListener() {
            @Override
            public void OnRotation(float rotation) {
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

    @OnClick({R.id.lh_btn_back, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
        }
    }
}
