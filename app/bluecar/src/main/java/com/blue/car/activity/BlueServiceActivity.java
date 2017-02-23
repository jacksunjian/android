package com.blue.car.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.util.Log;
import android.widget.Toast;

import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.events.GattConnectStatusEvent;
import com.blue.car.events.GattServiceDiscoveryEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.model.FirstStartCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.CollectionUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class BlueServiceActivity extends BaseActivity {
    private static final boolean USE_DEBUG = BluetoothConstant.USE_DEBUG;

    private static final String TAG = BlueServiceActivity.class.getSimpleName();

    private static final int COARSE_LOCATION_PERMS_REQUEST_CODE = 1011;
    private static final String[] COARSE_LOCATION_PERMS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private final static String UUID_STRING_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private final static String UUID_STRING_CHARACTER_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private final static String UUID_STRING_CHARACTER_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    private final static String UUID_STRING_CHARACTER_DESC = "00002902-0000-1000-8000-00805f9b34fb";

    public final static UUID UUID_SERVICE = UUID.fromString(UUID_STRING_SERVICE);
    public final static UUID UUID_CHARACTER_TX = UUID.fromString(UUID_STRING_CHARACTER_TX);
    public final static UUID UUID_CHARACTER_RX = UUID.fromString(UUID_STRING_CHARACTER_RX);
    public static final UUID UUID_CHARACTER_DESC = UUID.fromString(UUID_STRING_CHARACTER_DESC);

    private Handler processHandler = new Handler();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeService bluetoothLeService = null;

    private String deviceName;
    private String deviceAddress;

    private Map<String, Integer> commandMap = new HashMap<>();
    private String command;
    private int firstCommandSendCount = 0;
    private boolean firstCommandResp = false;

    private List<byte[]> commandDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Integer getLayoutId() {
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
        bluetoothAdapter = bluetoothManager.getAdapter();

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

    private void ensureBluetoothDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
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
        initRxCharacteristic(bluetoothLeService.getBluetoothGatt());
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
            byte[] result = obtainData(dataBytes);
            processCommandResp(result);
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

    private BluetoothGattCharacteristic initRxCharacteristic(BluetoothGatt bluetoothGatt) {
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID_SERVICE);
        if (bluetoothGattService == null) {
            return null;
        }
        BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID_CHARACTER_RX);
        if (characteristic == null) {
            return null;
        }
        boolean success = bluetoothGatt.setCharacteristicNotification(characteristic, true);
        LogUtils.e("UUID_CHARACTER_RX", "notifyEnableResult:" + success);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTER_DESC);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            success = bluetoothGatt.writeDescriptor(descriptor);
            LogUtils.e("UUID_CHARACTER_RX", "notifyDescriptorResult:" + success);
        }
        return characteristic;
    }

    private void processCommandResp(byte[] resp) {
        if (StringUtils.isNullOrEmpty(command) || !commandMap.containsKey(command)) {
            return;
        }
        switch (commandMap.get(command)) {
            case 1:
                processFirstCommandResp(resp);
                break;
            default:
                break;
        }
    }

    private synchronized byte[] obtainData(byte[] data) {
        boolean result = CommandManager.startWithStartFrameHead(data);
        if (result) {
            result = CommandManager.checkDataComplete(data);
            if (result) {
                return data;
            }
            commandDataList.add(data);
            return null;
        } else {
            commandDataList.add(data);
            List<byte[]> list = commandDataList;
            commandDataList = new ArrayList<>();
            return BlueUtils.byteListToArrayByte(list);
        }
    }

    private void processFirstCommandResp(byte[] data) {
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

    private synchronized boolean getFirstCommandResp(boolean needSet, boolean value) {
        if (needSet) {
            firstCommandResp = value;
        }
        return firstCommandResp;
    }

    private void startFirstStartCommand() {
        command = new String(CommandManager.getFirstCommand());
        commandMap.put(command, 1);
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
        BluetoothGattCharacteristic characteristic = getServiceCharacteristic(bluetoothGatt);
        if (characteristic == null) {
            showToast("找不到服务的特征，无法操作");
            return;
        }
        characteristic.setValue(command);
        boolean success = bluetoothGatt.writeCharacteristic(characteristic);
        LogUtils.e("sendCommand", "result:" + success);
    }

    private BluetoothGattCharacteristic getServiceCharacteristic(BluetoothGatt bluetoothGatt) {
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID_SERVICE);
        if (bluetoothGattService == null) {
            return null;
        }
        BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID_CHARACTER_TX);
        if (characteristic == null) {
            return null;
        }
        return characteristic;
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
}
