package com.blue.car.model;

public class BatteryInfoCommandResp {

    public int remainBatteryElectricity;
    public int remainPercent;
    public float electricCurrent;
    public float voltage;
    public int temperature;
    public byte[] remainForFuture = new byte[10];
    public int state;
}
