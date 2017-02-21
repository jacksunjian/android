package com.blue.car.model;

/**
 * Created by suicheng on 2017/2/20.
 */

public class FirstStartCommandResp {
    public String carId; //要求前6位必须是11960/ 或者 201702
    public String blePassword;

    public int boardVersion;
    public int errCode;
    public int warningCode;
    public int sysStatus;
    public byte[] remain;
}
