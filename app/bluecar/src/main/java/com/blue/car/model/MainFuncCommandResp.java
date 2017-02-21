package com.blue.car.model;

/**
 * Created by suicheng on 2017/2/20.
 */

public class MainFuncCommandResp {
    public int errCode;
    public int warningCode;
    public int sysStatus;
    public int workMode;
    public int remainBatteryElectricity;
    public int speed;
    public int averageSpeed;
    public int totalMileage;
    public int perMileage;
    public int perRunTime;
    public int temperature;
    public int speedLimit;
    public int electricCurrent;
    public byte[] remain = new byte[2];
    public int maxAbsSpeed;
}
