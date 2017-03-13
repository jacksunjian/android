package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.blue.car.R;
import com.blue.car.custom.SpeedMainView;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.BatteryInfoCommandResp;
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;

import butterknife.Bind;
import butterknife.OnClick;

public class TestActivity extends BaseActivity {
    private static final String TAG = TestActivity.class.getSimpleName();

    @Bind(R.id.speed_view)
    SpeedMainView speedView;

    private CommandRespManager respManager = new CommandRespManager();
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        testSpeedView();
    }

    private Random random = new Random();

    private Runnable testSpeedViewRunnable = new Runnable() {
        @Override
        public void run() {
            float value = random.nextInt(30);
            speedView.setSpeed(value);
            speedView.setBatteryProgress(value * 133.33f);
            testSpeedView();
        }
    };

    private void testSpeedView() {
        handler.postDelayed(testSpeedViewRunnable, 1000);
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
        }
    }

    @OnClick(R.id.main_func_command_test_button)
    void onMainFucButtonOnClick() {
        startMainFuncCommand();
        handler.removeCallbacks(testSpeedViewRunnable);
    }

    @OnClick(R.id.battery_command_test_button)
    void onBatteryInfoOnClick() {
        startBatteryQueryCommand();
    }

    private void startMainFuncCommand() {
        byte[] command = CommandManager.getMainFuncCommand();
        respManager.setCommandRespCallBack(new String(command), mainFuncRespCallback);
        writeCommand(command);
    }

    private void startBatteryQueryCommand() {
        byte[] command = CommandManager.getBatteryInfoCommand();
        respManager.setCommandRespCallBack(new String(command), batteryInfoRespCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback mainFuncRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                MainFuncCommandResp resp = CommandManager.getMainFuncCommandResp(data);
                LogUtils.jsonLog("mainFuncResp", resp);
                updateSpeedMainView(resp);
            }
        }
    };

    private CommandRespManager.OnDataCallback batteryInfoRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                BatteryInfoCommandResp resp = CommandManager.getBatteryInfoCommandResp(data);
                LogUtils.jsonLog("batteryResp", resp);
                updateBatteryView(resp);
            }
        }
    };

    private void updateSpeedMainView(MainFuncCommandResp resp) {
        if (resp == null) {
            return;
        }
        speedView.setBatteryPercent(resp.remainBatteryPercent * 1.0f / 100);
        speedView.setSpeed(resp.speed);
        speedView.setPerMileage(resp.perMileage);
    }

    private void updateBatteryView(BatteryInfoCommandResp resp) {
        if (resp == null) {
            return;
        }
        speedView.setBatteryPercent(resp.remainPercent * 1.0f / 100);
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
        super.onDestroy();
        handler.removeCallbacks(testSpeedViewRunnable);
    }
}
