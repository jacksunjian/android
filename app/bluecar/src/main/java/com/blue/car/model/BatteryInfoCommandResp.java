package com.blue.car.model;

/**
 * Created by suicheng on 2017/2/21.
 */
public class BatteryInfoCommandResp {

    public int remainBatteryElectricity;
    public int remainPercent;
    public int electricCurrent;
    public int voltage;
    public int temperature;
    public byte[] remainForFuture = new byte[10];
    public int state;
}
