package com.blue.car.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import com.blue.car.utils.UniversalViewUtils;

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
    private String frontLedCommand;
    private String brakeLedCommand;
    private int ambientLightModeCount = 10;
    private int ambientLightMode = 0;

    private View settingLayout;
    private ValueAnimator settingAnimator;
    private boolean windowShow = false;
    private float[] marginRange;
    private float marginBottom;

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
                setLedColor();
            }
        });
        initLightSettingLayout();
    }

    private Switch frontLightSwitch;
    private Switch brakeLightSwitch;

    private void initLightSettingLayout() {
        UniversalViewUtils.initNormalInfoLayout(this, R.id.ambient_light_layout, "氛围灯模式", R.mipmap.gengduo);
        frontLightSwitch = (Switch) UniversalViewUtils.initNormalSwitchLayout(this, R.id.front_light_layout, "前灯开关");
        frontLightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFrontLedSwitchCommand();
            }
        });
        brakeLightSwitch = (Switch) UniversalViewUtils.initNormalSwitchLayout(this, R.id.brake_light_layout, "刹车开关");
        brakeLightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBrakeLedSwitchCommand();
            }
        });
        settingLayout = findViewById(R.id.setting_layout);
        settingLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) settingLayout.getLayoutParams();
                marginBottom = layoutParams.bottomMargin;
                settingLayout.removeOnLayoutChangeListener(this);
            }
        });
    }

    @Override
    protected void initData() {
        startLedQueryCommand();
    }

    private int getColor(float rotation) {
        return LinearGradientUtil.getColor(rotation);
    }

    // return: actualColor and formatColor
    private int[] getActualColor(float rotation) {
        return LinearGradientUtil.getActualColor(rotation);
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
                updateView(ledCommandResp);
            }
        }
    };

    private void updateView(LedCommandResp resp) {
        frontLightSwitch.setChecked(resp.isFrontLedOpen());
        brakeLightSwitch.setChecked(resp.isBrakeLedOpen());
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

    private void startFrontLedSwitchCommand() {
        byte[] command;
        if (frontLightSwitch.isChecked()) {
            command = CommandManager.getFrontLightOpenCommand();
        } else {
            command = CommandManager.getFrontLightCloseCommand();
        }
        frontLedCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void startBrakeLedSwitchCommand() {
        byte[] command;
        if (brakeLightSwitch.isChecked()) {
            command = CommandManager.getBrakesLightOpenCommand();
        } else {
            command = CommandManager.getBrakesLightCloseCommand();
        }
        brakeLedCommand = BlueUtils.bytesToAscii(command);
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
        } else if (command.equals(frontLedCommand)) {
            showToast("前灯设置成功");
        } else if (command.equals(brakeLedCommand)) {
            showToast("刹车灯设置成功");
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

    @OnClick(R.id.arrow_indicator)
    void onSettingIndicationClick() {
        startTranslateSettingLayout();
    }

    //关闭、单色呼吸、全彩呼吸、双龙戏珠、全彩分向、单色流星、炫彩流星、警灯模式1-3
    @OnClick(R.id.ambient_light_layout)
    void onAmbientLightClick() {
        setAmbientLightMode(++ambientLightMode % ambientLightModeCount);
    }

    private void startTranslateSettingLayout() {
        if (marginRange == null) {
            marginRange = new float[]{marginBottom, 0};
        }
        if (settingAnimator == null) {
            settingAnimator = new ValueAnimator();
            settingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            settingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) settingLayout.getLayoutParams();
                    layoutParams.bottomMargin = value.intValue();
                    settingLayout.setLayoutParams(layoutParams);
                }
            });
            settingAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ImageView slideView = (ImageView) findViewById(R.id.arrow_indicator);
                    windowShow = !windowShow;
                    slideView.setImageResource(windowShow ? R.mipmap.xiahua_btn : R.mipmap.shanghua_btn);
                    marginRange = windowShow ? new float[]{0, marginBottom} : new float[]{marginBottom, 0};
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        settingAnimator.setFloatValues(marginRange);
        settingAnimator.setDuration(500);
        settingAnimator.start();
    }
}
