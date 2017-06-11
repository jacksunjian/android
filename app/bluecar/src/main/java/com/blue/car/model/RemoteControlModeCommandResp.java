package com.blue.car.model;

import com.blue.car.service.BlueUtils;

public class RemoteControlModeCommandResp {

    public int sysStatus;
    //当前工作模式：0待机，1助力，2骑行，3锁车，4遥控
    public int workMode;
    public float batteryRemainPercent;
    public float speed;

    public boolean isRemoteMode() {
        return workMode == 4;
    }

    public boolean isStandByMode() {
        return workMode == 0;
    }

    public boolean isPowerMode() {
        return workMode == 1;
    }

    /*系统状态：bit0 =1 表示限速模式 bit1表示锁车模式，bit4表示遥控模式，bit10表示站人，
    bit14表示被拎起，遥控模式时要求，不站人和拎起，有时弹窗提醒*/
    public boolean isSpeedLimitStatus() {
        return BlueUtils.isOnCondition(sysStatus, 0);
    }

    public boolean isLockConditionStatus() {
        return BlueUtils.isOnCondition(sysStatus, 1);
    }

    public boolean isRemoteConditionStatus() {
        return BlueUtils.isOnCondition(sysStatus, 4);
    }

    public boolean isStandingManStatus() {
        return BlueUtils.isOnCondition(sysStatus, 10);
    }

    public boolean isPickingUpStatus() {
        return BlueUtils.isOnCondition(sysStatus, 14);
    }
}
