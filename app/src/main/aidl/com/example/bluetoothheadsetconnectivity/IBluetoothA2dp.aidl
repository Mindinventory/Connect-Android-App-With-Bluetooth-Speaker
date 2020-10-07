// IBluetoothA2dp.aidl
package com.example.bluetoothheadsetconnectivity;

interface IBluetoothA2dp {
    /**
     * System private API for Bluetooth A2DP service
     */
     boolean connectSink(in BluetoothDevice device); // Pre API 11 only
       boolean disconnectSink(in BluetoothDevice device); // Pre API 11 only
       boolean connect(in BluetoothDevice device); // API 11 and up only
       boolean disconnect(in BluetoothDevice device); // API 11 and up only
       boolean suspendSink(in BluetoothDevice device); // all
       boolean resumeSink(in BluetoothDevice device); // all
       BluetoothDevice[] getConnectedSinks();  // change to Set<> once AIDL supports, pre API 11 only
       BluetoothDevice[] getNonDisconnectedSinks();  // change to Set<> once AIDL supports,
       int getSinkState(in BluetoothDevice device); // pre API 11 only
       int getConnectionState(in BluetoothDevice device); // API 11 and up
       boolean setSinkPriority(in BluetoothDevice device, int priority); // Pre API 11 only
       boolean setPriority(in BluetoothDevice device, int priority); // API 11 and up only
       int getPriority(in BluetoothDevice device); // API 11 and up only
       int getSinkPriority(in BluetoothDevice device); // Pre API 11 only
       boolean isA2dpPlaying(in BluetoothDevice device); // API 11 and up only
}
