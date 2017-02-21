package com.blue.car.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    public static void showLongToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showShortToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(Context context, int resId) {
        showLongToast(context, context.getString(resId));
    }

    public static void showShortToast(Context context, int resId) {
        showShortToast(context, context.getString(resId));
    }

    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }
}
