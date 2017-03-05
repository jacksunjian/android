package com.blue.car.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.UniversalViewUtils;

import butterknife.Bind;
import butterknife.OnClick;

public class SensorSettingActivity extends BaseActivity {


    @Bind(R.id.lh_tv_title)
    TextView actionBarTitle;
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sensor_setting;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        initActionBar();
        initSettingView();
    }

    private void initActionBar() {
        findViewById(R.id.ll_back).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_right).setVisibility(View.GONE);
        actionBarTitle.setText("传感器设置");
    }

    private void initSettingView() {
        UniversalViewUtils.initNormalInfoLayout(this, R.id.posture_layout, "姿态校准", R.mipmap.gengduo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Switch turningSwitchView = (Switch) UniversalViewUtils.initNormalSwitchLayout(this, R.id.turning_sensitivity_auto_regulation,
                "转向灵敏度自动调节");
        turningSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtils.e("turningSensitivity", "checked:" + isChecked);
            }
        });
        UniversalViewUtils.initNormalSeekBarLayout(this, R.id.turning_sensitivity, "转向灵敏度", 45, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        Switch ridingSwitchView = (Switch) UniversalViewUtils.initNormalSwitchLayout(this, R.id.riding_sensitivity_auto_regulation,
                "骑行灵敏度自动调节");
        ridingSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtils.e("ridingSensitivity", "checked:" + isChecked);
            }
        });
        UniversalViewUtils.initNormalSeekBarLayout(this, R.id.riding_sensitivity, "骑行灵敏度", 60, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        UniversalViewUtils.initNormalSeekBarLayout(this, R.id.power_balance, "助力平衡点", 0, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
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
