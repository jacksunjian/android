package com.blue.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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

    @Bind(R.id.blueControl_iv)
    ImageView blueControlIv;
    int isSpeedControl = 0;

    int isLock = 0;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.suo_iv)
    ImageView suoIv;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        llBack.setVisibility(View.GONE);
        ivRight.setVisibility(View.VISIBLE);
        speedIv.setBackgroundResource(R.mipmap.main);
        idspeedControlIv.setBackgroundResource(R.mipmap.xiansu_off);
    }

    @Override
    protected void initData() {

    }


    @OnClick({R.id.iv_right, R.id.ll_right, R.id.speed_iv, R.id.idspeedControl_iv, R.id.suo_iv, R.id.blueControl_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_right:
                Intent it = new Intent(this, SettingMoreActivity.class);
                startActivity(it);
                break;
            case R.id.speed_iv:
                if (isLock == 0) {
                    speedIv.setBackgroundResource(R.mipmap.main_lock);
                    isLock = 1;
                } else {
                    speedIv.setBackgroundResource(R.mipmap.main);
                    isLock = 0;
                }

                break;
            case R.id.idspeedControl_iv:
                if (isSpeedControl == 0) {
                    idspeedControlIv.setBackgroundResource(R.mipmap.xiansu_on);
                    isSpeedControl = 1;
                    isLimitTv.setVisibility(View.VISIBLE);
                } else {
                    idspeedControlIv.setBackgroundResource(R.mipmap.xiansu_off);
                    isSpeedControl = 0;
                    isLimitTv.setVisibility(View.GONE);
                }
                break;
            case R.id.suo_iv:
                break;
//                ActivityOptionsCompat options =
//                        ActivityOptionsCompat.makeCustomAnimation(this,
//                                R.anim.slide_bottom_in,
//                                R.anim.anim_none_alpha);
//                Intent intent = new Intent(this, CurrentInfoActivity.class);
//                intent.putExtra("isLimit", isSpeedControl);
//                ActivityCompat.startActivity(this, intent, options.toBundle());
//                break;
            case R.id.blueControl_iv:
                Intent it_blue = new Intent(this, BlueControlActivity.class);
                startActivity(it_blue);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.suo_iv)
    public void onClick() {
    }
}
