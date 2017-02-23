package com.blue.car.events;

import java.util.UUID;

public class GattCharacteristicReadEvent {

    public int status;
    public UUID uuid;
    public byte[] data;
}
