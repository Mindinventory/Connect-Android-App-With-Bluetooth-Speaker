// IBluetooth.aidl
package com.example.bluetoothheadsetconnectivity;

interface IBluetooth {
    /**
     * System private API for Bluetooth service
     */
    String getRemoteAlias(in String address);
      boolean setRemoteAlias(in String address, in String name);
}
