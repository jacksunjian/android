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
import com.blue.car.model.AccountInfo;
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

public class BlueSettingActivity extends BaseActivity {
    private static final String TAG = BlueSettingActivity.class.getSimpleName();

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
    private String settingPasswordCommand, settingNameCommand;

    private boolean needFinishActivity = false;

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
                processNameEdit();
                processPasswordEdit();
                break;
        }
    }

    private String getNameString() {
        return nameEt.getText().toString();
    }

    private String getPasswordString() {
        return secretEt.getText().toString();
    }

    private void processNameEdit() {
        String name = getNameString();
        if (StringUtils.isNotBlank(name)) {
            writeSettingNameCommand(name);
            needFinishActivity = true;
        }
    }

    private void processPasswordEdit() {
        String password = getPasswordString();
        if (StringUtils.isNullOrEmpty(password)) {
            return;
        }
        if (password.length() < 6) {
            showToast("密码需要6位数字");
            needFinishActivity = false;
            return;
        }
        writeSettingPasswordCommand(password);
        needFinishActivity = true;
    }

    private void writeSettingPasswordCommand(String s) {
        byte[] command = CommandManager.getBlueCarPasswordSettingCommand(s);
        settingPasswordCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void writeSettingNameCommand(String s) {
        byte[] command = CommandManager.getBlueCarNameSettingCommand(s);
        settingNameCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        byte[] dataBytes = printGattCharacteristicReadEvent(event);
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
        if (command.equals(settingPasswordCommand)) {
            saveAccountPassword();
            showToast("设置密码成功");
        } else if (command.equals(settingNameCommand)) {
            saveAccountName();
            showToast("设置名称成功");
        }
        if (needFinishActivity) {
            finish();
        }
    }

    private void saveAccountName() {
        AccountInfo accountInfo = AccountInfo.currentAccountInfo(this);
        accountInfo.bleName = getNameString();
        AccountInfo.saveAccountInfo(this, accountInfo);
    }

    private void saveAccountPassword() {
        AccountInfo accountInfo = AccountInfo.currentAccountInfo(this);
        accountInfo.blePassword = getPasswordString();
        AccountInfo.saveAccountInfo(this, accountInfo);
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
