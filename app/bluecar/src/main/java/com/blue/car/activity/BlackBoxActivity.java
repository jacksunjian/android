package com.blue.car.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blue.car.R;
import com.blue.car.events.GattCharacteristicReadEvent;
import com.blue.car.events.GattCharacteristicWriteEvent;
import com.blue.car.manager.CommandManager;
import com.blue.car.manager.CommandRespManager;
import com.blue.car.model.BlackBoxCommandResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.utils.LogUtils;
import com.blue.car.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;

public class BlackBoxActivity extends BaseActivity {
    private static int BLACK_BOX_START = 1024;
    private static int BLACK_BOX_END = 3072;

    @Bind(R.id.black_listView)
    ListView blackBoxListView;

    private CommandRespManager respManager = new CommandRespManager();
    private int blackBoxCommandIndex = BLACK_BOX_START;

    private String lockCommand;
    private String unLockCommand;
    private String blackBoxCommand;

    private ArrayAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_blackbox_query;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        initListView();
    }

    private void initListView() {
        blackBoxListView.setAdapter(adapter = new ArrayAdapter<>(this, R.layout.black_box_list_item));
        blackBoxListView.setDivider(new ColorDrawable(Color.WHITE));
        blackBoxListView.setDividerHeight(1);
    }

    @Override
    protected void initData() {
        showCarLockDialog();
    }

    private void showCarLockDialog() {
        new MaterialDialog.Builder(this)
                .content("黑匣子读取第一步要先锁车")
                .positiveText("同意")
                .negativeText("取消")
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startLockCommand();
                    }
                }).show();
    }

    private void startLockCommand() {
        byte[] command = CommandManager.getLockCarCommand();
        lockCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void startUnLockCommand() {
        byte[] command = CommandManager.getUnLockCarCommand();
        unLockCommand = BlueUtils.bytesToAscii(command);
        writeCommand(command);
    }

    private void startBlackBoxCommand(int data) {
        byte[] command = CommandManager.getBlackBoxCommand(data);
        respManager.setCommandRespCallBack(blackBoxCommand = BlueUtils.bytesToAscii(command), blackBoxCommandCallback);
        writeCommand(command);
    }

    private CommandRespManager.OnDataCallback blackBoxCommandCallback = new CommandRespManager.OnDataCallback() {
        @Override
        public void resp(byte[] data) {
            if (StringUtils.isNullOrEmpty(data)) {
                return;
            }
            boolean result = CommandManager.checkVerificationCode(data);
            LogUtils.e("checkVerificationCode", String.valueOf(result));
            if (result) {
                BlackBoxCommandResp boxCommandResp = CommandManager.getBlackBoxCommandResp(data);
                LogUtils.jsonLog(getClass().getSimpleName(), boxCommandResp);
                processBlackBoxCommandResp(boxCommandResp);
            }
        }
    };

    private boolean stopSendBlackBoxCommand = false;

    private void processBlackBoxCommandResp(BlackBoxCommandResp resp) {
        updateView(resp);
        if (resp.time == 0xFFFFFFFF && resp.code == 0xFFFF && resp.additional == 0xFFFF) {
            stopBlackAndUnlock();
        }
        if (!stopSendBlackBoxCommand) {
            startBlackBoxCommand(blackBoxCommandIndex += 4);
        }
    }

    private void stopBlackAndUnlock(){
        stopSendBlackBoxCommand = true;
        blackBoxCommandIndex = BLACK_BOX_START;
        startUnLockCommand();
    }

    private void updateView(BlackBoxCommandResp resp) {
        adapter.add(resp.time + "-" + resp.code + "-" + resp.additional);
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        byte[] dataBytes = printGattCharacteristicReadEvent(event);
        if (dataBytes != null) {
            byte[] result = respManager.obtainData(dataBytes);
            respManager.processCommandResp(result);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicWriteEvent(GattCharacteristicWriteEvent event) {
        printGattCharacteristicWriteEvent(event);
        processWriteEvent(event.data);
    }

    private void processWriteEvent(byte[] dataBytes) {
        String command = BlueUtils.bytesToAscii(dataBytes);
        if (command.equals(lockCommand)) {
            startBlackBoxCommand(blackBoxCommandIndex);
        } else if (command.equals(blackBoxCommand)) {
            if (BlueUtils.byteArrayToInt(dataBytes, 5, 2) >= BLACK_BOX_END - 4) {
                stopBlackAndUnlock();
            }
        } else if (command.equals(unLockCommand)) {
            showToast("信息收集完毕，车子解锁");
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
