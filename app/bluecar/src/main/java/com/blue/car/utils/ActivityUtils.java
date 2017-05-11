package com.blue.car.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.blue.car.activity.OtherSettingActivity;
import com.blue.car.activity.SearchActivity;

/**
 * Created by suicheng on 2017/5/11.
 */

public class ActivityUtils {

    public static void startActivityWithClearTask(Context context, Class<? extends Activity> cls) {
        Intent intent = new Intent(context, cls)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
