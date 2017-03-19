package com.blue.car.model;

import com.blue.car.service.BlueUtils;

public class LedCommandResp {
    public int ledMode;
    public int[] ledColor = new int[4];

    public boolean isFrontLedOpen() {
        return BlueUtils.isOnCondition(ledMode, 0);
    }

    public boolean isBrakeLedOpen() {
        return BlueUtils.isOnCondition(ledMode, 1);
    }
}
