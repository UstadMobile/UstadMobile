package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.util.List;

import static android.content.Context.BLUETOOTH_SERVICE;

public class NetworkManagerAndroidBle extends NetworkManagerBle{

    private WifiManager wifiManager;

    private BluetoothManager bluetoothManager;

    @Override
    public void init(Object mContext) {
        wifiManager= (WifiManager) ((Context)mContext).getSystemService(Context.WIFI_SERVICE);
        bluetoothManager = (BluetoothManager)((Context)mContext).getSystemService(BLUETOOTH_SERVICE);
    }

    @Override
    public boolean isWiFiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    @Override
    public boolean setWifiEnabled(boolean enabled) {
        return wifiManager.setWifiEnabled(enabled);
    }

    @Override
    public void createWifiDirectGroup() {

    }

    @Override
    protected BleEntryStatusTask makeEntryStatusTask(Object context, List<Long> entryUidsToCheck, NetworkNode peerToCheck) {
        return new BleEntryStatusTaskAndroid((Context)context,entryUidsToCheck,peerToCheck);
    }

    public BluetoothManager getBluetoothManager(){
        return bluetoothManager;
    }
}
