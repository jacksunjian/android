package com.blue.car.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.blue.car.model.SpeedLimitResp;

public class PreferenceManager {

    private static SharedPreferences sDefaultPreferences;
    private static SharedPreferences.Editor sDefaultEditor;

    public static void init(Context context) {
        init(context, context.getPackageName() + "_preferences");
    }

    public static void init(Context context, String preferenceName) {
        sDefaultPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getDefaultPrefs() {
        return sDefaultPreferences;
    }

    public static boolean getBooleanValue(Context context, int ResID, boolean defaultValue) {
        return getBooleanValue(context, context.getResources().getString(ResID), defaultValue);
    }

    public static int getIntValue(Context context, int ResID, int defaultValue) {
        return getIntValue(context, context.getResources().getString(ResID), defaultValue);
    }

    public static String getStringValue(Context context, int ResID, String defaultValue) {
        return getStringValue(context, context.getResources().getString(ResID), defaultValue);
    }

    public static boolean getBooleanValue(Context context, String key, boolean defaultValue) {
        return sDefaultPreferences.getBoolean(key, defaultValue);
    }

    public static int getIntValue(Context context, String key, int defaultValue) {
        return sDefaultPreferences.getInt(key, defaultValue);
    }

    public static String getStringValue(Context context, String key, String defaultValue) {
        return sDefaultPreferences.getString(key, defaultValue);
    }

    public static void setBooleanValue(Context context, int ResID, boolean value) {
        setBooleanValue(context, context.getResources().getString(ResID), value);
    }

    public static void setIntValue(Context context, int ResID, int value) {
        setIntValue(context, context.getResources().getString(ResID), value);
    }

    public static void setStringValue(Context context, int ResID, String value) {
        setStringValue(context, context.getResources().getString(ResID), value);
    }

    public static void setBooleanValue(Context context, String key, boolean value) {
        sDefaultEditor = sDefaultPreferences.edit();
        sDefaultEditor.putBoolean(key, value);
        sDefaultEditor.apply();
    }

    public static void setIntValue(Context context, String key, int value) {
        sDefaultEditor = sDefaultPreferences.edit();
        sDefaultEditor.putInt(key, value);
        sDefaultEditor.apply();
    }

    public static void setStringValue(Context context, String key, String value) {
        sDefaultEditor = sDefaultPreferences.edit();
        sDefaultEditor.putString(key, value);
        sDefaultEditor.apply();
    }

    public static void saveSpeedLimitResp(Context context, String value) {
        setStringValue(context, SpeedLimitResp.class.getSimpleName(), value);
    }

    public static String getSpeedLimitResp(Context context) {
        return getStringValue(context, SpeedLimitResp.class.getSimpleName(), "");
    }
}
