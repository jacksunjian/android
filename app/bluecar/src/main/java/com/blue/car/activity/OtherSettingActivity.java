package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.LockConditionInfoCommandResp;
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
 * Created by Administrator on 2017/3/4.
 */

public class OtherSettingActivity extends BaseActivity {
    private static final String TAG = OtherSettingActivity.class.getSimpleName();

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.can_off_switch)
    Switch canOffSwitch;
    @Bind(R.id.can_warn_switch)
    Switch canWarnSwitch;
    @Bind(R.id.close_rl)
    RelativeLayout closeRl;
    @Bind(R.id.back_warn_switch)
    Switch backWarnSwitch;
    private Handler handler = new Handler();

    private CommandRespManager respManager = new CommandRespManager();
    private LockConditionInfoCommandResp lockCommandResp;
    int workMode;
    private MainFuncCommandResp mainFuncResp;
    private String closeCarCommamd;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_other_setting;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        lhTvTitle.setText("其他设置");
        initSwitchView();
    }

    private void initSwitchView() {
        canOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (lockCommandResp != null) {
                    lockCommandResp.setLockCanPowerOff(isChecked);
                    writeLockCanDoCommand(lockCommandResp.getAlarmStatus());
                }
            }
        });
        canWarnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (lockCommandResp != null) {
                    lockCommandResp.setLockNotWarn(isChecked);
                    writeLockCanDoCommand(lockCommandResp.getAlarmStatus());
                }
            }
        });

        backWarnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (lockCommandResp != null) {
                    lockCommandResp.setBackCanWrn(isChecked);
                    writeLockCanDoCommand(lockCommandResp.getAlarmStatus());
                }
            }
        });




    }

    @Override
    protected void initData() {
        getLockConditionInfo();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainFuncCommand();
            }
        }, 1000);
    }

    private void startMainFuncCommand() {
        byte[] command = CommandManager.getMainFuncCommand();
        respManager.setCommandRespCallBack(new String(command), mainCommandCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback mainCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                mainFuncResp = CommandManager.getMainFuncCommandResp(data);
                LogUtils.jsonLog("sunjianjian", mainFuncResp);
                workMode = mainFuncResp.workMode;
            }
        }
    };


    private void writeLockCanDoCommand(int status) {
        byte[] command = CommandManager.getLockConditionSettingCommand(status);
        writeCommand(command);
    }

    private void getLockConditionInfo() {
        byte[] command = CommandManager.getLockConditionCommand();
        respManager.setCommandRespCallBack(new String(command), lockInfoRespCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback lockInfoRespCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                lockCommandResp = CommandManager.getLockConditionCommandResp(data);
                LogUtils.jsonLog("batteryResp", lockCommandResp);
                updateView(lockCommandResp);
            }
        }
    };

    private void updateView(LockConditionInfoCommandResp resp) {
        Log.e("sunjian", "获取了");
        canOffSwitch.setChecked(resp.isLockCanOff());
        canWarnSwitch.setChecked(resp.isLockNotWarn());
        backWarnSwitch.setChecked(resp.isbackCanWrn());
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
            processWriteEvent(event.data);
        }
    }

    private void processWriteEvent(byte[] dataBytes) {
        String command = BlueUtils.bytesToAscii(dataBytes);
        if (command.equals(closeCarCommamd)) {
            showGoToSearchDialog();
        }
    }


    private void showGoToSearchDialog() {
        new MaterialDialog.Builder(this)
                .content("需要进入重新搜索吗？")
                .positiveText("确定")
                .negativeText("取消")
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Intent intent = new Intent(OtherSettingActivity.this, SearchActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).show();
    }

    @OnClick({R.id.lh_btn_back, R.id.ll_back, R.id.close_rl})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:

            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.close_rl:
                // 0待机，1助力，2骑行，3锁车，4遥控
                showToast("" + workMode);
                if (workMode == 2) {
                    showToast("关机请先下车");
                } else {
                    closeCarCommamd();
                }
                break;
        }
    }

    private void closeCarCommamd() {
        byte[] command = CommandManager.closeCar();
        closeCarCommamd = BlueUtils.bytesToAscii(command);
        writeCommand(command);
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
