package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.blue.car.AppApplication;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.service.BluetoothLeService;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.ButterKnife;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int NOTHING_PERMISSIONS_REQUEST = -1000;

    protected abstract int getLayoutId();

    protected abstract void initConfig();

    protected abstract void initView();

    protected abstract void initData();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);

        initConfig();
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    protected void startRegisterEventBus() {
        EventBus.getDefault().register(this);
    }

    protected void stopRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    protected void showToast(int resId) {
        showToast(getString(resId));
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    protected void writeCommand(byte[] command) {
        BluetoothGattCharacteristic characteristic = getCommandWriteGattCharacteristic(AppApplication.getBluetoothLeService());
        characteristic.setValue(command);
        AppApplication.getBluetoothLeService().getBluetoothGatt().writeCharacteristic(characteristic);
    }

    protected BluetoothGattCharacteristic getCommandWriteGattCharacteristic(BluetoothLeService bluetoothLeService) {
        return bluetoothLeService.getCharacteristic(BluetoothConstant.UUID_SERVICE, BluetoothConstant.UUID_CHARACTER_TX);
    }

    protected byte[] printGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            final byte[] dataBytes = CommandManager.unEncryptData(event.data);
            if (BluetoothConstant.USE_DEBUG) {
                LogUtils.e("onCharacteristicRead", "status:" + event.status);
                LogUtils.e(getClass().getSimpleName(), "onCharRead "
                        + " read "
                        + event.uuid.toString()
                        + " -> "
                        + BlueUtils.bytesToHexString(dataBytes));
            }
            return dataBytes;
        }
        return null;
    }

    protected void printGattCharacteristicWriteEvent(GattCharacteristicWriteEvent event) {
        if (BluetoothConstant.USE_DEBUG && event.status == BluetoothGatt.GATT_SUCCESS) {
            final byte[] dataBytes = event.data;
            LogUtils.e("onCharacteristicWrite", "status:" + event.status);
            LogUtils.e(getClass().getSimpleName(), "onCharWrite "
                    + " write "
                    + event.uuid.toString()
                    + " -> "
                    + BlueUtils.bytesToHexString(dataBytes));
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (getPermissionRequestCode() == NOTHING_PERMISSIONS_REQUEST) {
            ToastUtils.showToast(this, "Permission RequestCode must be override", Toast.LENGTH_SHORT);
            return;
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
            new AppSettingsDialog.Builder(this, getPermissionRationale())
                    .setTitle(getString(R.string.goto_permission_setting))
                    .setPositiveButton(getString(R.string.go_to))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setRequestCode(getPermissionRequestCode())
                    .build()
                    .show();
        } else {
            processTemporaryPermissionsDenied(requestCode, list);
        }
    }

    protected void processTemporaryPermissionsDenied(int requestCode, List<String> list) {
        ToastUtils.showToast(this, getString(R.string.warning_of_permissions_denied), Toast.LENGTH_SHORT);
    }

    protected String getPermissionRationale() {
        return getString(R.string.tip_of_permissions_request);
    }

    //if development need request permission, must override this method
    protected int getPermissionRequestCode() {
        return NOTHING_PERMISSIONS_REQUEST;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }
}
