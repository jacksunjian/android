package com.blue.car.model;

/**
 * Created by admin on 2017/3/9.
 */

public class LockConditionInfoCommandResp {
    public int alarmStatus;

    public boolean isFrontLedOn() {
        return isOnCondition(0);
    }

    public boolean isOnCondition(int n) {
        return (alarmStatus & (1 << n)) == (1 << n);

    }

    public boolean isOffCondition(int n) {
        return (alarmStatus & (1 << n)) != (1 << n);
    }

    public boolean isLockCanOff() {
        return isOnCondition(2);
    }

    public boolean isLockCanWarn() {
        return isOnCondition(3);
    }

    public boolean isLockNotWarn() {
        return isOffCondition(3);
    }

}
