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
    @Bind(R.id.more_info)
    ImageView moreInfo;
    @Bind(R.id.remote_setting)
    ImageView remoteSetting;

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
        isSpeedControl = getIntent().getIntExtra("isLimit", -1);
        initActionBarLayout();
        initInfoLayout();
        if (isSpeedControl == 0) {
            speedLimit.setBackgroundResource(R.mipmap.xiansu_off);
            isSpeedControl = 1;
        } else {
            speedLimit.setBackgroundResource(R.mipmap.xiansu_on);
            isSpeedControl = 0;
        }

    }

    private void initActionBarLayout() {
        findViewById(R.id.ll_back).setVisibility(View.GONE);
        findViewById(R.id.iv_right).setVisibility(View.VISIBLE);
        actionBarTitle.setText("Balance");
    }

    private void initInfoLayout() {
        initNormalInfoLayout(R.id.per_riding_time, "本次骑行时间", "58s");
        initNormalInfoLayout(R.id.total_meter, "总里程", "6.5km");
        initNormalInfoLayout(R.id.per_meter, "本次行程", "0.0km");
        initNormalInfoLayout(R.id.car_temperature, "车体温度", "16.1℃");
        initNormalInfoLayout(R.id.average_speed, "平均速度", "0.0km/h");
    }

    private void initNormalInfoLayout(int parentId, String leftText, String rightText) {
        UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightText);
    }

    @Override
    protected void initData() {

    }

    private void processSpeedLimitClick() {
        if (isSpeedControl == 0) {
            speedLimit.setBackgroundResource(R.mipmap.xiansu_on);
            isSpeedControl = 1;
        } else {
            speedLimit.setBackgroundResource(R.mipmap.xiansu_off);
            isSpeedControl = 0;
        }
    }

    private void processRemoteSettingClick() {
        Intent it_blue = new Intent(this, BlueControlActivity.class);
        startActivity(it_blue);
    }

    private void processLoadMoreInfoClick() {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.slide_top_out,
                        R.anim.anim_none_alpha);
        Intent intent = new Intent(this, MainActivity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());


    }

    @OnClick({R.id.speed_limit, R.id.more_info, R.id.remote_setting, R.id.iv_right})
    void bottomFunPanelClick(View view) {
        switch (view.getId()) {
            case R.id.speed_limit:
                processSpeedLimitClick();
                break;
            case R.id.more_info:
                processLoadMoreInfoClick();
                break;
            case R.id.remote_setting:
                processRemoteSettingClick();
                break;
            case R.id.iv_right:
                Intent it = new Intent(this, SettingMoreActivity.class);
                startActivity(it);
                break;
        }
    }

}
