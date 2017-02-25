package com.blue.car.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.widget.Toast;

import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.events.GattServiceDiscoveryEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.FirstStartCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.CollectionUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class BlueServiceActivity extends BaseActivity {
    private static final boolean USE_DEBUG = BluetoothConstant.USE_DEBUG;

    private static final String TAG = BlueServiceActivity.class.getSimpleName();

    private static final int COARSE_LOCATION_PERMS_REQUEST_CODE = 1011;
    private static final String[] COARSE_LOCATION_PERMS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private BluetoothLeService bluetoothLeService = null;
    private Handler processHandler = new Handler();
    private CommandRespManager respManager = new CommandRespManager();

    private String deviceName;
    private String deviceAddress;

    private int firstCommandSendCount = 0;
    private boolean firstCommandResp = false;

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
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        requestPermission();
    }

    @Override
    protected int getPermissionRequestCode() {
        return COARSE_LOCATION_PERMS_REQUEST_CODE;
    }

    @AfterPermissionGranted(COARSE_LOCATION_PERMS_REQUEST_CODE)
    private void requestPermission() {
        String[] perms = COARSE_LOCATION_PERMS;
        if (EasyPermissions.hasPermissions(this, perms)) {
            afterPermissionGranted();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.request_coarse_location_permission_rationale),
                    COARSE_LOCATION_PERMS_REQUEST_CODE, perms);
        }
    }

    private void afterPermissionGranted() {
        initBluetooth();
    }

    private void initBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "你的手机不支持低功耗蓝牙4.0", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "你的手机连蓝牙都没有，(#‵′)凸", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothConstant.REQUEST_ENABLE_BT);
        } else {
            startIntentToDeviceScan();
        }
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
            showToast("onServiceConnecting");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattServiceDiscoveryEvent(GattServiceDiscoveryEvent event) {
        if (USE_DEBUG) {
            displayGattServices(bluetoothLeService.getSupportedGattServices());
        }
        bluetoothLeService.initNotifyCharacteristic(
                BluetoothConstant.UUID_SERVICE,
                BluetoothConstant.UUID_CHARACTER_RX,
                BluetoothConstant.UUID_CHARACTER_DESC);
        startFirstStartCommand();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            final byte[] dataBytes = CommandManager.unEncryptData(event.data);
            LogUtils.e("onCharacteristicRead", "status:" + event.status);
            LogUtils.e(TAG, "onCharRead " + deviceName
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
            LogUtils.e(TAG, "onCharWrite " + deviceName
                    + " write "
                    + event.uuid.toString()
                    + " -> "
                    + BlueUtils.bytesToHexString(dataBytes));
        }
    }

    private CommandRespManager.OnDataCallback firstCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            getFirstCommandResp(true, true);
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                FirstStartCommandResp resp = CommandManager.getFirstStartCommandRespData(data);
                LogUtils.jsonLog(TAG, resp);
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
                BluetoothGatt gatt = bluetoothLeService.getBluetoothGatt();
                writeCommand(gatt, CommandManager.getFirstCommand());
                writeFirstStartCommand();
            }
        }, 500);
    }

    private void writeCommand(BluetoothGatt bluetoothGatt, byte[] command) {
        BluetoothGattCharacteristic characteristic = getServiceCharacteristic();
        if (characteristic == null) {
            showToast("找不到服务的特征，无法操作");
            return;
        }
        characteristic.setValue(command);
        boolean success = bluetoothGatt.writeCharacteristic(characteristic);
        LogUtils.e("sendCommand", "result:" + success);
    }

    private BluetoothGattCharacteristic getServiceCharacteristic() {
        return bluetoothLeService.getCharacteristic(BluetoothConstant.UUID_SERVICE, BluetoothConstant.UUID_CHARACTER_TX);
    }

    private boolean hasProperty(BluetoothGattCharacteristic characteristic, int property) {
        return (characteristic.getProperties() & property) != 0;
    }

    private void displayCharacteristics(List<BluetoothGattCharacteristic> gattCharacteristicList) {
        if (CollectionUtils.isNullOrEmpty(gattCharacteristicList)) {
            return;
        }
        for (int i = 0; i < gattCharacteristicList.size(); i++) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = gattCharacteristicList.get(i);
            Log.e((i + 1) + ",---gattCharacteristic", "------------------------");
            Log.e("---uuid", bluetoothGattCharacteristic.getUuid().toString());
            Log.e("---canRead", String.valueOf(hasProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_READ)));
            Log.e("---canWrite", String.valueOf(hasProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_WRITE)));
            Log.e("---canWrite_no_response", String.valueOf(hasProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)));
            Log.e("---canNotify", String.valueOf(hasProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY)));
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServiceList) {
        if (CollectionUtils.isNullOrEmpty(gattServiceList)) {
            return;
        }
        for (int i = 0; i < gattServiceList.size(); i++) {
            BluetoothGattService bluetoothGattService = gattServiceList.get(i);
            Log.e((i + 1) + ".gattService", "------------------------");
            Log.e("uuid:", bluetoothGattService.getUuid().toString());
            Log.e("type:", String.valueOf(bluetoothGattService.getType()));
            displayCharacteristics(bluetoothGattService.getCharacteristics());
        }
    }

    private void startIntentToDeviceScan() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, BluetoothConstant.REQUEST_CONNECT_DEVICE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BluetoothConstant.REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    deviceName = data.getStringExtra(BluetoothConstant.EXTRAS_DEVICE_NAME);
                    deviceAddress = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS);
                    showToast(deviceAddress);
                    startServiceConnection();
                }
                break;
            case BluetoothConstant.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙请求开启通过议案", Toast.LENGTH_SHORT).show();
                    startIntentToDeviceScan();
                }
                break;
            case COARSE_LOCATION_PERMS_REQUEST_CODE:
                break;
        }
    }

    @OnClick(R.id.test_other_command_button)
    void onOtherCommandTestClick() {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.test_slide_up_image)
    void onSlideUpToOnClick() {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.slide_bottom_in,
                        R.anim.anim_none_alpha);
        Intent intent = new Intent(this, SlideUpTestActivity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }
}
