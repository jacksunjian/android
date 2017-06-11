package com.blue.car.events;

public class GattConnectStatusEvent {
    //1 :connect 0:disconnect
    public int status;

    public boolean isDisconnected() {
        return status == 0;
    }
}
