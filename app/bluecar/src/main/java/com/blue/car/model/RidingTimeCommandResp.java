package com.blue.car.model;

public class RidingTimeCommandResp {
    /*
    本次骑行时间：单位S，2字节，转化为时秒分
    总骑行时间：单位S ，4字节，转化为时秒分
    */
    public int totalRunningTime;
    public int totalRidingTime;
    public int totalPowerTime;
    public int totalRemoteTime;
    public int perRunningTime;
    public int perRidingTime;
    public int perPowerTime;
    public int perRemoteTime;
}
