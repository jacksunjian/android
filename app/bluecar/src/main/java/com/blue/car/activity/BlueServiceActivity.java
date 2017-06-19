package com.blue.car.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.custom.MyScrollView;
import com.blue.car.custom.OnScrollListener;
import com.blue.car.custom.OverScrollView;
import com.blue.car.custom.SpeedMainView;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.events.GattServiceDiscoveryEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.AccountInfo;
import com.blue.car.model.FirstStartCommandResp;
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.ActivityUtils;
import com.blue.car.utils.BluetoothGattUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.ScreenUtils;
import com.blue.car.utils.StringUtils;
import com.blue.car.utils.ToastUtils;
import com.blue.car.utils.UniversalViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.OnClick;

public class BlueServiceActivity extends BaseActivity {
    private static final boolean USE_DEBUG = BluetoothConstant.USE_DEBUG;

    private static final String TAG = BlueServiceActivity.class.getSimpleName();

    @Bind(R.id.speed_view)
    SpeedMainView speedMainView;
    @Bind(R.id.speed_limit_img)
    ImageView speedLimitView;
    @Bind(R.id.lock_off_img)
    ImageView lockOffView;

    @Bind(R.id.speedView_layout)
    ViewGroup speedViewLayout;

    /*@Bind(R.id.bounceScrollView)
    OverScrollView bounceScrollView;*/

    @Bind(R.id.myScrollView)
    MyScrollView myScrollView;
    @Bind(R.id.current_speed)
    TextView currentSpeed;
    @Bind(R.id.speed_unit)
    TextView currentSpeedUnit;

    @Bind(R.id.mode_desc_tv)
    TextView modeDescTv;
    @Bind(R.id.system_status_tv)
    TextView sysStatusTv;

    private TextView averageTv;
    private TextView perMeterTv;
    private TextView perRunTimeTv;
    private TextView restRideMeterTv;
    private TextView totalMeterTextTv;
    private TextView temperatureTextTv;
    private TextView batteryPercentTv;

    private BluetoothLeService bluetoothLeService = null;
    private Handler processHandler = new Handler();
    private CommandRespManager respManager = new CommandRespManager();

    private GestureDetectorCompat gestureDetector;
    private int scaledTouchSlop = 100;
    private int scaledMinimumFlingVelocity = 0;

    private String deviceName;
    private String deviceAddress;

    private int firstCommandSendCount = 0;
    private boolean firstCommandResp = false;

    private boolean intentToOtherBefore = false;
    private int firstCommandDelay = 500;
    private int mainFuncCommandDelay = 350;

    private String unLimitSpeedCommand;
    private String limitSpeedCommand;
    private String unLockCarCommand;
    private String lockCarCommand;

    private int speedLayoutHeight;
    private boolean startAnim = false;
    private int slideUpLimit = 80;
    private int slideDownLimit = 20;

    private String[] workModeArray;
    private boolean enablePasswordCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_blue_service2;
    }

    @Override
    protected void initConfig() {
        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());
        scaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        scaledMinimumFlingVelocity = ViewConfiguration.get(this).getScaledMinimumFlingVelocity();
        workModeArray = getResources().getStringArray(R.array.work_mode);
    }

    @Override
    protected void initView() {
        initSpecialLayout();
        initSpeedMainView();
        initScrollView();
        initInfoLayout();
    }

    private void initSpecialLayout() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.speed_panel);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewGroup.getLayoutParams();
        lp.height = ScreenUtils.screenHeight(this) - ScreenUtils.getNavigationBarHeight(this);
        viewGroup.setLayoutParams(lp);

        currentSpeedUnit.setText(AppApplication.instance().getUnitWithTime());
        currentSpeed.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/zaozigongfang.otf"), Typeface.ITALIC);
        currentSpeed.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                currentSpeed.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] position = new int[2];
                currentSpeed.getLocationInWindow(position);
                myScrollView.setDetailLayoutPosition(position[1]);
            }
        });
    }

    private void initSpeedMainView() {
        speedMainView.setKmUnit(AppApplication.instance().isKmUnit());
        speedMainView.setValueAnimatorDuration(mainFuncCommandDelay - 30);
        speedMainView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                speedMainView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                speedLayoutHeight = speedMainView.getHeight();
            }
        });
    }

    @Override
    protected void initData() {
        deviceName = getIntent().getStringExtra(BluetoothConstant.EXTRAS_DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS);
        startServiceConnection();
    }

    private final ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.BluetoothLeBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
            bluetoothLeService.connect(deviceAddress);
            AppApplication.setBluetoothLeService(bluetoothLeService);
            showToast("蓝牙服务连接成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattServiceDiscoveryEvent(GattServiceDiscoveryEvent event) {
        if (USE_DEBUG) {
            BluetoothGattUtils.displayGattServices(bluetoothLeService.getSupportedGattServices());
        }
        bluetoothLeService.initNotifyCharacteristic(
                BluetoothConstant.UUID_SERVICE,
                BluetoothConstant.UUID_CHARACTER_RX,
                BluetoothConstant.UUID_CHARACTER_DESC);
        startFirstStartCommand();
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

    private void processWriteEvent(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return;
        }
        String command = BlueUtils.bytesToAscii(bytes);
        if (command.equals(unLimitSpeedCommand)) {
            speedLimitView.setImageResource(R.mipmap.xiansu_off);
        } else if (command.equals(limitSpeedCommand)) {
            speedLimitView.setImageResource(R.mipmap.xiansu_on);
        } else if (command.equals(unLockCarCommand)) {
            lockOffView.setImageResource(R.mipmap.suo_off);
        } else if (command.equals(lockCarCommand)) {
            lockOffView.setImageResource(R.mipmap.suo_on);
        }
    }

    private CommandRespManager.OnDataCallback firstCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(final byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                getFirstCommandResp(true, true);
                final FirstStartCommandResp resp = CommandManager.getFirstStartCommandRespData(data);
                LogUtils.jsonLog(TAG, resp);
                if (!enablePasswordCheck) {
                    startMainFuncCommand();
                    return;
                }
                if (!resp.isCardIdValid()) {
                    ToastUtils.showLongToast(BlueServiceActivity.this, "cardId校验失败...");
                    AppApplication.instance().setDisconnectDetect(false);
                    ActivityUtils.startActivityWithClearTask(BlueServiceActivity.this, SearchActivity.class);
                    return;
                }
                if (resp.isEmptyPwd()) {
                    startMainFuncCommand();
                } else {
                    String password = null;
                    AccountInfo accountInfo = AccountInfo.currentAccountInfo(BlueServiceActivity.this);
                    if (accountInfo != null) {
                        password = accountInfo.blePassword;
                    }
                    if (StringUtils.isNullOrEmpty(password) || !password.equals(resp.blePassword)) {
                        showPwdCheckDialog(resp.blePassword);
                    } else {
                        startMainFuncCommand();
                    }
                }
            }
        }
    };

    private void showPwdCheckDialog(final String blePassword) {
        new MaterialDialog.Builder(BlueServiceActivity.this)
                .title("输入密码进行校验")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(Color.GRAY)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .inputRange(6, 6)
                .input("6位数字密码", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .alwaysCallInputCallback()
                .keyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return true;
                        }
                        return false;
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String pwd = dialog.getInputEditText().getText().toString();
                        if (pwd.length() != 6) {
                            ToastUtils.showShortToast(BlueServiceActivity.this, "请输入6位数的密码");
                            return;
                        }
                        if (!blePassword.equals(pwd)) {
                            ToastUtils.showShortToast(BlueServiceActivity.this, "密码校验失败");
                            return;
                        }
                        AccountInfo.saveAccountInfo(BlueServiceActivity.this, deviceAddress, deviceName, pwd);
                        startMainFuncCommand();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        AppApplication.instance().setDisconnectDetect(false);
                        ActivityUtils.startActivityWithClearTask(BlueServiceActivity.this, SearchActivity.class);
                    }
                })
                .show();
    }

    private synchronized boolean getFirstCommandResp(boolean needSet, boolean value) {
        if (needSet) {
            firstCommandResp = value;
        }
        return firstCommandResp;
    }

    private void startFirstStartCommand() {
        String command = new String(CommandManager.getFirstCommand());
        respManager.setCommandRespCallBack(command, firstCommandCallback);
        writeFirstStartCommand();
    }

    private void writeFirstStartCommand() {
        processHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getFirstCommandResp(false, false) || firstCommandSendCount++ > 10) {
                    return;
                }
                writeCommand(CommandManager.getFirstCommand());
                writeFirstStartCommand();
            }
        }, firstCommandDelay);
    }

    private void updateSpeedView(MainFuncCommandResp resp) {
        if (resp == null) {
            return;
        }
        speedMainView.setKmUnit(AppApplication.instance().isKmUnit());
        speedMainView.setBatteryPercent(resp.remainBatteryPercent * 1.0f / 100);
        //float speedLimit = Math.max(resp.speedLimit, resp.maxAbsSpeed);
        //speedMainView.setSpeedLimit(AppApplication.instance().getResultByUnit(Math.max(resp.speed, speedLimit) + 5));
        speedMainView.setSpeedLimit(AppApplication.instance().getResultByUnit(Math.max(resp.speed, resp.speedLimit)));
        speedMainView.setSpeed(AppApplication.instance().getResultByUnit(resp.speed));
        speedMainView.setPerMileage(AppApplication.instance().getResultByUnit(resp.perMileage));
    }

    private void updateCurrentSpeed(MainFuncCommandResp resp) {
        currentSpeedUnit.setText(AppApplication.instance().getUnitWithTime());
        currentSpeed.setText(StringUtils.dealSpeedFormatWithoutTime(AppApplication.instance().getResultByUnit(resp.speed)));
    }

    private void updateOtherView(MainFuncCommandResp resp) {
        speedLimitView.setImageResource(resp.isSpeedLimitStatus() ? R.mipmap.xiansu_on : R.mipmap.xiansu_off);
        lockOffView.setImageResource(resp.isLockConditionStatus() ? R.mipmap.suo_on : R.mipmap.suo_off);
        sysStatusTv.setText((resp.isError() || resp.isWarning()) ? R.string.warning_error : R.string.warning_normal);
        //如果骑行模式时有限速，则显示限速模式，否则显示骑行模式
        if (resp.workMode == 2 && resp.isSpeedLimitStatus()) {
            modeDescTv.setText(R.string.mode_speed_limit);
        } else {
            modeDescTv.setText(String.format(getString(R.string.mode_format), workModeArray[resp.workMode % workModeArray.length]));
        }
        if (resp.isError()) {
            ToastUtils.showContinueToast(getApplicationContext(), R.string.car_error_tip);
        }
        if (resp.isWarning()) {
            ToastUtils.showContinueToast(getApplicationContext(), R.string.car_waring_tip);
        }
    }

    private MainFuncCommandResp mainFuncResp;

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
                LogUtils.jsonLog(TAG, mainFuncResp);
                updateSpeedView(mainFuncResp);
                updateOtherView(mainFuncResp);
                updateCurrentInfoView(mainFuncResp);
            }
        }
    };

    private void startMainFuncCommand() {
        String command = new String(CommandManager.getMainFuncCommand());
        respManager.setCommandRespCallBack(command, mainCommandCallback);
        writeMainFuncCommand();
    }

    private void writeMainFuncCommand() {
        processHandler.postDelayed(writeMainFunCommandRunnable, mainFuncCommandDelay);
    }

    Runnable writeMainFunCommandRunnable = new Runnable() {
        @Override
        public void run() {
            writeCommand(CommandManager.getMainFuncCommand());
            writeMainFuncCommand();
        }
    };

    private void writeUnLimitSpeedCommand() {
        byte[] command = CommandManager.getUnLimitSpeedCommand();
        unLimitSpeedCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void writeLimitSpeedCommand() {
        byte[] command = CommandManager.getLimitSpeedCommand();
        limitSpeedCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void writeUnLockCarCommand() {
        byte[] command = CommandManager.getUnLockCarCommand();
        unLockCarCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void writeLockCarCommand() {
        byte[] command = CommandManager.getLockCarCommand();
        lockCarCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private Intent getServiceIntent() {
        return new Intent(this, BluetoothLeService.class);
    }

    private void startServiceConnection() {
        Intent gattServiceIntent = getServiceIntent();
        startService(gattServiceIntent);
        bindService(gattServiceIntent, bluetoothServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (intentToOtherBefore) {
            intentToOtherBefore = false;
            startMainFuncCommand();
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
        try {
            intentToOtherBefore = true;
            processHandler.removeCallbacks(writeMainFunCommandRunnable);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearBluetoothLeService();
        unbindService(bluetoothServiceConnection);
        stopService(getIntent());
    }

    private void clearBluetoothLeService() {
        if (bluetoothLeService != null) {
            bluetoothLeService.disconnect();
            bluetoothLeService = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @OnClick({R.id.info_rl, R.id.setting_rl, R.id.search_btn})
    void someFunPanelClick(View view) {
        switch (view.getId()) {
            case R.id.info_rl:
                gotoIntent(InfoMoreActivity.class);
                break;
            case R.id.setting_rl:
                gotoIntent(SettingMoreActivity.class);
                break;
            case R.id.search_btn:
                showGoToSearchDialog();
                break;
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
                        Intent intent = new Intent(BlueServiceActivity.this, SearchActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).show();
    }

    @OnClick(R.id.speed_limit_img)
    void onSpeedLimitImgClick() {
        if (mainFuncResp == null) {
            return;
        }
        if (mainFuncResp.isSpeedLimitStatus()) {
            writeUnLimitSpeedCommand();
        } else {
            new MaterialDialog.Builder(this)
                    .title("温馨提示")
                    .content("您确定要切换为限速模式吗？")
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .negativeColor(Color.BLACK)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            writeLimitSpeedCommand();
                        }
                    })
                    .show();
        }
    }

    @OnClick(R.id.lock_off_img)
    void onLockImgClick() {
        if (mainFuncResp == null) {
            return;
        }
        if (mainFuncResp.isLockConditionStatus()) {
            writeUnLockCarCommand();
        } else {
            writeLockCarCommand();
        }
    }

    @OnClick(R.id.remote_setting_img)
    void onRemoteSettingImgClick() {
        if (mainFuncResp == null) {
            showToast("未有连接上平衡车");
            return;
        }
        if (mainFuncResp.workMode != 0 && mainFuncResp.workMode != 1) {
            showToast("进入遥控，当前模式必须为助力或者待机");
            return;
        }
        gotoIntent(BlueControlActivity.class);
    }

    private void gotoIntent(Class gotoClass) {
        intentToOtherBefore = true;
        Intent intent = new Intent(this, gotoClass);
        startActivity(intent);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float y = e2.getY() - e1.getY();
            if (y < -scaledTouchSlop
                    && Math.abs(velocityX) < Math.abs(velocityY)
                    && Math.abs(velocityY) > scaledMinimumFlingVelocity) {
                actionGestureFlingUp();
                return true;
            }
            return false;
        }
    }

    private void actionGestureFlingUp() {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.slide_bottom_in,
                        R.anim.anim_none_alpha);
        Intent intent = new Intent(this, CurrentInfoActivity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());
        intentToOtherBefore = true;
    }

    private void initScrollView() {
        /*bounceScrollView.setScrollViewListener(new OnScrollListener() {
            @Override
            public void onScrollChanged(int nowX, int nowY, int oldX, int oldY) {
                updateSpeedViewLayout(nowY, oldY);
            }
        });*/
        myScrollView.setOnScrollChangedListener(new MyScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                if (t <= 390) {
                    int diff = 390 - t;
                    speedViewLayout.setAlpha(diff * 1.0f / 390);
                    if (t <= 240) {
                        currentSpeed.setAlpha(t * 1.0f / 240);
                        currentSpeedUnit.setAlpha(t * 1.0f / 240);
                    }
                }
            }
        });
    }

    /*private void updateSpeedViewLayout(int nowY, int oldY) {
        int limit = slideUpLimit;
        if (nowY > oldY) {
            if (nowY > limit) {
                if (nowY > speedLayoutHeight + 24 + 40) {
                    return;
                }
                speedViewLayout.scrollTo(0, nowY);
            }
        } else {
            if (nowY > speedLayoutHeight + 24 + 40) {
                return;
            }
            if (nowY < 0) {
                return;
            }
            speedViewLayout.scrollTo(0, nowY * -1);
        }
    }*/

    private void updateSpeedViewLayout(int nowY, int oldY) {
        int limit = slideUpLimit;
        if (nowY > oldY) {
            if (nowY > limit) {
                if (!startAnim) {
                    startAnim = true;
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_alpha);
                    speedViewLayout.startAnimation(animation);
                }
            }
        } else {
            if (nowY < limit) {
                if (nowY <= slideDownLimit) {
                    if (startAnim) {
                        startAnim = false;
                        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_alpha);
                        speedViewLayout.startAnimation(animation);
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateCurrentInfoView(MainFuncCommandResp resp) {
        if (resp == null) {
            return;
        }
        updateCurrentSpeed(resp);

        AppApplication app = AppApplication.instance();
        perMeterTv.setText(StringUtils.dealMileFormatWithoutUnit(app.getResultByUnit(resp.perMileage)) +
                app.getPerMeterUnit());
        restRideMeterTv.setText(String.valueOf((int)app.getResultByUnit(resp.getRemainMileage())) +
                app.getPerMeterUnit());
        temperatureTextTv.setText(StringUtils.dealTempFormatWithoutUnit(app.getTemperByUnit(resp.temperature)) +
                app.getTemperUnit());

        //totalMeterTextTv.setText(StringUtils.dealMileFormat(AppApplication.instance().getResultByUnit(resp.totalMileage)));
        //perRunTimeTv.setText(StringUtils.getTime(resp.perRunTime));
        //temperatureTextTv.setText(StringUtils.dealTempFormat(resp.temperature));
        //batteryPercentTv.setText(StringUtils.dealBatteryPercentFormat(resp.remainBatteryPercent * 1.0f / 100));
    }

    private void initInfoLayout() {
        initNormalInfoLayout(R.id.info_rl, "信息", R.mipmap.gengduo);
        initNormalInfoLayout(R.id.setting_rl, "设置", R.mipmap.gengduo);
        AppApplication app = AppApplication.instance();
        perMeterTv = initNormalInfoLayout(R.id.per_meter, "本次里程", "0.0" + app.getPerMeterUnit());
        temperatureTextTv = initNormalInfoLayout(R.id.temperature, "车体温度", "20" + app.getTemperUnit());
        restRideMeterTv = initNormalInfoLayout(R.id.rest_ride_meter, "剩余行驶里程", "0" + app.getPerMeterUnit());

        //in 2 layout no use
        //perRunTimeTv = initNormalInfoLayout(R.id.per_runTime, "本次行驶时间", "0min");
        //totalMeterTextTv = initNormalInfoLayout(R.id.total_meter, "总里程", "0.0km");
        //temperatureTextTv = initNormalInfoLayout(R.id.temperature, "温度", "20℃");
        //batteryPercentTv = initNormalInfoLayout(R.id.battery_percent, "剩余电量百分比", "50%");
    }

    private TextView initNormalInfoLayout(int parentId, String leftText, String rightText) {
        return (TextView) UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightText);
    }

    private void initNormalInfoLayout(int parentId, String leftText, int rightImageResId) {
        UniversalViewUtils.initNormalInfoLayout(this, parentId, leftText, rightImageResId);
    }
}
