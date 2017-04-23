package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    @Bind(R.id.power_tv)
    TextView powerTv;
    public float power;

    private CommandRespManager respManager = new CommandRespManager();
    boolean stopThread = false;
    //    private Handler mHandler = new Handler();
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

        new Thread(mRunnable).start();
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            startBatteryQueryCommand();
        }
    };


    private void startBatteryQueryCommand() {
        byte[] command = CommandManager.getBatteryInfoCommand();
        respManager.setCommandRespCallBack(new String(command), batteryInfoRespCallback);
        writeCommand(command);
    }

    Runnable mRunnable = new Runnable() {
        public void run() {
            while (!stopThread) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                mHandler.sendMessage(mHandler.obtainMessage());
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


    private void updateBatteryView(BatteryInfoCommandResp resp) {

        restBatteryTv.setText(resp.remainBatteryElectricity + "mAh");
        restPercentTv.setText(resp.remainPercent + "%");
        electricTv.setText(String.format("%.2fA",resp.electricCurrent));
        voltageTv.setText( String.format("%.2fV",resp.voltage));
        temperatureTv.setText("" + resp.temperature + "℃");
        power =(resp.electricCurrent)*(resp.voltage);
        Log.e("power",""+power);
        powerTv.setText(String.format("%.2fW",power));

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

    @Override
    protected void onDestroy() {
        stopThread = true;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
