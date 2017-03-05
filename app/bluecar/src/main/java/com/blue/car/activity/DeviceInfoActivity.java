package com.blue.car.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/5.
 */

public class DeviceInfoActivity extends BaseActivity {
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.current_speed_tv)
    TextView currentSpeedTv;
    @Bind(R.id.average_speed_tv)
    TextView averageSpeedTv;
    @Bind(R.id.allmile_tv)
    TextView allmileTv;
    @Bind(R.id.this_mile_tv)
    TextView thisMileTv;
    @Bind(R.id.temperature_tv)
    TextView temperatureTv;
    @Bind(R.id.top_speed_tv)
    TextView topSpeedTv;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_info;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("设备信息");
    }

    @Override
    protected void initData() {

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
