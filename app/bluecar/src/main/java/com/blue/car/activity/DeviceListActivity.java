package com.blue.car.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.blue.car.R;
import com.blue.car.service.BluetoothConstant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends AppCompatActivity {
    private static final String TAG = DeviceListActivity.class.getSimpleName();
    private static final String SEPARATOR = "\n";
    private static final int STOP_LE_SCAN_DELAY = 60 * 1000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    private BroadcastReceiver bleDiscoveryReceiver;

    private ArrayList<String> deviceList = new ArrayList<>();

    private TextView bindTextView;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
        // Set result CANCELED inCase the user backs out
        //setResult(Activity.RESULT_CANCELED);
        initViews();
        doLeScan();
    }

    private void initViews() {
        bindTextView = (TextView) findViewById(R.id.title_paired_devices);

        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cleanDevicesArrayAdapter();
                reLeScan();
            }
        });

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_list_item));
        pairedListView.setOnItemClickListener(deviceItemClickListener);
    }

    private void initBondedDevices() {
        Set<BluetoothDevice> deviceSet = getBluetoothAdapter().getBondedDevices();
        if (deviceSet != null && !deviceSet.isEmpty()) {
            Iterator<BluetoothDevice> iterator = deviceSet.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice device = iterator.next();
                addDeviceToAdapter(getBluetoothDeviceAlias(device));
            }
        }
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bluetoothAdapter;
    }

    private BluetoothLeScanner getBluetoothLeScanner() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        }
        return bluetoothLeScanner;
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
        mScanning = false;
        //1.first method
        //getBluetoothAdapter().stopLeScan(scanCallback1);
        //2.second method
        //getBluetoothLeScanner().stopScan(scanCallback2);
        //3.third method
        stopDiscovery();
    }

    private void startLeScan() {
        mScanning = true;
        //1.first method
        //getBluetoothAdapter().startLeScan(scanCallback1);
        //2.second method
        //getBluetoothLeScanner().startScan(scanCallback2);
        //3.broadcast method
        startDiscovery();
    }

    private void processDeviceItemClick(View view, int position) {
        if (!(view instanceof TextView)) {
            return;
        }
        String alias = ((TextView) view).getText().toString();
        String[] nameAndAddress = splitBluetoothDeviceAlias(alias);
        Intent intent = new Intent();
        intent.putExtra(BluetoothConstant.EXTRAS_DEVICE_NAME, nameAndAddress[0]);
        intent.putExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS, nameAndAddress[1]);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private OnItemClickListener deviceItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            stopLeScan();
            processDeviceItemClick(view, position);
        }
    };

    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    private void scanLeDevice(final boolean enable) {
        if (!enable) {
            stopLeScan();
            return;
        }
        if (mScanning) {
            return;
        }
        startLeScan();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopLeScan();
            }
        }, STOP_LE_SCAN_DELAY);
    }

    private void addDeviceToAdapter(String deviceAlias) {
        if (deviceList.contains(deviceAlias)) {
            return;
        }
        bindTextView.setVisibility(View.VISIBLE);
        deviceList.add(deviceAlias);
        pairedDevicesArrayAdapter.add(deviceAlias);
        pairedDevicesArrayAdapter.notifyDataSetChanged();
    }

    private void cleanDevicesArrayAdapter() {
        pairedDevicesArrayAdapter.clear();
        pairedDevicesArrayAdapter.notifyDataSetChanged();
    }

    private String[] splitBluetoothDeviceAlias(String bluetoothDeviceAlias) {
        return bluetoothDeviceAlias.split(SEPARATOR);
    }

    private String getBluetoothDeviceAlias(BluetoothDevice device) {
        if (device == null) {
            return "unknown name" + SEPARATOR + "unknown address";
        }
        String name = device.getName();
        String address = device.getAddress();
        if (name == null) {
            name = "unknown name";
        }
        return getBluetoothDeviceAlias(name, address);
    }

    private String getBluetoothDeviceAlias(String deviceName, String deviceAddress) {
        return deviceName + SEPARATOR + deviceAddress;
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
                    addDeviceToAdapter(getBluetoothDeviceAlias(device));
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Toast.makeText(DeviceListActivity.this, "扫描完成了", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(bleDiscoveryReceiver, filter);
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
}
