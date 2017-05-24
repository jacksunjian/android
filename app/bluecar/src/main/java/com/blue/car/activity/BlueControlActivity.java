package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.RemoteControlInfoCommandResp;
import com.blue.car.model.RemoteControlModeCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.utils.DigitalUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;
import com.blue.car.utils.UniversalViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/5.
 */

public class BlueControlActivity extends BaseActivity {
    private static final String TAG = BlueControlActivity.class.getSimpleName();
    private static final long SEND_MOVE_COMMAND_INTERVAL = 180;
    private static final int CONTROL_MODE_DELAY = 250;

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;

    @Bind(R.id.speed_textView)
    TextView speedTextView;
    @Bind(R.id.speed_unit)
    TextView speedUnitTextView;
    @Bind(R.id.battery_remain_percent)
    TextView batteryTextView;
    SeekBar speedLimitSeekBar;

    @Bind(R.id.remote_control_view)
    RotationImageView controlView;

    private CommandRespManager respManager = new CommandRespManager();
    private String speedLimitCommand;
    private String remoteOpenCommand;
    private String remoteCloseCommand;

    private int speedLimitSeekBarOffset = -2;
    private int speedLimit = 2;

    private long lastSendMoveCommandTime = System.currentTimeMillis();

    private Handler controlModeHandler = new Handler();


    @Override
    protected int getLayoutId() {
        return R.layout.activity_blue_control;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("蓝牙控制");
        speedUnitTextView.setText(AppApplication.instance().getUnitWithTime());
        speedTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/zaozigongfang.otf"));
        controlView.setRotationDiff(90);
        controlView.setRotationSelectListener(new RotationImageView.OnRotationSelectListener() {
            @Override
            public void OnRotation(float rotation) {
            }

            @Override
            public void OnRotationUp(float rotation) {
                controlView.resetToOriginalRotation();
            }
        });
        controlView.setOnMoveScaleChangedListener(new RotationImageView.OnMoveScaleChangedListener() {
            @Override
            public void onScaleChanged(float xScale, float yScale) {
                if (BluetoothConstant.USE_DEBUG) {
                    LogUtils.e(TAG, String.format("xScale:%.1f,yScale:%.1f", xScale, yScale));
                }
                startRemoteControlMoveCommand(yScale, xScale * -1);
            }

            @Override
            public void onLongScaleChanged(float xScale, float yScale) {
            }
        });
        initSpeedLimitView();
    }

    private TextView seekBarTextView;
    private int speedLimitOffset = 0;

    private void initSpeedLimitView() {
        String leftText = String.format("最大速度(%s)", AppApplication.instance().getUnitWithTime());
        seekBarTextView = (TextView) UniversalViewUtils.initNormalSeekBarLayoutWithoutRightTextSet(this, R.id.remote_maxSpeed_layout, leftText,
                0, speedLimitSeekBarOffset, new SeekBar.OnSeekBarChangeListener() {
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
                        startSetRemoteSpeedLimit(seekBar.getProgress() - speedLimitSeekBarOffset);
                    }
                });
        UniversalViewUtils.getItemDividerView((ViewGroup) findViewById(R.id.remote_maxSpeed_layout)).setVisibility(View.VISIBLE);
        speedLimitSeekBar = UniversalViewUtils.getSeekBarView((ViewGroup) findViewById(R.id.remote_maxSpeed_layout));
        speedLimitSeekBar.setMax(5);
    }

    @Override
    protected void initData() {
        enterRemoteModeCommand();
    }

    private String getSpecialCommand(byte[] command) {
        return BlueUtils.bytesToAscii(CommandManager.getSpecialCommandBytes(command));
    }

    private void startSetRemoteSpeedLimit(int speedLimit) {
        byte[] command = CommandManager.getRemoteControlInfoSettingCommand(speedLimit);
        speedLimitCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void enterRemoteModeCommand() {
        byte[] command = CommandManager.getRemoteControlOpenCommand();
        remoteOpenCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void quitRemoteModeCommand() {
        byte[] command = CommandManager.getRemoteControlCloseCommand();
        remoteCloseCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void startRemoteControlModeCommand() {
        controlModeHandler.postDelayed(controlModeRunnable, CONTROL_MODE_DELAY);
    }

    private Runnable controlModeRunnable = new Runnable() {
        @Override
        public void run() {
            postRemoteControlModeCommand();
            startRemoteControlModeCommand();
        }
    };

    private void postRemoteControlModeCommand() {
        byte[] command = CommandManager.getRemoteControlModeCommand();
        respManager.addCommandRespCallBack(getSpecialCommand(command), remoteModeRespCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback remoteModeRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                RemoteControlModeCommandResp resp = CommandManager.getRemoteControlModeCommandResp(data);
                LogUtils.jsonLog("RemoteControlModeCommandResp", resp);
                updateControlModeView(resp);
            }
        }
    };

    private void startRemoteControlInfoCommand() {
        byte[] command = CommandManager.getRemoteControlInfoCommand();
        respManager.addCommandRespCallBack(getSpecialCommand(command), remoteInfoRespCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback remoteInfoRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                RemoteControlInfoCommandResp resp = CommandManager.getRemoteControlInfoCommandResp(data);
                LogUtils.jsonLog("RemoteControlInfoCommandResp", resp);
                updateSpeedLimitView(resp);
            }
        }
    };

    private void startRemoteControlMoveCommand(float xValue, float yValue) {
        if (System.currentTimeMillis() < lastSendMoveCommandTime + SEND_MOVE_COMMAND_INTERVAL) {
            return;
        }
        lastSendMoveCommandTime = System.currentTimeMillis();
        byte[] command = CommandManager.getRemoteControlMoveCommand((int) xValue * speedLimit * 500, (int) yValue * speedLimit * 1000);
        writeCommand(command);
    }

    private int getSpeedLimitSeekBarProgress() {
        int value = speedLimit + speedLimitSeekBarOffset;
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    private void updateSpeedLimitView(RemoteControlInfoCommandResp resp) {
        if (resp == null) {
            return;
        }
        speedLimit = (int) resp.blueCtrlSpeedLimit;
        speedLimitSeekBar.setProgress(getSpeedLimitSeekBarProgress());
    }

    private void updateControlModeView(RemoteControlModeCommandResp resp) {
        if (resp == null) {
            return;
        }
        if (resp.workMode != 0 && resp.workMode != 1) {
            showToast("要求当前模式为助力或者待机");
        }
        if (resp.isRemoteConditionStatus() && (resp.isPickingUpStatus() || resp.isStandingManStatus())) {
            showToast("遥控模式，不可站人和拎起");
        }
        speedTextView.setText(String.format("%.1f", AppApplication.instance().getResultByUnit(resp.speed)));
        batteryTextView.setText(String.format(getString(R.string.battery_remain_format), resp.batteryRemainPercent));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        final byte[] dataBytes = printGattCharacteristicReadEvent(event);
        if (dataBytes != null) {
            byte[] result = respManager.obtainData(dataBytes);
            processReadEvent(result);
        }
    }

    private void processReadEvent(byte[] dataBytes) {
        if (dataBytes == null || dataBytes.length <= 0) {
            return;
        }
        respManager.processCommandResp(getSpecialCommand(dataBytes), dataBytes);
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
        if (command.equals(speedLimitCommand)) {
            showToast("限速设置成功");
            setSuccessSpeedLimit();
        } else if (command.equals(remoteOpenCommand)) {
            showToast("已成功进入遥控模式");
            afterEnterRemoteMode();
        } else if (command.equals(remoteCloseCommand)) {
            afterQuitRemoteMode();
        }
    }

    private void setSuccessSpeedLimit() {
        speedLimit = speedLimitSeekBar.getProgress() - speedLimitSeekBarOffset;
    }

    private void afterEnterRemoteMode() {
        startRemoteControlInfoCommand();
        startRemoteControlModeCommand();
    }

    private void afterQuitRemoteMode() {
        finish();
    }

    @OnClick({R.id.lh_btn_back, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                quitRemoteModeCommand();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            quitRemoteModeCommand();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
    protected void onDestroy() {
        try {
            controlModeHandler.removeCallbacks(controlModeRunnable);
        } catch (Exception e) {
        }
        super.onDestroy();
    }
}
