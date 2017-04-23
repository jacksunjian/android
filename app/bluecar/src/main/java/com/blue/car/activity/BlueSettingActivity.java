package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.AccountInfo;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;

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

    byte[] nameBytes;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.secret_tv)
    TextView secretTv;

    private CommandRespManager respManager = new CommandRespManager();
    private String settingPasswordCommand, settingNameCommand;

    private boolean needFinishActivity = false;
    String name="",password="";

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
    }

    @Override
    protected void initData() {
    }


    @OnClick({R.id.lh_btn_back, R.id.ll_back, R.id.ll_right, R.id.name_rl, R.id.password_rl})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.name_rl:
                showname();
                break;
            case R.id.password_rl:
                showpassword();
                break;
        }
    }

    private void showpassword() {
        new MaterialDialog.Builder(this)
                .title("蓝牙密码")
                .content("password")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("请输入六位数字密码", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().length() != 6) {
                            showToast("密码需为6位数字");
                            return;
                        }
                        writeSettingPasswordCommand(input.toString());
                        password=input.toString();
                    }
                }).show();


    }

    private void showname() {
        new MaterialDialog.Builder(this)
                .title("蓝牙名称")
                .content("name")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入名称", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (StringUtils.isNotBlank(input.toString())) {
                            if (input.toString().length() <= 10) {
                                writeSettingNameCommand(input.toString());
                                name=input.toString();
                            }else{
                                showToast("请输入小于十个字数");
                            }
                        }
                    }
                })
                .show();
    }

    public static byte[] getBytesByUTF8CharsetName(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
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
    }

    private void saveAccountName() {
        AccountInfo accountInfo = AccountInfo.currentAccountInfo(this);
        accountInfo.bleName = name;
        AccountInfo.saveAccountInfo(this, accountInfo);
    }

    private void saveAccountPassword() {
        AccountInfo accountInfo = AccountInfo.currentAccountInfo(this);
        accountInfo.blePassword = password;
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
