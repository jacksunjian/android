package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.SensitivityCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;
import com.blue.car.utils.UniversalViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

public class SensorSettingActivity extends BaseActivity {


    @Bind(R.id.lh_tv_title)
    TextView actionBarTitle;
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    private static final String TAG = "SensorSettingActivity";
    Switch turningSwitch, ridingSwitch;
    SeekBar turningSeekBar, ridingSeekBar, balanceSeekBar;

    private CommandRespManager respManager = new CommandRespManager();

    SensitivityCommandResp sensitivityCommandResp;

    private String turnSensorSettingCommand;

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
                byte[] command;
                if (isChecked) {
                    command = CommandManager.getOpenTurnSensitivityCommand();
                } else {
                    command = CommandManager.getCloseTurnSensitivityCommand();
                }
                writeCommand(command);

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
                writeTurnSensonrCommand(seekBar.getProgress());

            }
        });
        Switch ridingSwitchView = (Switch) UniversalViewUtils.initNormalSwitchLayout(this, R.id.riding_sensitivity_auto_regulation,
                "骑行灵敏度自动调节");
        ridingSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtils.e("ridingSensitivity", "checked:" + isChecked);
                byte[] ridingCommand;
                if (isChecked) {
                    ridingCommand = CommandManager.getOpenRidingSensitivityCommand();
                } else {
                    ridingCommand = CommandManager.getCloseRidingSensitivityCommand();
                }
                writeCommand(ridingCommand);
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
                writeRideSensonrCommand(seekBar.getProgress());
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

        ViewGroup turningViewGroup = (ViewGroup) findViewById(R.id.turning_sensitivity_auto_regulation);
        turningSwitch = UniversalViewUtils.getSwitchView(turningViewGroup);


        ViewGroup ridingViewGroup = (ViewGroup) findViewById(R.id.riding_sensitivity_auto_regulation);
        ridingSwitch = UniversalViewUtils.getSwitchView(ridingViewGroup);

        ViewGroup turningseekBarViewGroup = (ViewGroup) findViewById(R.id.turning_sensitivity);
        turningSeekBar = UniversalViewUtils.getSeekBarView(turningseekBarViewGroup);

        ViewGroup ridingseekBarViewGroup = (ViewGroup) findViewById(R.id.riding_sensitivity);
        ridingSeekBar = UniversalViewUtils.getSeekBarView(ridingseekBarViewGroup);

        ViewGroup balanceseekBarViewGroup = (ViewGroup) findViewById(R.id.power_balance);
        balanceSeekBar = UniversalViewUtils.getSeekBarView(balanceseekBarViewGroup);


    }

    private void writeRideSensonrCommand(int progress) {
        byte[] command = CommandManager.setRidingSensitivityCommand(progress);
        writeCommand(command);
    }

    private void writeTurnSensonrCommand(int progress) {
        byte[] command = CommandManager.setTurnSensitivityCommand(progress);
        writeCommand(command);
//        speedLimitSettingCommand = new String(command);
//        speedLimitResp.speedLimit = value;
    }


    @Override
    protected void initData() {
        getSensorInfo();
    }

    private void getSensorInfo() {
        byte[] command = CommandManager.getSensitivityCommand();
        respManager.setCommandRespCallBack(new String(command), sensorInfoRespCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback sensorInfoRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                sensitivityCommandResp = CommandManager.getSensitivityCommandResp(data);
                LogUtils.jsonLog("speedLimitResp", sensitivityCommandResp);
                // saveSensitivityResp();
                updateView(sensitivityCommandResp);
            }
        }
    };


    private void updateView(SensitivityCommandResp resp) {
        if (resp == null) {
            return;
        }
        turningSwitch.setChecked(resp.isAutoTurningSensityAdjust());
        turningSeekBar.setEnabled(resp.isEnableTurningSeekBar());
        turningSeekBar.setProgress(resp.turningSensitivity);

        ridingSwitch.setChecked(resp.isAutoRidingSensityAdjust());
        ridingSeekBar.setEnabled(resp.isEnableRidingSeekBar());
        ridingSeekBar.setProgress(resp.ridingSensitivity);

        balanceSeekBar.setProgress(resp.balanceInPowerMode);

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
        if (dataBytes.equals(CommandManager.getOpenTurnSensitivityCommand())) {
            turningSeekBar.setEnabled(false);
        } else if (dataBytes.equals(CommandManager.getCloseTurnSensitivityCommand())) {
            turningSeekBar.setEnabled(true);
        } else if (dataBytes.equals(CommandManager.getOpenRidingSensitivityCommand())) {
            ridingSeekBar.setEnabled(false);
        } else if (dataBytes.equals(CommandManager.getCloseRidingSensitivityCommand())) {
            ridingSeekBar.setEnabled(true);
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
