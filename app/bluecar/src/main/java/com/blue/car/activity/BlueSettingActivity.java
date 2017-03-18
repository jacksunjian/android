package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/4.
 */

public class BlueSettingActivity extends BaseActivity {
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.ll_right)
    LinearLayout llRight;
    @Bind(R.id.tv_right)
    TextView tvRight;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.name_et)
    EditText nameEt;
    @Bind(R.id.secret_et)
    EditText secretEt;
    private CommandRespManager respManager = new CommandRespManager();
    private static final String TAG = "BlueSettingActivity";

    private String settingPasswordCommand,settingNameCommand;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_blue_setting;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("蓝牙设置");
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("确定");
      //  getBlueCarNameSettingCommand
        //getBlueCarPasswordSettingCommand
    }

    @Override
    protected void initData() {

    }


    @OnClick({R.id.lh_btn_back, R.id.ll_back, R.id.ll_right, R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:

            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.ll_right:

            case R.id.tv_right:
                writeSettingNameCommand(nameEt.getText().toString());
                writeSettingPasswordCommand(secretEt.getText().toString());
                break;
        }
    }

    private void writeSettingPasswordCommand(String s) {
        byte[] command = CommandManager.getBlueCarPasswordSettingCommand(s);
        settingPasswordCommand = new String(command);
        writeCommand(command);
    }

    private void writeSettingNameCommand(String s) {
        byte[] command = CommandManager.getBlueCarNameSettingCommand(s);
        settingNameCommand = new String(command);
        writeCommand(command);
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
        if (dataBytes == null || dataBytes.length <= 0) {
            return;
        }
        if (new String(dataBytes).equals(settingPasswordCommand)) {
            showToast("设置密码成功");
        }else if(new String(dataBytes).equals(settingNameCommand))
        {
            showToast("设置名称成功");
        }
        finish();
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



}
