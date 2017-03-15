package com.blue.car.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.blue.car.service.BluetoothConstant;

import java.util.List;

public class BluetoothGattUtils {

    public static void displayGattServices(List<BluetoothGattService> gattServiceList) {
        if (BluetoothConstant.USE_DEBUG && CollectionUtils.isNullOrEmpty(gattServiceList)) {
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

    public static void displayCharacteristics(List<BluetoothGattCharacteristic> gattCharacteristicList) {
        if (!BluetoothConstant.USE_DEBUG) {
            return;
        }
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

    public static boolean hasProperty(BluetoothGattCharacteristic characteristic, int property) {
        return (characteristic.getProperties() & property) != 0;
    }
}
