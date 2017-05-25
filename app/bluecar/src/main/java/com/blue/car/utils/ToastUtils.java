package com.blue.car.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.blue.car.R;

public class ToastUtils {

    private static String oldMsg;
    private static Toast toast = null;
    private static long oneTime = 0;
    private static long twoTime = 0;

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

    public static void showContinueToast(Context appContext, String s) {
        if (toast == null) {
            toast = getWhiteToast(appContext, s, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (s.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = s;
                toast.setText(s);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    private static Toast getWhiteToast(Context context, String message,int duration) {
        TextView textView = new TextView(context);
        textView.setBackgroundResource(R.drawable.toast_white_background);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        textView.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(textView);
        return toast;
    }

    public static void showContinueToast(Context appContext, int resId) {
        showContinueToast(appContext, appContext.getString(resId));
    }
}
