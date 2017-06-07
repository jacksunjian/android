package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.AccountInfo;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.ActivityUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
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

    private EditText firstPwdEdit, repeatPwdEdit;

    private CommandRespManager respManager = new CommandRespManager();
    private String settingPasswordCommand, settingNameCommand;

    String name = "", password = "";

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
                showRenameDialog();
                break;
            case R.id.password_rl:
                showResetPassword();
                break;
        }
    }

    private void showResetPassword() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("密码设置")
                .autoDismiss(false)
                .customView(R.layout.dialog_custom_multi_edit, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(Color.GRAY)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        boolean correct = checkInputPasswordCorrect();
                        if (correct) {
                            dialog.dismiss();
                        }
                    }
                })
                .show();
        firstPwdEdit = (EditText) dialog.getCustomView().findViewById(R.id.first_pwd);
        repeatPwdEdit = (EditText) dialog.getCustomView().findViewById(R.id.repeat_pwd);
    }

    private boolean checkInputPasswordCorrect() {
        String firstPwd = firstPwdEdit.getText().toString();
        String repeatPwd = repeatPwdEdit.getText().toString();
        if (firstPwd.length() != 6 || repeatPwd.length() != 6) {
            showToast("密码需为6位数字");
            return false;
        }
        if (!firstPwd.equals(repeatPwd)) {
            showToast("密码不一致");
            return false;
        }
        writeSettingPasswordCommand(password = repeatPwd);
        return true;
    }

    private void showRenameDialog() {
        AccountInfo account = AccountInfo.currentAccountInfo(this);
        String defaultName = null;
        if (account != null) {
            defaultName = account.bleName;
        }
        if(StringUtils.isNullOrEmpty(defaultName)) {
            defaultName = "请输入名称";
        }
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("蓝牙名称设置")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(Color.GRAY)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(defaultName, "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (StringUtils.isNotBlank(input.toString())) {
                            if (input.toString().length() <= 10) {
                                writeSettingNameCommand(name = input.toString());
                            } else {
                                showToast("请输入小于十个字数");
                            }
                        }
                    }
                });
        builder.show();
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
            showToast("设置密码成功，平衡车即将重启");
            startToResearch();
        } else if (command.equals(settingNameCommand)) {
            showToast("设置名字成功，平衡车即将重启");
            startToResearch();
        }
    }

    private void startToResearch() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ActivityUtils.startActivityWithClearTask(BlueSettingActivity.this, SearchActivity.class);
            }
        }, 1500);
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
