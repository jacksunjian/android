package com.blue.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.utils.UniversalViewUtils;

import butterknife.Bind;
import butterknife.OnClick;

public class CurrentInfoActivity extends BaseActivity {

    @Bind(R.id.lh_tv_title)
    TextView actionBarTitle;
    @Bind(R.id.iv_right)
    ImageView ivRight;
    int isSpeedControl;
    @Bind(R.id.speed_limit)
    ImageView speedLimit;

    @Bind(R.id.remote_setting)
    ImageView remoteSetting;
    @Bind(R.id.lock_iv)
    ImageView lockIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_more_info;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        initActionBarLayout();
        initInfoLayout();
    }

    private void initActionBarLayout() {
        findViewById(R.id.ll_back).setVisibility(View.GONE);
        actionBarTitle.setText("Balance");
    }

    private void initInfoLayout() {
        initNormalInfoLayout(R.id.info_rl, "信息", R.mipmap.gengduo);
        initNormalInfoLayout(R.id.setting_rl, "设置", R.mipmap.gengduo);
        initNormalInfoLayout(R.id.average_speed, "平均速度", "0.0km/h");
        initNormalInfoLayout(R.id.per_meter, "本次里程", "0.0km");
        initNormalInfoLayout(R.id.per_runTime, "本次行驶时间", "5min");
        initNormalInfoLayout(R.id.rest_ride_meter, "剩余行驶里程", "3.2km");
        initNormalInfoLayout(R.id.total_meter, "总里程", "23.2km");
        initNormalInfoLayout(R.id.temperature, "温度", "14℃");
        initNormalInfoLayout(R.id.battery_percent, "剩余电量百分比", "46%");
    }

    private void initNormalInfoLayout(int parentId, String leftText, String rightText) {
        UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightText);
    }

    private void initNormalInfoLayout(int parentId, String leftText, int rightImageResId) {
        UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightImageResId);
    }

    @Override
    protected void initData() {
        isSpeedControl = getIntent().getIntExtra("isLimit", 0);
        invalidateSpeedLimitView(isSpeedControl);
    }

    private void invalidateSpeedLimitView(int isSpeedControl) {
        speedLimit.setBackgroundResource(isSpeedControl == 0 ? R.mipmap.xiansu_off : R.mipmap.xiansu_on);
    }

    private void processSpeedLimitClick() {
        invalidateSpeedLimitView(isSpeedControl = (isSpeedControl + 1) % 2);
    }

    private void processRemoteSettingClick() {
        Intent it_blue = new Intent(this, BlueControlActivity.class);
        startActivity(it_blue);
    }

    private void processLockClick() {
    }

    @OnClick({R.id.lh_btn_back, R.id.ll_back, R.id.speed_limit, R.id.lock_iv, R.id.remote_setting, R.id.info_rl, R.id.setting_rl})
    void bottomFunPanelClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.speed_limit:
                processSpeedLimitClick();
                break;
            case R.id.lock_iv:
                processLockClick();
                break;
            case R.id.remote_setting:
                processRemoteSettingClick();
                break;
            case R.id.info_rl:
                break;
            case R.id.setting_rl:
                Intent it = new Intent(this, SettingMoreActivity.class);
                startActivity(it);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.slide_top_out,
                        R.anim.anim_none_alpha);
        Intent intent = new Intent(this, BlueServiceActivity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }
}
