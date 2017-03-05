package com.blue.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blue.car.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/2/28.
 */

public class SearchActivity extends BaseActivity {
    @Bind(R.id.searchstate_tv)
    TextView searchstateTv;
    @Bind(R.id.search_pb)
    ProgressBar searchPb;
    @Bind(R.id.enter_btn)
    Button enterBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
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

    @OnClick({R.id.search_pb, R.id.enter_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_pb:
                break;
            case R.id.enter_btn:
                Intent it = new Intent(this,MainActivity.class);
                startActivity(it);
                break;
        }
    }
}
