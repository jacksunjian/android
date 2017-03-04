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
 * Created by Administrator on 2017/3/4.
 */

public class BatteryInfoActivity extends BaseActivity {
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.rest_battery_tv)
    TextView restBatteryTv;
    @Bind(R.id.rest_percent_tv)
    TextView restPercentTv;
    @Bind(R.id.electric_tv)
    TextView electricTv;
    @Bind(R.id.voltage_tv)
    TextView voltageTv;
    @Bind(R.id.temperature_tv)
    TextView temperatureTv;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_battery_info;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("电池信息");
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
