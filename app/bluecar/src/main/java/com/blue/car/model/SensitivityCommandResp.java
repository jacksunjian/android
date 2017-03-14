package com.blue.car.model;

public class SensitivityCommandResp {
    public int turningSensitivity;
    public int ridingSensitivity;
    public int balanceInPowerMode;
    public byte[] remainA;
    public int remainB;


    public boolean isAutoRidingSensityAdjust(){
        return ridingSensitivity == 101;
    }

    public boolean isAutoTurningSensityAdjust(){
        return turningSensitivity == 101;
    }

    public boolean isEnableRidingSeekBar(){return ridingSensitivity != 101;}

    public boolean isEnableTurningSeekBar(){return turningSensitivity != 101;}

}
