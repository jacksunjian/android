package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.SpeedLimitResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;
import com.blue.car.utils.UniversalViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/4.
 */

public class SpeedControlActivity extends BaseActivity {
    private static final String TAG = SpeedControlActivity.class.getSimpleName();

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    SeekBar speedLimitSeekBar;

    private CommandRespManager respManager = new CommandRespManager();
    private SpeedLimitResp speedLimitResp;
    private String speedLimitSettingCommand;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_speedcontrol;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("车速设置");

        SharedPreferences sharedPreferences = getSharedPreferences("speedLimit", Context.MODE_PRIVATE);
        int speed = sharedPreferences.getInt("limit",  0  );
        UniversalViewUtils.initNormalSeekBarLayout(this, R.id.speedg_control, "限速模式限速值", speed, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = getSharedPreferences("speedLimit",MODE_PRIVATE).edit();
                editor.putInt("limit", seekBar.getProgress());
                editor.commit();

                writeSpeedLimitCommand(seekBar.getProgress()*1000);
            }
        });
        ViewGroup seekBarViewGroup = (ViewGroup) findViewById(R.id.speedg_control);
        speedLimitSeekBar = UniversalViewUtils.getSeekBarView(seekBarViewGroup);
    }

    @Override
    protected void initData() {
        getSpeedLimitInfo();
    }

    private void getSpeedLimitInfo() {
        byte[] command = CommandManager.getQueryLimitSpeedCommand();
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
                speedLimitResp = CommandManager.getSpeedLimitCommandResp(data);

                LogUtils.jsonLog("speedLimitResp", speedLimitResp);
                updateView(speedLimitResp);
            }
        }
    };

    private void updateView(SpeedLimitResp resp) {
        speedLimitSeekBar.setProgress(resp.speedLimit);
    }

    private void writeSpeedLimitCommand(int value) {
        byte[] command = CommandManager.getLimitSpeedSettingCommand(value);
        writeCommand(command);
        speedLimitSettingCommand = new String(command);
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
            processWriteEvent(dataBytes);
        }
    }

    private void processWriteEvent(byte[] dataBytes) {
        if (dataBytes == null || dataBytes.length <= 0) {
            return;
        }
        if (new String(dataBytes).equals(speedLimitSettingCommand)) {
            showToast("骑行限速值设置成功");
            getSpeedLimitInfo();
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
