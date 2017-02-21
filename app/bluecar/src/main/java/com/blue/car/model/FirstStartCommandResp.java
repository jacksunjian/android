package com.blue.car.model;

public class FirstStartCommandResp {
    public String carId; //要求前6位必须是11960/ 或者 201702
    public String blePassword;

    public int boardVersion;
    public int errCode;
    public int warningCode;
    public int sysStatus;
    public byte[] remain;
}
