package com.blue.car.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blue.car.R;

import butterknife.Bind;
import butterknife.OnClick;

public class CurrentInfoActivity extends BaseActivity {

    @Bind(R.id.lh_tv_title)
    TextView actionBarTitle;

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
        ViewGroup viewGroup = (ViewGroup) findViewById(parentId);
        TextView leftTextView = (TextView) viewGroup.findViewById(R.id.info_left_text);
        TextView rightTextView = (TextView) viewGroup.findViewById(R.id.info_right_text);
        leftTextView.setText(leftText);
        rightTextView.setText(rightText);
    }

    @Override
    protected void initData() {

    }

    private void processSpeedLimitClick() {
    }

    private void processRemoteSettingClick() {
    }

    private void processLoadMoreInfoClick() {
    }

    @OnClick({R.id.speed_limit, R.id.more_info, R.id.remote_setting})
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
        }
    }
}
