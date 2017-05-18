package com.blue.car;

import android.app.Application;

import com.blue.car.manager.PreferenceManager;
import com.blue.car.service.BluetoothLeService;

public class AppApplication extends Application {
    public static final String UNIT_KEY = "unit_key";

    static private AppApplication sInstance;
    static private BluetoothLeService bluetoothLeService;

    private boolean kmUnit = true;

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    private void initConfig() {
        try {
            sInstance = this;
            initPreferenceManager();
            initKmUnitValue();
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

    public void initKmUnitValue() {
        setKmUnit(PreferenceManager.getBooleanValue(sInstance.getApplicationContext(), UNIT_KEY, kmUnit));
    }

    public static void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        AppApplication.bluetoothLeService = bluetoothLeService;
    }

    public static BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }

    private void saveKmUnit(boolean unit) {
        PreferenceManager.setBooleanValue(sInstance.getApplicationContext(), UNIT_KEY, unit);
    }

    public void setKmUnit(boolean unit) {
        this.kmUnit = unit;
        saveKmUnit(unit);
    }

    public boolean isKmUnit() {
        return this.kmUnit;
    }

    public String getUnit() {
        if (kmUnit) {
            return "km";
        } else {
            return "mp";
        }
    }

    public String getUnitWithTime() {
        String value = getUnit();
        String time = "/h";
        if (!kmUnit) {
            time = "h";
        }
        return value + time;
    }

    public float getResultByUnit(float origin) {
        if (kmUnit) {
            return origin;
        } else {
            return origin * 0.62f;
        }
    }

    public String getPerMeterUnit() {
        if (kmUnit) {
            return "km";
        } else {
            return "ml";
        }
    }

    public float getTemperByUnit(float origin) {
        if (kmUnit) {
            return origin;
        } else {
            return 32 + origin * 1.8f;
        }
    }

    public String getTemperUnit() {
        if (kmUnit) {
            return "℃";
        } else {
            return "℉";
        }
    }
}
