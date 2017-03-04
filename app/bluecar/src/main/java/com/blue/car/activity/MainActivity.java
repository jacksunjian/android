package com.blue.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/4.
 */

public class MainActivity extends BaseActivity {
    @Bind(R.id.iv_right)
    ImageView ivRight;
    @Bind(R.id.ll_right)
    LinearLayout llRight;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;
    @Bind(R.id.speed_iv)
    ImageView speedIv;
    @Bind(R.id.state_btn)
    Button stateBtn;
    @Bind(R.id.isLimit_tv)
    TextView isLimitTv;
    @Bind(R.id.idspeedControl_iv)
    ImageView idspeedControlIv;
    @Bind(R.id.showMore_iv)
    ImageView showMoreIv;
    @Bind(R.id.blueControl_iv)
    ImageView blueControlIv;
    int isSpeedControl = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        ivRight.setVisibility(View.VISIBLE);

    }

    @Override
    protected void initData() {

    }


    @OnClick({R.id.iv_right, R.id.ll_right, R.id.speed_iv, R.id.idspeedControl_iv, R.id.showMore_iv, R.id.blueControl_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_right:
                Intent it = new Intent(this,SettingMoreActivity.class);
                startActivity(it);
                break;
            case R.id.speed_iv:
                break;
            case R.id.idspeedControl_iv:
              if (isSpeedControl == 0){
                  idspeedControlIv.setBackgroundResource(R.mipmap.xiansu_on);
                  isSpeedControl=1;
                  isLimitTv.setVisibility(View.VISIBLE);
              }else{
                  idspeedControlIv.setBackgroundResource(R.mipmap.xiansu_off);
                  isSpeedControl=0;
                  isLimitTv.setVisibility(View.GONE);
              }
                break;
            case R.id.showMore_iv:
                break;
            case R.id.blueControl_iv:
                break;
        }
    }
}
