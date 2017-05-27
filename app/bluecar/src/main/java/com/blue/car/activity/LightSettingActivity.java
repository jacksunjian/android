package com.blue.car.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.LedCommandResp;
import com.blue.car.model.LockConditionInfoCommandResp;
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

    @Bind(R.id.color1)
    ImageView color1View;
    @Bind(R.id.color2)
    ImageView color2View;
    @Bind(R.id.color3)
    ImageView color3View;
    @Bind(R.id.color4)
    ImageView color4View;

    private ImageView[] colorCollectionViews;
    private ShapeDrawable[] shapeDrawableCollection;

    private TextView ambientTextView;
    private Switch frontLightSwitch;
    private Switch brakeLightSwitch;

    private float rotation = 0;
    private int colorSelectIndex = 0;

    private CommandRespManager respManager = new CommandRespManager();
    private String ledColorCommand;
    private String ambientLightCommand;
    private String frontLedCommand;
    private String brakeLedCommand;
    private int ambientLightModeCount = 10;
    private int ambientLightMode = 0;

    private LockConditionInfoCommandResp lockCommandResp;
    private LedCommandResp ledCommandResp;

    private View settingLayout;
    private ValueAnimator settingAnimator;
    private boolean windowShow = false;
    private float[] marginRange;
    private float marginBottom;

    private String[] ambientModeStringArray;
    private Handler handler = new Handler();
    private Handler colorHandler = new Handler();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_light_setting;
    }

    @Override
    protected void initConfig() {
        ambientModeStringArray = getResources().getStringArray(R.array.ambient_mode_array);
        ambientLightModeCount = ambientModeStringArray.length;
    }

    @Override
    protected void initView() {
        lhTvTitle.setText("灯光设置");
        colorControlView.setConstantlyRotationSelect(true);
        colorControlView.setRotationSelectListener(new RotationImageView.OnRotationSelectListener() {
            @Override
            public void OnRotation(float ro) {
                rotation = ro;
                updateColorView();
                postColorSetCommand();
            }

            @Override
            public void OnRotationUp(float rotation) {
                setLedColor();
            }
        });
        initLightSettingLayout();
        initColorView();
    }

    private void initLightSettingLayout() {
        UniversalViewUtils.initNormalInfoLayout(this, R.id.ambient_light_layout, "氛围灯模式", R.mipmap.gengduo);
        ambientTextView = UniversalViewUtils.getRightTextView((ViewGroup) findViewById(R.id.ambient_light_layout));
        ambientTextView.setVisibility(View.VISIBLE);
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

    private ShapeDrawable getOvalShapeDrawable(int index) {
        return shapeDrawableCollection[index];
    }

    private ImageView getColorSelectImageView(int index) {
        return colorCollectionViews[index];
    }

    private ShapeDrawable getOvalShapeDrawable() {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(Color.RED);
        int size = getResources().getDimensionPixelSize(R.dimen.oval_shape_size);
        drawable.setBounds(0, 0, size, size);
        return drawable;
    }

    private void initColorView() {
        colorCollectionViews = new ImageView[4];
        colorCollectionViews[0] = color1View;
        colorCollectionViews[1] = color2View;
        colorCollectionViews[2] = color3View;
        colorCollectionViews[3] = color4View;

        shapeDrawableCollection = new ShapeDrawable[4];
        shapeDrawableCollection[0] = getOvalShapeDrawable();
        shapeDrawableCollection[1] = getOvalShapeDrawable();
        shapeDrawableCollection[2] = getOvalShapeDrawable();
        shapeDrawableCollection[3] = getOvalShapeDrawable();
        for (int i = 0; i < colorCollectionViews.length; i++) {
            colorCollectionViews[i].setBackground(shapeDrawableCollection[i]);
        }
    }

    private void updateColorView(int index, int color) {
        ImageView imageView = getColorSelectImageView(index);
        ShapeDrawable drawable = getOvalShapeDrawable();
        drawable.getPaint().setColor(color);
        imageView.setBackground(drawable);
    }

    private void updateColorView() {
        int color = getColor(rotation);
        updateColorView(colorSelectIndex, color);
    }

    @Override
    protected void initData() {
        startLedQueryCommand();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLedLockConditionCommand();
            }
        }, 1000);
    }

    private int getColor(float rotation) {
        return LinearGradientUtil.getColor(rotation);
    }

    // return: actualColor and formatColor
    private int[] getActualColor(float rotation) {
        return LinearGradientUtil.getActualColor(rotation);
    }

    private String getOnReadCommand(byte[] dataBytes) {
        return BlueUtils.bytesToAscii(CommandManager.getSpecialCommandBytes(dataBytes));
    }

    private void startLedQueryCommand() {
        byte[] ledCommand = CommandManager.getLedCommand();
        respManager.setCommandRespCallBack(getOnReadCommand(ledCommand), ledCommandCallback);
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
                ledCommandResp = CommandManager.getLedCommandResp(data);
                LogUtils.jsonLog(getClass().getSimpleName(), ledCommandResp);
                updateLedView(ledCommandResp);
            }
        }
    };

    private void startLedLockConditionCommand() {
        byte[] lockCommand = CommandManager.getLockConditionCommand();
        respManager.addCommandRespCallBack(getOnReadCommand(lockCommand), lockCommandCallback);
        writeCommand(lockCommand);
    }

    private CommandRespManager.OnDataCallback lockCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                lockCommandResp = CommandManager.getLockConditionCommandResp(data);
                LogUtils.jsonLog(getClass().getSimpleName(), lockCommandResp);
                updateSwitchView(lockCommandResp);
            }
        }
    };

    private void updateLedView(LedCommandResp resp) {
        if (resp == null) {
            return;
        }
        ambientLightMode = resp.ledMode;
        updateAmbientModeView(ambientLightMode);
        for (int i = 0; i < resp.ledColor.length; i++) {
            int hsb = LinearGradientUtil.hsbToColor(resp.ledColor[i])[0];
            Log.e("AA-hsb",""+i+":"+Integer.toHexString(hsb));

            updateColorView(i, hsb );
        }
    }

    private void updateSwitchView(LockConditionInfoCommandResp resp) {
        if (resp == null) {
            return;
        }
        frontLightSwitch.setChecked(resp.isFrontLedOn());
        brakeLightSwitch.setChecked(resp.isBrakeLedOn());
    }

    private void updateAmbientModeView(int mode) {
        ambientTextView.setText(ambientModeStringArray[mode % ambientLightModeCount]);
    }

    private void postColorSetCommand() {
        setLedColor();
    }

    private void setLedColor() {
        int color = getActualColor(rotation)[1];
        byte[] command = null;
        switch (colorSelectIndex) {
            case 0:
                command = CommandManager.getLed1ColorSettingCommand(color);
                break;
            case 1:
                command = CommandManager.getLed2ColorSettingCommand(color);
                break;
            case 2:
                command = CommandManager.getLed3ColorSettingCommand(color);
                break;
            case 3:
                command = CommandManager.getLed4ColorSettingCommand(color);
                break;
        }
        ledColorCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void setAmbientLightMode(int mode) {
        byte[] command = CommandManager.getAmbientLightSettingCommand(mode);
        ambientLightCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void startFrontLedSwitchCommand() {
        byte[] command;
        if (lockCommandResp == null) {
            lockCommandResp = new LockConditionInfoCommandResp();
        }
        lockCommandResp.setFrontLedEnable(frontLightSwitch.isChecked());
        command = CommandManager.getFrontBehindLightCommand(lockCommandResp.alarmStatus & 0x03);
        frontLedCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void startBrakeLedSwitchCommand() {
        byte[] command;
        if (lockCommandResp == null) {
            lockCommandResp = new LockConditionInfoCommandResp();
        }
        lockCommandResp.setBrakeLedEnable(brakeLightSwitch.isChecked());
        command = CommandManager.getFrontBehindLightCommand(lockCommandResp.alarmStatus & 0x03);
        brakeLedCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        byte[] dataBytes = printGattCharacteristicReadEvent(event);
        if (dataBytes != null) {
            processReadEvent(respManager.obtainData(dataBytes));
        }
    }

    private void processReadEvent(byte[] data) {
        if (data == null) {
            return;
        }
        Log.e("AA-data",BlueUtils.bytesToHexString(data));

        respManager.processCommandResp(getOnReadCommand(data), data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicWriteEvent(GattCharacteristicWriteEvent event) {
        printGattCharacteristicWriteEvent(event);
        processWriteEvent(event.data);
    }

    @SuppressLint("DefaultLocale")
    private void processWriteEvent(byte[] dataBytes) {
        if (dataBytes == null || dataBytes.length <= 0) {
            return;
        }
        String command = BlueUtils.bytesToAscii(dataBytes);
        if (command.equals(ledColorCommand)) {
            showToast(String.format("颜色%d设置成功", colorSelectIndex + 1));
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

    private void renderCollectionImageView() {
        for (int i = 0; i < colorCollectionViews.length; i++) {
            colorCollectionViews[i].setImageResource(i == colorSelectIndex ? R.mipmap.ic_color_pick : 0);
        }
    }

    @OnClick({R.id.color1, R.id.color2, R.id.color3, R.id.color4})
    public void onColorViewClick(View view) {
        switch (view.getId()) {
            case R.id.color1:
                colorSelectIndex = 0;
                break;
            case R.id.color2:
                colorSelectIndex = 1;
                break;
            case R.id.color3:
                colorSelectIndex = 2;
                break;
            case R.id.color4:
                colorSelectIndex = 3;
                break;
        }
        renderCollectionImageView();
    }

    @OnClick(R.id.arrow_indicator)
    void onSettingIndicationClick() {
        startTranslateSettingLayout();
    }

    //关闭、单色呼吸、全彩呼吸、双龙戏珠、全彩分向、单色流星、炫彩流星、警灯模式1-3
    @OnClick(R.id.ambient_light_layout)
    void onAmbientLightClick() {
        showDialogAmbientLightSelect();
    }

    private void showDialogAmbientLightSelect() {
        new MaterialDialog.Builder(this)
                .title("氛围灯模式选择")
                .items(R.array.ambient_mode_array)
                .itemsCallbackSingleChoice(ambientLightMode % ambientLightModeCount, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        ambientLightMode = which;
                        updateAmbientModeView(which);
                        setAmbientLightMode(which);
                        return true;
                    }
                })
                .negativeText("取消")
                .negativeColor(Color.BLACK)
                .show();
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
