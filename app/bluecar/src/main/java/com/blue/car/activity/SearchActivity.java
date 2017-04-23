package com.blue.car.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;
import com.blue.car.utils.StringUtils;

import java.io.UnsupportedEncodingException;
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
    private BluetoothLeScanner bluetoothLeScanner;
    private BroadcastReceiver bleDiscoveryReceiver;
    private Map<String, String> blueNameAddressMap = new HashMap<>();

    private boolean scanning = false;
    private Handler handler = new Handler();
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 0x11;
    byte[] nameBytes;

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
        if (isLocationEnable(SearchActivity.this)) {
            initBluetooth();
        } else {
            showCarLockDialog();

        }
    }

    private void showCarLockDialog() {
        new MaterialDialog.Builder(this)
                .content("需要打开位置，是否继续")
                .positiveText("同意")
                .negativeText("取消")
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        setLocationService();
                    }
                }).show();
    }



    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }

    public static final boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
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
        //1.first method
//        getBluetoothAdapter().stopLeScan(scanCallback1);
        //2.second method
        getBluetoothLeScanner().stopScan(scanCallback2);
        //3.third method
//        stopDiscovery();
    }

    private void startLeScan() {
        scanning = true;
        //1.first method
//        getBluetoothAdapter().startLeScan(scanCallback1);
//        2.second method
//        getBluetoothLeScanner().startScan(scanCallback2);
        List<ScanFilter> bleScanFilters = new ArrayList<>();
        bleScanFilters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(BluetoothConstant.UUID_SERVICE)).build());
        getBluetoothLeScanner().startScan(bleScanFilters, new ScanSettings.Builder().build(), scanCallback2);

        //3.broadcast method
//        startDiscovery();
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
        try {
            nameBytes =name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e("names",""+ BlueUtils.bytesToHexString(nameBytes));
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
                afterPermissionGranted();
                break;
            case REQUEST_CODE_LOCATION_SETTINGS:
                initBluetooth();
                break;


        }
    }

    private BluetoothLeScanner getBluetoothLeScanner() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        }
        return bluetoothLeScanner;
    }

    private BluetoothAdapter.LeScanCallback scanCallback1 = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device != null) {
                        String deviceAlias = getBluetoothDeviceAlias(device);
                        addDeviceToAdapter(deviceAlias);
                    }
                }
            });
        }
    };

    private ScanCallback scanCallback2 = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null) {
                final BluetoothDevice device = result.getDevice();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null) {
                            addDeviceToAdapter(getBluetoothDeviceAlias(device));
                        }
                    }
                });
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };
}
