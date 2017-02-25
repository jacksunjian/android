package com.blue.car.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.blue.car.R;

public class SlideUpTestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_slide_up;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void startRegisterEventBus() {
    }

    @Override
    protected void stopRegisterEventBus() {
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_top_out);
    }
}
