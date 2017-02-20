package com.blue.car;

import android.app.Application;

public class AppApplication extends Application {
    static private AppApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    private void initConfig() {
        try {
            sInstance = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
