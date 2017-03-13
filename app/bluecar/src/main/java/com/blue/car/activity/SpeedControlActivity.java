package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.manager.PreferenceManager;
import com.blue.car.model.SpeedLimitResp;
import com.blue.car.service.BlueUtils;
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

    private void initSpeedLimitSeekBar() {
        UniversalViewUtils.initNormalSeekBarLayout(this, R.id.speedg_control, "限速模式限速值", 0, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                writeSpeedLimitCommand(seekBar.getProgress());
            }
        });
        ViewGroup seekBarViewGroup = (ViewGroup) findViewById(R.id.speedg_control);
        speedLimitSeekBar = UniversalViewUtils.getSeekBarView(seekBarViewGroup);
        if (speedLimitResp != null) {
            speedLimitSeekBar.setMax(30);
            speedLimitSeekBar.setProgress(speedLimitResp.speedLimit);
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

    private void updateView(SpeedLimitResp resp) {
        if (resp == null) {
            return;
        }
        speedLimitSeekBar.setProgress(resp.speedLimit);
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
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            final byte[] dataBytes = CommandManager.unEncryptData(event.data);
            LogUtils.e("onCharacteristicRead", "status:" + event.status);
            LogUtils.e(TAG, "onCharRead "
                    + " read "
                    + event.uuid.toString()
                    + " -> "
                    + BlueUtils.bytesToHexString(dataBytes));
            byte[] result = respManager.obtainData(dataBytes);
            respManager.processCommandResp(result);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicWriteEvent(GattCharacteristicWriteEvent event) {
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
           // final byte[] dataBytes = CommandManager.unEncryptData(event.data);
            final byte[] dataBytes = event.data;
            LogUtils.e("onCharacteristicWrite", "status:" + event.status);
            LogUtils.e(TAG, "onCharWrite "
                    + " write "
                    + event.uuid.toString()
                    + " -> "
                    + BlueUtils.bytesToHexString(dataBytes));
            processWriteEvent(dataBytes);
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
