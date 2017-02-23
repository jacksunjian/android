package com.blue.car.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.events.GattConnectStatusEvent;
import com.blue.car.events.GattServiceDiscoveryEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;

    private void onGattConnect(BluetoothGatt gatt) {
        GattConnectStatusEvent statusEvent = new GattConnectStatusEvent();
        statusEvent.status = 1;
        EventBus.getDefault().post(statusEvent);
    }

    private void onGattDisconnect(BluetoothGatt gatt) {
        EventBus.getDefault().post(new GattConnectStatusEvent());
    }

    private void onServiceDiscovery(BluetoothGatt gatt) {
        EventBus.getDefault().post(new GattServiceDiscoveryEvent());
    }

    private void onDataCharacteristicRead(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
        GattCharacteristicReadEvent readEvent = new GattCharacteristicReadEvent();
        readEvent.status = status;
        readEvent.uuid = characteristic.getUuid();
        readEvent.data = characteristic.getValue();
        EventBus.getDefault().post(readEvent);
    }

    private void onDataCharacteristicWrite(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic,
                                           int status) {
        GattCharacteristicWriteEvent writeEvent = new GattCharacteristicWriteEvent();
        writeEvent.status = status;
        writeEvent.uuid = characteristic.getUuid();
        writeEvent.data = characteristic.getValue();
        EventBus.getDefault().post(writeEvent);
    }

    private void onDataCharacteristicChanged(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
        GattCharacteristicReadEvent readEvent = new GattCharacteristicReadEvent();
        readEvent.status = status;
        readEvent.uuid = characteristic.getUuid();
        readEvent.data = characteristic.getValue();
        EventBus.getDefault().post(readEvent);
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.e(TAG, "Connected to GATT server.");
                    Log.e(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());
                    onGattConnect(gatt);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "Disconnected from GATT server.");
                    onGattDisconnect(gatt);
                    break;
                default:
                    Log.e(TAG, "connect status: " + status);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onServiceDiscovery(gatt);
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onDataCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            onDataCharacteristicChanged(gatt, characteristic, BluetoothGatt.GATT_SUCCESS);
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            onDataCharacteristicWrite(gatt, characteristic, status);
        }
    };

    public class BluetoothLeBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    //bindService流程，onCreate()--->onBind()
    //重复调用bindService的话，onCreate(),onBind()这两个方法不会再执行。
    //unbindService()以后，就会执行onUnbind()-->onDestroy()方法。
    //大致的生命周期是：onCreate()--->onBind()--->onUnbind()-->onDestroy();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //startService流程,onCreate->onStartCommand->onDestroy
    //多次startService的话，只会走onStartCommand,方便你传数据intent进来
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        closeResource();
        super.onDestroy();
    }

    private final IBinder mBinder = new BluetoothLeBinder();

    /*
    要实现这样的需求：在activity中要得到service对象调用对象的方法，但同时又不希望activity finish的时候
    service也被destroy了。
    -->startService->bindService
    那么在onPause方法中，执行unbindService()即可，只有onUnbind方法会执行，onDestroy不会执行
    (Service依然存在，可通过isDestroy方法判断)，因为还有一个startService的启动方式存在。
    如果要完全退出Service，那么就得执行unbindService()以及stopService（或者stopSelf）。
    也许有人会问，那如果先执行stopService，会出现什么情况呢？
    答案是：Service依然存在，可通过isDestroy方法判断，因为还有一个bindService的启动方式存在。*/

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress)
                && bluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing bluetoothGatt for connection.");
            if (bluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        bluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (!isValidBluetooth()) {
            return;
        }
        bluetoothGatt.disconnect();
    }

    public void closeResource() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private boolean isValidBluetooth() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        return true;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            return null;
        }
        return bluetoothGatt.getServices();
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
