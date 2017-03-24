package com.blue.car.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blue.car.R;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Administrator on 2017/2/28.
 */
public class SearchActivity extends BaseActivity {
    private static final String SEPARATOR = "\n";
    private static final int STOP_LE_SCAN_DELAY = 60 * 1000;

    private static final int COARSE_LOCATION_PERMS_REQUEST_CODE = 1011;
    private static final String[] COARSE_LOCATION_PERMS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Bind(R.id.search_state_tv)
    TextView searchStateTv;
    @Bind(R.id.devices_listView)
    ListView pairedListView;

    private List<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bleDiscoveryReceiver;
    private Map<String, String> blueNameAddressMap = new HashMap<>();

    private boolean scanning = false;
    private Handler handler = new Handler();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        initDeviceListView();
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
            doLeScan();
        }
    }

    private void initDeviceListView() {
        pairedListView.setAdapter(pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_list_item));
        pairedListView.setDivider(new ColorDrawable(Color.WHITE));
        pairedListView.setDividerHeight(1);
        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopLeScan();
                processDeviceItemClick(position);
            }
        });
    }

    private void processDeviceItemClick(int position) {
        String deviceAlias = deviceList.get(position);
        String name = getDeviceAliasName(deviceAlias);
        String remoteAddress = getDeviceAliasAddress(deviceAlias);
        if (StringUtils.isNullOrEmpty(remoteAddress)) {
            showToast("找不到蓝牙地址");
            return;
        }
        Intent intent = new Intent(this, BlueServiceActivity.class);
        intent.putExtra(BluetoothConstant.EXTRAS_DEVICE_NAME, name);
        intent.putExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS, remoteAddress);
        startActivity(intent);
        finish();
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doLeScan() {
        scanLeDevice(true);
    }

    private void reLeScan() {
        stopLeScan();
        doLeScan();
    }

    private void stopLeScan() {
        scanning = false;
        stopDiscovery();
    }

    private void startLeScan() {
        scanning = true;
        startDiscovery();
    }

    private void scanLeDevice(final boolean enable) {
        if (!enable) {
            stopLeScan();
            return;
        }
        if (scanning) {
            return;
        }
        startLeScan();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopLeScan();
            }
        }, STOP_LE_SCAN_DELAY);
    }

    private void startDiscovery() {
        initReceiver();
        getBluetoothAdapter().startDiscovery();
    }

    private void stopDiscovery() {
        cleanReceiverRegistered();
        if (getBluetoothAdapter().isDiscovering()) {
            getBluetoothAdapter().cancelDiscovery();
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bleDiscoveryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!(device.getType() == BluetoothDevice.DEVICE_TYPE_LE)) {
                        return;
                    }
                    addDeviceToAdapter(getBluetoothDeviceAlias(device));
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Toast.makeText(SearchActivity.this, "扫描完成了哈", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(bleDiscoveryReceiver, filter);
    }

    private void addDeviceToAdapter(String deviceAlias) {
        if (deviceList.contains(deviceAlias)) {
            return;
        }
        deviceList.add(deviceAlias);
        pairedDevicesArrayAdapter.add(getDeviceAliasName(deviceAlias) + " " + getDeviceAliasAddress(deviceAlias));
        pairedDevicesArrayAdapter.notifyDataSetChanged();
    }

    private String getBluetoothDeviceAlias(BluetoothDevice device) {
        if (device == null) {
            return "unknown name" + SEPARATOR + "unknown address";
        }
        String name = device.getName();
        String address = device.getAddress();
        if (StringUtils.isNullOrEmpty(name)) {
            name = "unknown name";
        }
        if (StringUtils.isNullOrEmpty(address)) {
            address = "unknown address";
        }
        return getBluetoothDeviceAlias(name, address);
    }

    private String getBluetoothDeviceAlias(String deviceName, String deviceAddress) {
        return deviceName + SEPARATOR + deviceAddress;
    }

    private String getDeviceAliasName(String deviceAlias) {
        return splitDeviceAlias(deviceAlias, 0);
    }

    private String getDeviceAliasAddress(String deviceAlias) {
        return splitDeviceAlias(deviceAlias, 1);
    }

    private String splitDeviceAlias(String deviceAlias, int index) {
        String[] result = deviceAlias.split(SEPARATOR);
        return result[index % result.length];
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bluetoothAdapter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLeScan();
    }

    private void cleanReceiverRegistered() {
        try {
            if (bleDiscoveryReceiver != null) {
                unregisterReceiver(bleDiscoveryReceiver);
            }
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.search_btn)
    public void onSearchClick(View view) {
        stopLeScan();
        deviceList.clear();
        pairedDevicesArrayAdapter.clear();
        initBluetooth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BluetoothConstant.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙请求开启", Toast.LENGTH_SHORT).show();
                    doLeScan();
                }
                break;
            case COARSE_LOCATION_PERMS_REQUEST_CODE:
                break;
        }
    }
}
