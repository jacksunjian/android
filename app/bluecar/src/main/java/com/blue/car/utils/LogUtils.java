package com.blue.car.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.blue.car.service.BluetoothConstant;

public class LogUtils {

    public static void e(String tag, String str) {
        if (BluetoothConstant.USE_DEBUG) {
            Log.e(tag, JSON.toJSONString(str));
        }
    }

    public static void jsonLog(String tag, Object j) {
        if (BluetoothConstant.USE_DEBUG) {
            Log.e(tag, JSON.toJSONString(j));
        }
    }

    public static void printThrowable(Throwable e) {
        if (BluetoothConstant.USE_DEBUG && e != null) {
            e.printStackTrace();
        }
    }
}
