package com.blue.car.activity;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.LedCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LinearGradientUtil;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/5.
 */
public class LightSettingActivity extends BaseActivity {

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;

    @Bind(R.id.color_control_view)
    RotationImageView colorControlView;

    @Bind(R.id.color_select_view)
    View colorSelectView;

    private float rotation = 0;

    private CommandRespManager respManager = new CommandRespManager();
    private String ledColorCommand;
    private String ambientLightCommand;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_light_setting;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("灯光设置");
        colorControlView.setRotationSelectListener(new RotationImageView.OnRotationSelectListener() {
            @Override
            public void OnRotation(float ro) {
                colorSelectView.setBackgroundColor(getColor(rotation = ro));
            }
        });
    }

    @Override
    protected void initData() {
        startLedQueryCommand();
    }

    private int getColor(float rotation) {
        int startColor = 0, endColor = 0;
        float tmp = rotation % 360;
        if (tmp <= 60) {
            startColor = Color.RED;
            endColor = Color.YELLOW;
        } else if (tmp <= 120) {
            startColor = Color.YELLOW;
            endColor = Color.GREEN;
        } else if (tmp <= 180) {
            startColor = Color.GREEN;
            endColor = Color.CYAN;
        } else if (tmp < 240) {
            startColor = Color.CYAN;
            endColor = Color.BLUE;
        } else if (tmp <= 300) {
            startColor = Color.BLUE;
            endColor = Color.MAGENTA;
        } else if (tmp <= 360) {
            startColor = Color.MAGENTA;
            endColor = Color.RED;
        }
        float radio = tmp % 60 / 60;
        return LinearGradientUtil.getColor(startColor, endColor, radio);
    }

    // return: actualColor and formatColor
    private int[] getActualColor(float rotation) {
        //纯红0，纯绿80，纯蓝160
        int startColor = 0, endColor = 0;
        float tmp = 360 - rotation % 360;
        if (tmp <= 120) {
            startColor = Color.RED;
            endColor = Color.BLUE;
        } else if (tmp <= 240) {
            startColor = Color.BLUE;
            endColor = Color.GREEN;
        } else if (tmp <= 360) {
            startColor = Color.GREEN;
            endColor = Color.RED;
        }
        int actualColor = LinearGradientUtil.getColor(startColor, endColor, tmp % 60 / 60);
        return new int[]{actualColor, (int) (tmp / 360 * 240)};
    }

    private void startLedQueryCommand() {
        byte[] ledCommand = CommandManager.getLedCommand();
        respManager.setCommandRespCallBack(BlueUtils.bytesToAscii(ledCommand), ledCommandCallback);
        writeCommand(ledCommand);
    }

    private CommandRespManager.OnDataCallback ledCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                LedCommandResp ledCommandResp = CommandManager.getLedCommandResp(data);
                LogUtils.jsonLog(getClass().getSimpleName(), ledCommandResp);
            }
        }
    };

    private void updateView(LedCommandResp resp) {

    }

    private void setLedColor() {
        int color = getActualColor(rotation)[1];
        byte[] command = CommandManager.getLedColorSettingCommand(color);
        ledColorCommand = BlueUtils.bytesToAscii(command);
        respManager.setCommandRespCallBack(ledColorCommand, null);
        writeCommand(command);
    }

    private void setAmbientLightMode(int mode) {
        byte[] command = CommandManager.getAmbientLightSettingCommand(mode);
        ambientLightCommand = BlueUtils.bytesToAscii(command);
        respManager.setCommandRespCallBack(ambientLightCommand, null);
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
        processWriteEvent(event.data);
    }

    private void processWriteEvent(byte[] dataBytes) {
        if (dataBytes == null || dataBytes.length <= 0) {
            return;
        }
        String command = BlueUtils.bytesToAscii(dataBytes);
        if (command.equals(ledColorCommand)) {
            showToast("颜色设置成功");
        } else if (command.equals(ambientLightCommand)) {
            showToast("氛围灯设置成功");
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

    @OnClick({R.id.lh_btn_back, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
        }
    }
}
