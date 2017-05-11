package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class MockNetworkManager extends NetworkManager {


    protected BluetoothServer bluetoothServer;

    private HashMap<String, MockBluetoothServer> mockBluetoothServers;



    public MockNetworkManager() {
        mockBluetoothServers = new HashMap<>();
        bluetoothServer = new MockBluetoothServer();
    }

    public void setMockBluetoothServer(String macAddr, MockBluetoothServer server) {
        mockBluetoothServers.put(macAddr, server);
    }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {

    }

    @Override
    public void startSuperNode() {

    }

    @Override
    public void stopSuperNode() {

    }

    @Override
    public boolean isSuperNodeEnabled() {
        return false;
    }

    @Override
    public void init(Object mContext, String serviceName) {

    }

    @Override
    public boolean isBluetoothEnabled() {
        return false;
    }

    @Override
    public BluetoothServer getBluetoothServer() {
        return bluetoothServer;
    }

    @Override
    public boolean isWiFiEnabled() {
        return false;
    }

    @Override
    public NetworkTask createFileStatusTask(List<String> entryIds, Object mContext) {
        return null;
    }


    @Override
    public NetworkTask createAcquisitionTask(UstadJSOPDSFeed feed, Object mContext) {
        return null;
    }

    @Override
    public void connectBluetooth(String deviceAddress, BluetoothConnectionHandler handler) {
        if(mockBluetoothServers.containsKey(deviceAddress)) {
            handler.onConnected(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream());
        }
    }

    @Override
    public void handleEntriesStatusUpdate(NetworkNode node, String[] fileIds, boolean[] status) {

    }

    @Override
    public int addNotification(int notificationType, String title, String message) {
        return 0;
    }

    @Override
    public void updateNotification(int notificationId, int progress, String title, String message) {

    }

    @Override
    public void removeNotification(int notificationId) {

    }
}
