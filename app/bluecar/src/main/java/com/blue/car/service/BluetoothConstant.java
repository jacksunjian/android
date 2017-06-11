package com.blue.car.service;

import java.util.UUID;

public class BluetoothConstant {
    public static final boolean USE_DEBUG = false;
    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRAS_DEVICE_NAME = "device_name";

    public final static String UUID_STRING_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String UUID_STRING_CHARACTER_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String UUID_STRING_CHARACTER_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String UUID_STRING_CHARACTER_DESC = "00002902-0000-1000-8000-00805f9b34fb";

    public final static UUID UUID_SERVICE = UUID.fromString(UUID_STRING_SERVICE);
    public final static UUID UUID_CHARACTER_TX = UUID.fromString(UUID_STRING_CHARACTER_TX);
    public final static UUID UUID_CHARACTER_RX = UUID.fromString(UUID_STRING_CHARACTER_RX);
    public static final UUID UUID_CHARACTER_DESC = UUID.fromString(UUID_STRING_CHARACTER_DESC);
}
