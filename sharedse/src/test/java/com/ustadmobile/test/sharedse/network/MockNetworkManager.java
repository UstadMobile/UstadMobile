package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class MockNetworkManager extends NetworkManager {

    protected Vector<MockRemoteDevice> mockRemoteDevices;

    private String mockBluetoothAddr;


    public MockNetworkManager(String bluetoothAddr) {
        mockRemoteDevices = new Vector<>();
        this.mockBluetoothAddr = bluetoothAddr;
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
        super.init(mContext, serviceName);
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
    public void connectBluetooth(String deviceAddress, BluetoothConnectionHandler handler) {
        for(int i = 0; i < mockRemoteDevices.size(); i++) {
            if(mockRemoteDevices.get(i).getBluetoothAddr().equals(deviceAddress)) {
                //"connect" to this
                PipedOutputStream outToServer = new PipedOutputStream();
                PipedInputStream inFromServer = new PipedInputStream();
                mockRemoteDevices.get(i).getMockBluetoothServer().connectMockClient(mockBluetoothAddr,
                        inFromServer, outToServer);
                handler.onConnected(inFromServer, outToServer);
                return;
            }
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


    /**
     *
     * @param bluetoothAddr
     * @param context
     */
    public void addMockRemoteDevice(String bluetoothAddr, Object context) {
        MockRemoteDevice remoteDevice = new MockRemoteDevice(bluetoothAddr, context);
        MockNetworkManager remoteNetworkManager = new MockNetworkManager(bluetoothAddr);
        remoteNetworkManager.init(context, TestConstants.TEST_NETWORK_SERVICE_NAME);
        mockRemoteDevices.add(remoteDevice);
        NetworkNode mockNode = new NetworkNode("wifidirectmac");
        mockNode.setDeviceBluetoothMacAddress(bluetoothAddr);
        handleNodeDiscovered(mockNode);



    }
}
