package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;

import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.BatteryInfoCommandResp;
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

public class TestActivity extends BaseActivity {
    private static final String TAG = TestActivity.class.getSimpleName();

    private CommandRespManager respManager = new CommandRespManager();

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
            final byte[] dataBytes = CommandManager.unEncryptData(event.data);
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
       // startMainFuncCommand();
        Intent intent = new Intent(this,OtherSettingActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.battery_command_test_button)
    void onBatteryInfoOnClick() {
     //   startBatteryQueryCommand();
        Intent intent = new Intent(this,BatteryInfoActivity.class);
        startActivity(intent);
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
            }
        }
    };

//    private void writeCommand(byte[] command) {
//        BluetoothGattCharacteristic characteristic = getCommandWriteGattCharacteristic(AppApplication.getBluetoothLeService());
//        characteristic.setValue(command);
//        AppApplication.getBluetoothLeService().getBluetoothGatt().writeCharacteristic(characteristic);
//    }

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
}
