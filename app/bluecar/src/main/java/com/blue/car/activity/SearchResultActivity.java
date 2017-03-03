package com.blue.car.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.blue.car.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by admin on 2017/3/2.
 */

public class SearchResultActivity extends BaseActivity {
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.paired_devices)
    ListView pairedDevices;
    @Bind(R.id.button_scan)
    Button buttonScan;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_result;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("搜索结果");

    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.button_scan)
    public void onClick() {
    }
}
