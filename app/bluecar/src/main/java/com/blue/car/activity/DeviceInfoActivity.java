package com.blue.car.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/5.
 */

public class DeviceInfoActivity extends BaseActivity {
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.current_speed_tv)
    TextView currentSpeedTv;
    @Bind(R.id.average_speed_tv)
    TextView averageSpeedTv;
    @Bind(R.id.allmile_tv)
    TextView allmileTv;
    @Bind(R.id.this_mile_tv)
    TextView thisMileTv;
    @Bind(R.id.temperature_tv)
    TextView temperatureTv;
    @Bind(R.id.top_speed_tv)
    TextView topSpeedTv;
    @Bind(R.id.per_runTime)
    TextView perRunTime;
    private static final String TAG = DeviceInfoActivity.class.getSimpleName();

    private CommandRespManager respManager = new CommandRespManager();
    boolean stopThread = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_info;
    }

    @Override
    protected void initConfig() {


    }

    @Override
    protected void initView() {
        lhTvTitle.setText("设备信息");
    }

    @Override
    protected void initData() {
        new Thread(mRunnable).start();
        startDeviceInfoQueryCommand();
    }

    private void startDeviceInfoQueryCommand() {
        byte[] command = CommandManager.getMainFuncCommand();
        respManager.setCommandRespCallBack(new String(command), deviceInfoRespCallback);
        writeCommand(command);
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            startDeviceInfoQueryCommand();
        }
    };
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

    private CommandRespManager.OnDataCallback deviceInfoRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                MainFuncCommandResp resp = CommandManager.getMainFuncCommandResp(data);
                LogUtils.jsonLog("deviceInfoResp", resp);
                updateDeviceInfoView(resp);
            }
        }
    };


    @SuppressLint("SetTextI18n")
    private void updateDeviceInfoView(MainFuncCommandResp resp) {
        AppApplication app = AppApplication.instance();
        currentSpeedTv.setText(StringUtils.dealSpeedFormatWithoutTime(app.getResultByUnit(resp.speed)) +
                app.getUnitWithTime());
        averageSpeedTv.setText(StringUtils.dealSpeedFormatWithoutTime(app.getResultByUnit(resp.averageSpeed)) +
                app.getUnitWithTime());
        topSpeedTv.setText(StringUtils.dealSpeedFormatWithoutTime(app.getResultByUnit(resp.speedLimit)) +
                app.getUnitWithTime());
        allmileTv.setText(StringUtils.dealMileFormatWithoutUnit(app.getResultByUnit(resp.totalMileage)) +
                app.getPerMeterUnit());
        thisMileTv.setText(StringUtils.dealMileFormatWithoutUnit(app.getResultByUnit(resp.perMileage)) +
                app.getPerMeterUnit());
        perRunTime.setText(StringUtils.getHour(resp.perRunTime));
        temperatureTv.setText(StringUtils.dealTempFormatWithoutUnit(app.getTemperByUnit(resp.temperature)) +
                app.getTemperUnit());
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
}
