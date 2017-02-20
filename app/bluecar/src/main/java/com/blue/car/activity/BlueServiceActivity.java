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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.blue.car.R;
import com.blue.car.manager.CommandManager;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.CollectionUtils;

import java.util.List;
import java.util.UUID;

public class BlueServiceActivity extends AppCompatActivity {

    private static final String TAG = BlueServiceActivity.class.getSimpleName();

    private final static String UUID_STRING_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final static String UUID_STRING_CHARACTER = "0000ffe4-0000-1000-8000-00805f9b34fb";

    public final static UUID UUID_SERVICE = UUID.fromString(UUID_STRING_SERVICE);
    public final static UUID UUID_CHARACTER = UUID.fromString(UUID_STRING_CHARACTER);

    private final Handler handler = new Handler();

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_service);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
        initConfig();
    }

    private void initConfig() {
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

    private BluetoothLeService bluetoothLeService = null;
    private String deviceName;
    private String deviceAddress;

    private final ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.BluetoothLeBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }

            bluetoothLeService.setOnConnectListener(connectListener);
            bluetoothLeService.setOnServiceDiscoverListener(serviceDiscoverListener);
            bluetoothLeService.setOnDataAvailableListener(dataAvailableListener);
            bluetoothLeService.setHandler(new Handler());
            bluetoothLeService.connect(deviceAddress);

            showToast("onServiceConnecting");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    private BluetoothLeService.OnConnectListener connectListener = new BluetoothLeService.OnConnectListener() {
        @Override
        public void onConnect(BluetoothGatt gatt) {
        }

        @Override
        public void onDisConnect(BluetoothGatt gatt) {
        }
    };

    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeService.OnServiceDiscoverListener serviceDiscoverListener = new BluetoothLeService.OnServiceDiscoverListener() {
        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(bluetoothLeService.getSupportedGattServices());
            writeCommand(gatt, CommandManager.getFirstCommand());
        }
    };

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeService.OnDataAvailableListener dataAvailableListener = new BluetoothLeService.OnDataAvailableListener() {

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                final byte[] dataBytes = characteristic.getValue();
                Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + BlueUtils.bytesToHexString(dataBytes));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + new String(characteristic.getValue()));
        }
    };

    private void writeCommand(BluetoothGatt bluetoothGatt, byte[] command) {
        BluetoothGattService service = bluetoothGatt.getService(UUID_SERVICE);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHARACTER);
            if (characteristic != null) {
                bluetoothGatt.setCharacteristicNotification(characteristic, true);
                characteristic.setValue(command);
                bluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }

    private void displayCharacteristics(List<BluetoothGattCharacteristic> gattCharacteristicList) {
        if (CollectionUtils.isNullOrEmpty(gattCharacteristicList)) {
            return;
        }
        for (int i = 0; i < gattCharacteristicList.size(); i++) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = gattCharacteristicList.get(i);
            Log.e((i + 1) + ",---gattCharacteristic", "------------------------");
            Log.e("---uuid", bluetoothGattCharacteristic.getUuid().toString());
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

    private void processAvailableData(byte[] data) {
        final byte[] dataBuf = data;
        //method 1
        int result = (dataBuf[9] & 0xFF) |
                (dataBuf[8] & 0xFF) << 8 |
                (dataBuf[7] & 0xFF) << 16;
        //method 2
        int result2 = BlueUtils.byteArrayToInt(dataBuf, 7, 3);
        Log.e("data equal?", String.valueOf(result == result2));
    }

    private void startIntentToDeviceScan() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, BluetoothConstant.REQUEST_CONNECT_DEVICE);
    }

    private void startServiceConnection() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, bluetoothServiceConnection, BIND_AUTO_CREATE);
    }

    private void showToast(int resId) {
        showToast(getString(resId));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearBluetoothLeService();
        unbindService(bluetoothServiceConnection);
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
        }
    }
}
