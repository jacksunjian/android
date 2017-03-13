package com.blue.car;

import android.app.Application;

import com.blue.car.manager.PreferenceManager;
import com.blue.car.service.BluetoothLeService;

public class AppApplication extends Application {

    static private AppApplication sInstance;
    static private BluetoothLeService bluetoothLeService;

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    private void initConfig() {
        try {
            sInstance = this;
            initPreferenceManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AppApplication instance() {
        return sInstance;
    }

    public void initPreferenceManager() {
        PreferenceManager.init(sInstance.getApplicationContext());
    }

    public static void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        AppApplication.bluetoothLeService = bluetoothLeService;
    }

    public static BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }
}
