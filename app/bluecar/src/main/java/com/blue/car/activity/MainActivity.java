package com.blue.car.activity;

import android.bluetooth.BluetoothGatt;
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
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private CommandRespManager respManager = new CommandRespManager();
    private static final String TAG = "MainActivity";

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
                    writeLimitSpeedCommand();
                } else {
                    idspeedControlIv.setBackgroundResource(R.mipmap.xiansu_off);
                    isSpeedControl = 0;
                    isLimitTv.setVisibility(View.GONE);
                    writeUnLimitSpeedCommand();
                }
                break;
            case R.id.suo_iv:

                writeLockCarCommand();

                writeUnLockCarCommand();

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

    private void writeUnLimitSpeedCommand() {
        byte[] command = CommandManager.getUnLimitSpeedCommand();
        writeCommand(command);
    }

    private void writeLimitSpeedCommand() {
        byte[] command = CommandManager.getLimitSpeedCommand();
        writeCommand(command);
        
    }

    private void writeUnLockCarCommand() {
        byte[] command = CommandManager.getUnLockCarCommand();
        writeCommand(command);
    }

    private void writeLockCarCommand() {
        byte[] command = CommandManager.getLockCarCommand();
        writeCommand(command);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            final byte[] dataBytes = CommandManager.unEncryptData(event.data);
            LogUtils.e("onCharacteristicRead", "status:" + event.status);
            LogUtils.e(TAG, "onCharRead "
                    + " read "
                    + event.uuid.toString()
                    + " -> "
                    + BlueUtils.bytesToHexString(dataBytes));
            byte[] result = respManager.obtainData(dataBytes);
            respManager.processCommandResp(result);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicWriteEvent(GattCharacteristicWriteEvent event) {
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            final byte[] dataBytes = CommandManager.unEncryptData(event.data);
            LogUtils.e("onCharacteristicWrite", "status:" + event.status);
            LogUtils.e(TAG, "onCharWrite "
                    + " write "
                    + event.uuid.toString()
                    + " -> "
                    + BlueUtils.bytesToHexString(dataBytes));
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        startRegisterEventBus();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRegisterEventBus();
    }



}
