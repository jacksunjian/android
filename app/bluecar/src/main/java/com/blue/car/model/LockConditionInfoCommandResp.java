package com.blue.car.model;

import com.blue.car.service.BlueUtils;

/**
 * Created by admin on 2017/3/9.
 */

public class LockConditionInfoCommandResp {
    public int alarmStatus;

    public int getAlarmStatus() {
        return this.alarmStatus;
    }

    public boolean isFrontLedOn() {
        return isOnCondition(0);
    }

    public boolean isBrakeLedOn() {
        return isOnCondition(1);
    }

    public boolean isLockCanOff() {
        return isOnCondition(2);
    }

    public void setLockCanPowerOff(boolean canPowerOff) {
        setCondition(2, canPowerOff);
    }

    public boolean isLockNotWarn() {
        return isOnCondition(3);
    }

    public void setLockNotWarn(boolean notWarn) {
        setCondition(3, notWarn);
    }

    public void setFrontLedEnable(boolean enable) {
        setCondition(0, enable);
    }

    public void setBrakeLedEnable(boolean enable) {
        setCondition(1, enable);
    }

    public boolean isOnCondition(int n) {
        return BlueUtils.isOnCondition(alarmStatus, n);
    }

    public boolean isOffCondition(int n) {
        return BlueUtils.isOffCondition(alarmStatus, n);
    }

    public void setCondition(int n, boolean enable) {
        alarmStatus = BlueUtils.setCondition(alarmStatus, n, enable);
    }
}
