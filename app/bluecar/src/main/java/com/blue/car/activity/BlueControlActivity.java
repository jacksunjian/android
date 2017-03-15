package com.blue.car.activity;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blue.car.R;
import com.blue.car.custom.RotationImageView;
import com.blue.car.service.BluetoothConstant;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/5.
 */

public class BlueControlActivity extends BaseActivity {

    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;

    @Bind(R.id.speed_textView)
    TextView speedTextView;

    @Bind(R.id.remote_control_view)
    RotationImageView controlView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_blue_control;
    }

    @Override
    protected void initConfig() {

    }

    @Override
    protected void initView() {
        lhTvTitle.setText("蓝牙控制");
        speedTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/zaozigongfang.otf"));
        controlView.setRotationDiff(90);
        controlView.setOnMoveScaleChangedListener(new RotationImageView.OnMoveScaleChangedListener() {
            @Override
            public void onScaleChanged(float xScale, float yScale) {
                if (BluetoothConstant.USE_DEBUG) {
                    speedTextView.setText(String.format("%.1f", xScale * 30));
                }
            }

            @Override
            public void onLongScaleChanged(float xScale, float yScale) {
            }
        });
    }

    @Override
    protected void initData() {

    }


    @OnClick({R.id.lh_btn_back, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
        }
    }
}
