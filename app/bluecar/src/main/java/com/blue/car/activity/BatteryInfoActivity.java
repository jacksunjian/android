package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.BatteryInfoCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/4.
 */

public class BatteryInfoActivity extends BaseActivity {
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.rest_battery_tv)
    TextView restBatteryTv;
    @Bind(R.id.rest_percent_tv)
    TextView restPercentTv;
    @Bind(R.id.electric_tv)
    TextView electricTv;
    @Bind(R.id.voltage_tv)
    TextView voltageTv;
    @Bind(R.id.temperature_tv)
    TextView temperatureTv;
    private static final String TAG = BatteryInfoActivity.class.getSimpleName();

    private CommandRespManager respManager = new CommandRespManager();
    private Handler mHandler = new Handler();
    int k = 0;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_battery_info;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        lhTvTitle.setText("电池信息");
    }

    @Override
    protected void initData() {
        startBatteryQueryCommand();
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                startBatteryQueryCommand();
//                k++;
//                if (k < 20) {
//                    mHandler.postDelayed(this, 350);
//                }
//
//            }
//        });

    }

    private void startBatteryQueryCommand() {
        byte[] command = CommandManager.getBatteryInfoCommand();
        respManager.setCommandRespCallBack(new String(command), batteryInfoRespCallback);
        writeCommand(command);
    }


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


   private void updateBatteryView(BatteryInfoCommandResp resp){
       restBatteryTv.setText("" + resp.remainBatteryElectricity + "mAh");
       restPercentTv.setText("" + resp.remainPercent + "%");
       electricTv.setText("" + resp.electricCurrent + "A");
       voltageTv.setText("" + resp.voltage + "V");
       temperatureTv.setText("" + resp.temperature + "℃");

   }



//    private void writeCommand(byte[] command) {
//        BluetoothGattCharacteristic characteristic = getCommandWriteGattCharacteristic(AppApplication.getBluetoothLeService());
//        characteristic.setValue(command);
//        AppApplication.getBluetoothLeService().getBluetoothGatt().writeCharacteristic(characteristic);
//    }

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


    @OnClick({R.id.lh_btn_back, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:

            case R.id.ll_back:
                onBackPressed();
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
}
