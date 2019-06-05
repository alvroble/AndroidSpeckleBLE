package com.uc3m.Speckle_BLE;
import java.util.UUID;

public class HR_Profile {

    // UUID for the UART BTLE client characteristic which is necessary for notifications.
    //public static UUID DESCRIPTOR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //public static UUID SERVICE_HR_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    //public static UUID CHARACTERISTIC_HR_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    //public static UUID CHARACTERISTIC_CONTROL_UUID = UUID.fromString("00002a3a-0000-1000-8000-00805f9b34fb");
    //public static final byte[] COMMAND_START = {(byte)10 & 0xFF};
    //public static final byte[] COMMAND_STOP  = {(byte)20 & 0xFF};

    public static UUID BATTERY_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_LEVEL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

}