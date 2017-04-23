package com.blue.car.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blue.car.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity
{

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        final Intent localIntent = new Intent(this, SearchActivity.class);
        Timer timer = new Timer();
        TimerTask tast = new TimerTask() {
            @Override
            public void run() {
                startActivity(localIntent);
            }
        };
        timer.schedule(tast, 1500);
    }


    @Override
    protected void initData() {

    }

}
