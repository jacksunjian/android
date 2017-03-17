package com.blue.car.model;

public class BlackBoxCommandResp {
    //4字节时间+2字节代码+2字节附加信息
    public long time;
    public int code;
    public int additional;
}
