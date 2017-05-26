package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;
import com.ustadmobile.test.sharedse.impl.TestContext;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class MockNetworkManager extends NetworkManager {

    protected Vector<MockRemoteDevice> mockRemoteDevices;

    private String mockBluetoothAddr;

    private String mockIpAddr;

    private MockWirelessArea wirelessArea;

    private Timer wifiDirectBroadcastTimer;

    public static final int WIFI_DIRECT_BROADCAST_INTERVAL = (30*1000);//Broadcast service records every 30s

    public static final int WIFI_DIRECT_BROADCAST_DELAY = 1000;

    public static final String TMP_MOCK_WIFIDIRECT_MAC = "01:00:00:00:00:00";


    class WifiDirectBroadcastTimerTask extends TimerTask{

        private HashMap<String, String> txtRecords;

        WifiDirectBroadcastTimerTask(HashMap<String, String> txtRecords) {
            this.txtRecords = txtRecords;
        }

        @Override
        public void run() {
            MockNetworkManager.this.wirelessArea.sendSdTxtRecords(CoreBuildConfig.NETWORK_SERVICE_NAME,
                    txtRecords, MockNetworkManager.this);
        }
    };

    public MockNetworkManager(String bluetoothAddr, MockWirelessArea wirelessArea) {
        mockRemoteDevices = new Vector<>();
        this.mockBluetoothAddr = bluetoothAddr;
        this.wirelessArea = wirelessArea;
    }

    @Override
    public void init(Object mContext) {
        super.init(mContext);
    }

    @Override
    public void handleEntriesStatusUpdate(NetworkNode node, List<String> fileIds, List<Boolean> status) {
        super.handleEntriesStatusUpdate(node, fileIds, status);
    }

    public MockRemoteDevice addMockTestDriver(String bluetoothAddr) {
        Object mockRemoteContext = new TestContext();
        MockNetworkManager driverNetworkManager = new MockNetworkManager(bluetoothAddr, wirelessArea);
        MockRemoteDevice remoteDevice = addMockRemoteDevice(bluetoothAddr, driverNetworkManager,
                mockRemoteContext);
        remoteDevice.startTestControlServer();

        return remoteDevice;
    }




    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
        if(enabled && wifiDirectBroadcastTimer == null) {
            startSuperNode();
        }else if(!enabled && wifiDirectBroadcastTimer != null) {
            stopSuperNode();
        }
    }

    @Override
    public synchronized void startSuperNode() {
        if(wifiDirectBroadcastTimer == null) {
            HashMap<String, String> txtRecords = new HashMap<>();
            txtRecords.put(NetworkManager.SD_TXT_KEY_BT_MAC, mockBluetoothAddr);
            txtRecords.put(NetworkManager.SD_TXT_KEY_IP_ADDR,
                    MockNetworkManager.this.getDeviceIPAddress());
            txtRecords.put(NetworkManager.SD_TXT_KEY_PORT, String.valueOf(getHttpListeningPort()));
            wifiDirectBroadcastTimer = new Timer();
            wifiDirectBroadcastTimer.scheduleAtFixedRate(new WifiDirectBroadcastTimerTask(txtRecords),
                    WIFI_DIRECT_BROADCAST_DELAY, WIFI_DIRECT_BROADCAST_INTERVAL);
        }
    }

    @Override
    public synchronized void stopSuperNode() {
        if(wifiDirectBroadcastTimer != null) {
            wifiDirectBroadcastTimer.cancel();
            wifiDirectBroadcastTimer = null;
        }
    }

    @Override
    public synchronized boolean isSuperNodeEnabled() {
        return wifiDirectBroadcastTimer != null;
    }



    @Override
    public boolean isBluetoothEnabled() {
        return true;
    }

    @Override
    public BluetoothServer getBluetoothServer() {
        return bluetoothServer;
    }

    @Override
    public boolean isWiFiEnabled() {
        return true;
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
    public int addNotification(int notificationType, String title, String message) {
        return 0;
    }

    @Override
    public void updateNotification(int notificationId, int progress, String title, String message) {

    }

    @Override
    public void removeNotification(int notificationId) {

    }

    @Override
    public String getDeviceIPAddress() {
        return mockIpAddr;
    }


    public void setMockDeviceIpAddress(String mockIpAddr) {
        this.mockIpAddr = mockIpAddr;
    }

    /**
     *
     * @param bluetoothAddr
     * @param context
     */
    public MockRemoteDevice addMockRemoteDevice(String bluetoothAddr, MockNetworkManager manager, Object context) {
        MockRemoteDevice remoteDevice = new MockRemoteDevice(bluetoothAddr, wirelessArea, manager, context);
        mockRemoteDevices.add(remoteDevice);
        return remoteDevice;
    }

    public MockWirelessArea getWirelessArea() {
        return wirelessArea;
    }

    public void setWirelessArea(MockWirelessArea wirelessArea) {
        this.wirelessArea = wirelessArea;
    }

    public String getWifiDirectMacAddr() {
        return TMP_MOCK_WIFIDIRECT_MAC;
    }

    @Override
    public void createWifiDirectGroup() {

    }

    @Override
    public void removeWiFiDirectGroup() {

    }

    @Override
    public WiFiDirectGroup getWifiDirectGroup() {
        return null;
    }

    @Override
    public String getWifiDirectIpAddress() {
        return null;
    }

    @Override
    public int getWifiDirectGroupStatus() {
        return 0;
    }

    @Override
    public void disconnectBluetooth() {

    }

    @Override
    public void connectWifi(String SSID, String passPhrase) {

    }
}
