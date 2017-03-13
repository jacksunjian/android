package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.LockConditionInfoCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
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

    private CommandRespManager respManager = new CommandRespManager();
    private LockConditionInfoCommandResp lockCommandResp;

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
    }

    @Override
    protected void initData() {
        getLockConditionInfo();
    }

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
