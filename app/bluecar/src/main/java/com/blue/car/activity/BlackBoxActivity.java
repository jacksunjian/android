package com.blue.car.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlackBoxActivity extends BaseActivity {
    private static int BLACK_BOX_START = 1024;
    private static int BLACK_BOX_END = 3072;

    @Bind(R.id.black_listView)
    ListView blackBoxListView;
    @Bind(R.id.lh_btn_back)
    Button lhBtnBack;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.lh_tv_title)
    TextView lhTvTitle;

    private CommandRespManager respManager = new CommandRespManager();
    private int blackBoxCommandIndex = BLACK_BOX_START;

    private String lockCommand;
    private String unLockCommand;
    private String blackBoxCommand;

    private boolean stopSendBlackBoxCommand = false;


    ArrayList<BlackBoxCommandResp> list = new ArrayList<>();
    MyAdapter myAdapter;

    private MaterialDialog loadingDialog;

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
        lhTvTitle.setText("黑匣子信息");
    }

    private void initListView() {
        blackBoxListView.setAdapter(myAdapter = new MyAdapter(list));
        blackBoxListView.setDivider(new ColorDrawable(Color.WHITE));
        blackBoxListView.setDividerHeight(1);
    }

    @Override
    protected void initData() {
        showCarLockDialog();
    }

    private void showCarLockDialog() {
        new MaterialDialog.Builder(this)
                .content("黑匣子读取需要锁车，是否继续？")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startLockCommand();
                    }
                }).show();
    }

    private void showReadingBlackInfoDialog() {
        loadingDialog = new MaterialDialog.Builder(this)
                .backgroundColor(Color.argb(80, 0, 0, 0))
                .contentColor(Color.WHITE)
                .widgetColor(Color.WHITE)
                .canceledOnTouchOutside(false)
                .keyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return true;
                        }
                        return false;
                    }
                })
                .content("读取中，请等待完毕...")
                .progress(true, 0)
                .build();
        loadingDialog.getWindow().setDimAmount(0);
        loadingDialog.show();
    }

    private void stopLoadingDialog() {
        loadingDialog.dismiss();
        loadingDialog = null;
    }

    private void showUnLockDialog() {
        new MaterialDialog.Builder(this)
                .content("黑匣子数据读取完成，是否解锁?")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startUnLockCommand();
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
        if (StringUtils.isNullOrEmpty(blackBoxCommand)) {
            blackBoxCommand = getBlackBoxCommand(command);
        }
        respManager.addCommandRespCallBack(blackBoxCommand, blackBoxCommandCallback);
        writeCommand(command);
    }

    private String getBlackBoxCommand(byte[] command) {
        return BlueUtils.bytesToAscii(command, 4, 1) + " blackBox";
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

    private void processBlackBoxCommandResp(BlackBoxCommandResp resp) {
        if (resp.time == 0xFFFFFFFF && resp.code == 0xFFFF && resp.additional == 0xFFFF) {
            stopBlackAndUnlock();
        }
        if (stopSendBlackBoxCommand) {
            return;
        }
        updateView(resp);
        startBlackBoxCommand(blackBoxCommandIndex += 4);
    }

    private void stopBlackAndUnlock() {
        stopSendBlackBoxCommand = true;
        blackBoxCommandIndex = BLACK_BOX_START;
        showUnLockDialog();
        stopLoadingDialog();
    }

    private void updateView(BlackBoxCommandResp resp) {
        //  adapter.add(resp.time + "-" + resp.code + "-" + resp.additional);
        list.add(resp);
        myAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattCharacteristicReadEvent(GattCharacteristicReadEvent event) {
        byte[] dataBytes = printGattCharacteristicReadEvent(event);
        if (dataBytes != null) {
            byte[] result = respManager.obtainData(dataBytes);
            if (result == null) {
                return;
            }
            respManager.processCommandResp(getBlackBoxCommand(result), result);
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
            showReadingBlackInfoDialog();
            startBlackBoxCommand(blackBoxCommandIndex);
            return;
        } else if (command.equals(unLockCommand)) {
            showToast("车子解锁成功");
            return;
        }
        String blackCommand = getBlackBoxCommand(dataBytes);
        if (blackCommand.equals(blackBoxCommand)) {
            if (BlueUtils.byteArrayToInt(dataBytes, 5, 2) >= BLACK_BOX_END - 4) {
                stopBlackAndUnlock();
            }
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


    @OnClick({R.id.lh_btn_back, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lh_btn_back:
            case R.id.ll_back:
                onBackPressed();
                break;
        }
    }

    public static class MyAdapter extends BaseAdapter {

        private List<BlackBoxCommandResp> data;

        public MyAdapter(List<BlackBoxCommandResp> data) {
            this.data = data;
        }


        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public BlackBoxCommandResp getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            BlackBoxCommandResp resp = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.black_box_list_item, null);
                holder = new ViewHolder();
                holder.timeTv = (TextView) convertView.findViewById(R.id.time_tv);
                holder.codeTv = (TextView) convertView.findViewById(R.id.code_tv);
                holder.messageTv = (TextView) convertView.findViewById(R.id.message_tv);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            holder.timeTv.setText("" + StringUtils.getTime(resp.time));
            holder.codeTv.setText("" + resp.code);
            holder.messageTv.setText("" + resp.additional);
            return convertView;
        }


        static class ViewHolder {
            TextView timeTv;
            TextView codeTv;
            TextView messageTv;
        }
    }
}
