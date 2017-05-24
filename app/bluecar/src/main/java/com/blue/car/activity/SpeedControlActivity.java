package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.manager.PreferenceManager;
import com.blue.car.model.SpeedLimitResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.DigitalUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;
import com.blue.car.utils.UniversalViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/4.
 */

public class SpeedControlActivity extends BaseActivity {
    private static final String TAG = SpeedControlActivity.class.getSimpleName();

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    SeekBar speedLimitSeekBar;

    private int speedLimitMax = 10;
    private int speedLimitOffset = -4;

    private CommandRespManager respManager = new CommandRespManager();
    private SpeedLimitResp speedLimitResp;
    private String speedLimitSettingCommand;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_speedcontrol;
    }

    @Override
    protected void initConfig() {
        String resp = PreferenceManager.getSpeedLimitResp(this);
        if (StringUtils.isNotBlank(resp)) {
            speedLimitResp = JSON.parseObject(resp, SpeedLimitResp.class);
        }
    }

    @Override
    protected void initView() {
        lhTvTitle.setText("车速设置");
        initSpeedLimitSeekBar();
    }

    private TextView seekBarTextView;

    private void initSpeedLimitSeekBar() {
        seekBarTextView = (TextView) UniversalViewUtils.initNormalSeekBarLayoutWithoutRightTextSet(this, R.id.speedg_control, "限速模式限速值",
                0, speedLimitOffset, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double result = DigitalUtils.round(AppApplication.instance().getResultByUnit(progress - speedLimitOffset), 1);
                seekBarTextView.setText(StringUtils.dealSpeedFormatWithoutTime((float) result));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                writeSpeedLimitCommand(seekBar.getProgress() - speedLimitOffset);

            }
        });
        speedLimitSeekBar = UniversalViewUtils.getSeekBarView((ViewGroup) findViewById(R.id.speedg_control));
        speedLimitSeekBar.setMax(speedLimitMax + speedLimitOffset);
        if (speedLimitResp != null) {
            speedLimitSeekBar.setProgress(getSpeedLimitSeekBarProgress(speedLimitResp.speedLimit) > speedLimitSeekBar.getMax() ?
                    getSpeedLimitSeekBarProgress(speedLimitResp.speedLimit) : speedLimitResp.speedLimit);
        }
    }

    @Override
    protected void initData() {
        getSpeedLimitInfo();
    }

    private void getSpeedLimitInfo() {
        byte[] command = CommandManager.getQueryLimitSpeedCommand();
        respManager.setCommandRespCallBack(new String(command), speedLimitRespCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback speedLimitRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                speedLimitResp = CommandManager.getSpeedLimitCommandResp(data);
                LogUtils.jsonLog("speedLimitResp", speedLimitResp);
                saveSpeedLimitResp();
                updateView(speedLimitResp);
            }
        }
    };

    private int getSpeedLimitSeekBarProgress(int speedLimit) {
        int value = speedLimit + speedLimitOffset;
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    private void updateView(SpeedLimitResp resp) {
        if (resp == null) {
            return;
        }
        speedLimitSeekBar.setProgress(getSpeedLimitSeekBarProgress(resp.speedLimit));
    }

    private void saveSpeedLimitResp() {
        if (speedLimitResp != null) {
            PreferenceManager.saveSpeedLimitResp(this, JSON.toJSONString(speedLimitResp));
        }
    }

    private void writeSpeedLimitCommand(int value) {
        byte[] command = CommandManager.getLimitSpeedSettingCommand(value);
        writeCommand(command);
        speedLimitSettingCommand = new String(command);
        speedLimitResp.speedLimit = value;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        byte[] dataBytes = printGattCharacteristicReadEvent(event);
        if (dataBytes != null) {
            byte[] result = respManager.obtainData(dataBytes);
            respManager.processCommandResp(result);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicWriteEvent(GattCharacteristicWriteEvent event) {
        printGattCharacteristicWriteEvent(event);
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            processWriteEvent(event.data);
        }
    }

    private void processWriteEvent(byte[] dataBytes) {
        if (dataBytes == null || dataBytes.length <= 0) {
            return;
        }
        if (new String(dataBytes).equals(speedLimitSettingCommand)) {
            showToast("骑行限速值设置成功");
            getSpeedLimitInfo();
            saveSpeedLimitResp();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRegisterEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRegisterEventBus();
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
