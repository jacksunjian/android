package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.MainFuncCommandResp;
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
    TextView balanceText;
    int workMode;
    private MainFuncCommandResp mainFuncResp;
    private CommandRespManager respManager = new CommandRespManager();

    private SensitivityCommandResp sensitivityCommandResp;

    private String turnOnSensorSettingCommand, turnoffSensorSettingCommand,
            ridingOnSensorSettingCommand,
            ridingOffSensorSettingCommand;

    private int balanceProgressOffset = 20;
    private String lockCommand;
    private String unLockCommand;
    private String checkCommand;

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

    private void startMainFuncCommand() {
        byte[] command = CommandManager.getMainFuncCommand();
        respManager.setCommandRespCallBack(new String(command), mainCommandCallback);
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
                mainFuncResp = CommandManager.getMainFuncCommandResp(data);
                LogUtils.jsonLog("sunjianjian", mainFuncResp);
                workMode = mainFuncResp.workMode;
            }
        }
    };

    private void initActionBar() {
        findViewById(R.id.ll_back).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_right).setVisibility(View.GONE);
        actionBarTitle.setText("传感器设置");
    }

    private void initSettingView() {
        UniversalViewUtils.initNormalInfoLayout(this, R.id.posture_layout, "姿态校准", R.mipmap.gengduo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("sunjian",""+workMode);
//                if (workMode == 1) {
                    showCarLockDialog();
//                } else {
//                    showToast("当前不是助力模式，请下车");
//                }
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
                    turnOnSensorSettingCommand = BlueUtils.bytesToAscii(command);
                } else {
                    command = CommandManager.getCloseTurnSensitivityCommand();
                    turnoffSensorSettingCommand = BlueUtils.bytesToAscii(command);
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
                writeTurnSensorCommand(seekBar.getProgress());

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
                    ridingOnSensorSettingCommand = BlueUtils.bytesToAscii(ridingCommand);
                } else {
                    ridingCommand = CommandManager.getCloseRidingSensitivityCommand();
                    ridingOffSensorSettingCommand = BlueUtils.bytesToAscii(ridingCommand);
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
                writeRideSensorCommand(seekBar.getProgress());
            }
        });

        balanceText = (TextView) UniversalViewUtils.initNormalSeekBarLayout(this, R.id.power_balance, "助力平衡点", 20, balanceProgressOffset, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                writePowerBalanceCommand(seekBar.getProgress() - balanceProgressOffset);
            }
        });

        turningSwitch = UniversalViewUtils.getSwitchView((ViewGroup) findViewById(R.id.turning_sensitivity_auto_regulation));
        ridingSwitch = UniversalViewUtils.getSwitchView((ViewGroup) findViewById(R.id.riding_sensitivity_auto_regulation));

        turningSeekBar = UniversalViewUtils.getSeekBarView((ViewGroup) findViewById(R.id.turning_sensitivity));
        ridingSeekBar = UniversalViewUtils.getSeekBarView((ViewGroup) findViewById(R.id.riding_sensitivity));
        balanceSeekBar = UniversalViewUtils.getSeekBarView((ViewGroup) findViewById(R.id.power_balance));
        balanceSeekBar.setMax(balanceProgressOffset * 2);
    }

    private void writeRideSensorCommand(int progress) {
        byte[] command = CommandManager.setRidingSensitivityCommand(progress);
        writeCommand(command);
    }

    private void writeTurnSensorCommand(int progress) {
        byte[] command = CommandManager.setTurnSensitivityCommand(progress);
        writeCommand(command);
//        speedLimitSettingCommand = new String(command);
//        speedLimitResp.speedLimit = value;
    }

    private void writePowerBalanceCommand(int balance) {
        byte[] command = CommandManager.getPowerBalanceSettingCommand(balance);
        writeCommand(command);
    }

    @Override
    protected void initData() {
      //  startMainFuncCommand();
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

    private void showCarLockDialog() {
        new MaterialDialog.Builder(this)
                .content("标定传感器需要锁车，是否继续")
                .positiveText("同意")
                .negativeText("取消")
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startLockCommand();
                    }
                }).show();
    }
    private void startLockCommand() {
        byte[] command = CommandManager.getLockCarCommand();
        lockCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }


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

        balanceSeekBar.setProgress(resp.balanceInPowerMode + balanceProgressOffset);
        balanceText.setText(String.valueOf(resp.balanceInPowerMode));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        final byte[] dataBytes = printGattCharacteristicReadEvent(event);
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
        String command = BlueUtils.bytesToAscii(dataBytes);
        Log.e("sunjian-ridingSwitch", command);
        if (command.equals(turnOnSensorSettingCommand)) {
            //55 AA 04 0A 03 A1 65 00 E8 FE
            showToast("转向开启");
            turningSeekBar.setEnabled(false);
        } else if (command.equals(turnoffSensorSettingCommand)) {
            //55 AA 04 0A 03 A1 32 00 1B FF
            turningSeekBar.setEnabled(true);
            showToast("转向关闭");
            turningSeekBar.setProgress(50);
        } else if (command.equals(ridingOnSensorSettingCommand)) {
            //55 AA 04 0A 03 A2 65 00 E7 FE
            ridingSeekBar.setEnabled(false);
            showToast("骑行开启");
        } else if (command.equals(ridingOffSensorSettingCommand)) {
            //55 AA 04 0A 03 A2 32 00 1A FF
            ridingSeekBar.setEnabled(true);
            showToast("骑行关闭");
            ridingSeekBar.setProgress(50);
        }else  if (command.equals(lockCommand)) {
          showWarnCommandPop();
        }
        else if(command.equals(checkCommand)){
            startUnLockCommand();
        }
        else if (command.equals(unLockCommand)) {
            showToast("调整完毕，车子解锁");
        }
    }

    private void showWarnCommandPop() {
        new MaterialDialog.Builder(this)
                .content(R.string.warning)
                .positiveText("同意")
                .negativeText("取消")
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startCheckCommand();
                    }
                }).show();
    }

    private void startCheckCommand() {
        byte[] command = CommandManager.setCheckCommand();
        checkCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }
    private void startUnLockCommand() {
        byte[] command = CommandManager.getUnLockCarCommand();
        unLockCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
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
