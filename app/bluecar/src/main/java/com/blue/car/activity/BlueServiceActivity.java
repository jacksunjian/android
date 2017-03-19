package com.blue.car.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.custom.SpeedMainView;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.events.GattServiceDiscoveryEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.FirstStartCommandResp;
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.BluetoothGattUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

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

    @Bind(R.id.mode_desc_tv)
    TextView modeDescTv;
    @Bind(R.id.system_status_tv)
    TextView sysStatusTv;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_blue_service;
    }

    @Override
    protected void initConfig() {
        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());
        scaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        scaledMinimumFlingVelocity = ViewConfiguration.get(this).getScaledMinimumFlingVelocity();
    }

    @Override
    protected void initView() {
        speedMainView.setValueAnimatorDuration(mainFuncCommandDelay - 30);
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
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                getFirstCommandResp(true, true);
                FirstStartCommandResp resp = CommandManager.getFirstStartCommandRespData(data);
                LogUtils.jsonLog(TAG, resp);
                startMainFuncCommand();
            }
        }
    };

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
        speedMainView.setBatteryPercent(resp.remainBatteryPercent * 1.0f / 100);
        //speedMainView.setSpeedLimit(resp.speedLimit);
        float speedLimit = Math.max(resp.speedLimit, resp.maxAbsSpeed);
        speedMainView.setSpeedLimit(Math.max(resp.speed, speedLimit) + 5);
        speedMainView.setSpeed(resp.speed);
        speedMainView.setPerMileage(resp.perMileage);
    }

    private void updateOtherView(MainFuncCommandResp resp) {
        speedLimitView.setImageResource(resp.isSpeedLimitStatus() ? R.mipmap.xiansu_on : R.mipmap.xiansu_off);
        lockOffView.setImageResource(resp.isLockConditionStatus() ? R.mipmap.suo_on : R.mipmap.suo_off);
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
            bluetoothLeService.closeResource();
            bluetoothLeService = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @OnClick(R.id.speed_limit_img)
    void onSpeedLimitImgClick() {
        if (mainFuncResp == null) {
            return;
        }
        if (mainFuncResp.isSpeedLimitStatus()) {
            writeUnLimitSpeedCommand();
        } else {
            writeLimitSpeedCommand();
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
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
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
}
