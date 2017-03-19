package com.blue.car.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;
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

    @Bind(R.id.current_speed)
    TextView currentSpeedView;

    private TextView averageTv;
    private TextView perMeterTv;
    private TextView perRunTimeTv;
    private TextView restRideMeterTv;
    private TextView totalMeterTextTv;
    private TextView temperatureTextTv;
    private TextView batteryPercentTv;

    private CommandRespManager respManager = new CommandRespManager();

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
        currentSpeedView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/zaozigongfang.otf"));
    }

    private void initInfoLayout() {
        initNormalInfoLayout(R.id.info_rl, "信息", R.mipmap.gengduo);
        initNormalInfoLayout(R.id.setting_rl, "设置", R.mipmap.gengduo);
        averageTv = initNormalInfoLayout(R.id.average_speed, "平均速度", "0.0km/h");
        perMeterTv = initNormalInfoLayout(R.id.per_meter, "本次里程", "0.0km");
        perRunTimeTv = initNormalInfoLayout(R.id.per_runTime, "本次行驶时间", "5min");
        restRideMeterTv = initNormalInfoLayout(R.id.rest_ride_meter, "剩余行驶里程", "3.2km");
        totalMeterTextTv = initNormalInfoLayout(R.id.total_meter, "总里程", "23.2km");
        temperatureTextTv = initNormalInfoLayout(R.id.temperature, "温度", "14℃");
        batteryPercentTv = initNormalInfoLayout(R.id.battery_percent, "剩余电量百分比", "46%");
    }

    private TextView initNormalInfoLayout(int parentId, String leftText, String rightText) {
        return (TextView) UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightText);
    }

    private void initNormalInfoLayout(int parentId, String leftText, int rightImageResId) {
        UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightImageResId);
    }

    @Override
    protected void initData() {
        isSpeedControl = getIntent().getIntExtra("isLimit", 0);
        invalidateSpeedLimitView(isSpeedControl);
        startMainFuncCommand();
    }

    private void startMainFuncCommand() {
        byte[] command = CommandManager.getMainFuncCommand();
        respManager.setCommandRespCallBack(BlueUtils.bytesToAscii(command), mainCommandCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback mainCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                MainFuncCommandResp mainFuncResp = CommandManager.getMainFuncCommandResp(data);
                updateView(mainFuncResp);
            }
        }
    };

    private void updateView(MainFuncCommandResp resp) {
        if (resp == null) {
            return;
        }
        currentSpeedView.setText(StringUtils.dealSpeedFormat(resp.speed));
        batteryPercentTv.setText(String.valueOf(resp.remainBatteryPercent * 1.0f / 100));
        averageTv.setText(String.valueOf(resp.averageSpeed));
        perMeterTv.setText(StringUtils.dealMileFormat(resp.perMileage));
        totalMeterTextTv.setText(StringUtils.dealMileFormat(resp.totalMileage));
        perRunTimeTv.setText(StringUtils.getTime(resp.perRunTime));
        restRideMeterTv.setText(StringUtils.dealMileFormat(resp.getRemainMileage()));
        temperatureTextTv.setText(StringUtils.dealTempFormat(resp.temperature));
    }

    private void processSpeedLimitClick() {
        invalidateSpeedLimitView(isSpeedControl = (isSpeedControl + 1) % 2);
    }

    private void invalidateSpeedLimitView(int isSpeedControl) {
        speedLimit.setBackgroundResource(isSpeedControl == 0 ? R.mipmap.xiansu_off : R.mipmap.xiansu_on);
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
            case R.id.search_btn:
                startMainFuncCommand();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startRegisterEventBus();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRegisterEventBus();
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
