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

/**
 * Created by Administrator on 2017/3/4.
 */

public class SettingMoreActivity extends BaseActivity {
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.lightSetting_rl)
    RelativeLayout lightSettingRl;
    @Bind(R.id.speedSetting_rl)
    RelativeLayout speedSettingRl;
    @Bind(R.id.sensorSetting_rl)
    RelativeLayout sensorSettingRl;
    @Bind(R.id.blueSetting_rl)
    RelativeLayout blueSettingRl;
    @Bind(R.id.othersSetting_rl)
    RelativeLayout othersSettingRl;

    Intent it;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settingmore;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("设置");

    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.lh_btn_back, R.id.ll_back, R.id.lightSetting_rl, R.id.speedSetting_rl, R.id.sensorSetting_rl, R.id.blueSetting_rl, R.id.othersSetting_rl})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.lightSetting_rl:
                it = new Intent(this, LightSettingActivity.class);
                startActivity(it);
                break;
            case R.id.speedSetting_rl:
                 it = new Intent(this, SpeedControlActivity.class);
                startActivity(it);
                break;
            case R.id.sensorSetting_rl:
                it = new Intent(this, SensorSettingActivity.class);
                startActivity(it);
                break;
            case R.id.blueSetting_rl:
                it = new Intent(this, BlueSettingActivity.class);
                startActivity(it);
                break;
            case R.id.othersSetting_rl:
                it = new Intent(this, OtherSettingActivity.class);
                startActivity(it);
                break;

        }
    }
}
