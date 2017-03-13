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

    public boolean isLockCanOff() {
        return isOnCondition(2);
    }

    public void setLockCanPowerOff(boolean canPowerOff) {
        setCondition(2, canPowerOff);
    }

    public boolean isLockNotWarn() {
        return isOffCondition(3);
    }

    public void setLockCanWarn(boolean canWarn) {
        setCondition(3, canWarn);
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
