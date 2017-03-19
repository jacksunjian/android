package com.blue.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blue.car.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoMoreActivity extends BaseActivity {

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.infoSetting_rl)
    RelativeLayout infoSettingRl;
    @Bind(R.id.batterySetting_rl)
    RelativeLayout batterySettingRl;
    @Bind(R.id.blackSetting_rl)
    RelativeLayout blackSettingRl;
    Intent it;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_info_more;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("信息");
    }

    @Override
    protected void initData() {

    }


    @OnClick({R.id.lh_btn_back, R.id.ll_back, R.id.infoSetting_rl, R.id.batterySetting_rl, R.id.blackSetting_rl})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
                onBackPressed();
            case R.id.ll_back:
                break;
            case R.id.infoSetting_rl:
                it = new Intent(this, DeviceInfoActivity.class);
                startActivity(it);
                break;
            case R.id.batterySetting_rl:
                it = new Intent(this, BatteryInfoActivity.class);
                startActivity(it);
                break;
            case R.id.blackSetting_rl:
                it = new Intent(this, BlackBoxActivity.class);
                startActivity(it);
                break;
        }
    }
}
