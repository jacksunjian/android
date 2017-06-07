package com.blue.car.model;

import com.blue.car.utils.StringUtils;

public class FirstStartCommandResp {
    public String carId; //要求前6位必须是11960/ 或者 201702
    public String blePassword;

    public int boardVersion;
    public int errCode;
    public int warningCode;
    public int sysStatus;
    public byte[] remain;

    public boolean isCardIdValid() {
        if (StringUtils.isNullOrEmpty(carId)) {
            return false;
        }
        return carId.startsWith("11960") || carId.startsWith("201702");
    }

    public boolean isEmptyPwd() {
        return StringUtils.isNullOrEmpty(blePassword) || "000000".equals(blePassword);
    }
}
