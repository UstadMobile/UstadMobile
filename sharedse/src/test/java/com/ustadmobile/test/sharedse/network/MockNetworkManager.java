package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MockNetworkManager can represent the device running the tests itself AND other devices on a
 * mock network.
 *
 * Created by kileha3 on 10/05/2017.
 */
public class MockNetworkManager extends NetworkManager {

    private String mockBluetoothAddr;

    private String mockIpAddr;

    private String mockWifiDirectIpAddr;

    private MockWirelessArea wirelessArea;

    private Timer wifiDirectBroadcastTimer;

    private Timer wifiNetworkServiceBroadcastTimer;

    public static final int WIFI_DIRECT_BROADCAST_INTERVAL = (30*1000);//Broadcast service records every 30s

    public static final int WIFI_DIRECT_BROADCAST_DELAY = 1000;

    public static final int MOCK_GROUP_CREATION_DELAY = 1000;

    public static final int MOCK_GROUP_REMOVAL_DELAY = 1000;

    public static final String TMP_MOCK_WIFIDIRECT_MAC = "01:00:00:00:00:00";

    private static AtomicInteger macAddrCounter = new AtomicInteger(1);

    private static AtomicInteger ipAddrCounter = new AtomicInteger(10);

    public static final String MOCK_WIRELESS_DEFAULT_WIRELESS_SSID = "mocknet";

    public static final String MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE = "mock";

    private RemoteTestServerHttpd mockRemoteDeviceControlHttpd;

    private MockBluetoothServer mockBluetoothServer;

    private MockWifiNetwork connectedWifiNetwork;

    private final Object wifiLockObj = new Object();

    private static AtomicInteger mockNameCounter = new AtomicInteger();

    private String mockDeviceName;

    private AtomicInteger mockNetworkCounter = new AtomicInteger();

    private static SecureRandom passphraseSecureRandom = new SecureRandom();

    private MockWifiDirectGroup mockWifiDirectGroup;

    private MockWifiNetwork mockWifiDirectGroupNetwork;

    private int mockWifiDirectStatus = WIFI_DIRECT_GROUP_STATUS_INACTIVE;

    public static final int MOCK_WIFI_CONNECTION_DELAY = 1000;

    private List<String> temporaryWifiDirectSsids = new ArrayList<>();

    private HashMap<String, String> savedNetworks = new HashMap<>();

    private static final AtomicInteger connectionChangeAtomicInteger = new AtomicInteger();

    private volatile boolean wifiDirectDiscoveryEnabled;

    private boolean networkServiecDiscoveryEnabled;

    private boolean supernodeEnabled = false;

    private String wifiDirectMac;

    private Timer wifiDirectNodeConnectionTimer = new Timer();

    private Vector<TimerTask> wifiDirectNodeConnectionPendingTasks = new Vector<>();


    class WifiDirectBroadcastTimerTask extends TimerTask{

        private HashMap<String, String> txtRecords;

        WifiDirectBroadcastTimerTask(HashMap<String, String> txtRecords) {
            this.txtRecords = txtRecords;
        }

        @Override
        public void run() {
            MockNetworkManager.this.wirelessArea.sendSdTxtRecords(CoreBuildConfig.WIFI_P2P_INSTANCE_NAME,
                    txtRecords, MockNetworkManager.this);
        }
    };

    class WifiNetworkBroadcastTimer extends TimerTask {
        public void run() {
            synchronized (wifiLockObj) {
                if (MockNetworkManager.this.connectedWifiNetwork != null) {
                    MockNetworkManager.this.connectedWifiNetwork.sendWirelessServiceBroadcast(
                        mockDeviceName, MockNetworkManager.this.getDeviceIPAddress(),
                        MockNetworkManager.this.getHttpListeningPort());
                }
            }
        }
    }


    public MockNetworkManager(String bluetoothAddr, MockWirelessArea wirelessArea, String mockDeviceName) {
        this.mockDeviceName = mockDeviceName != null ? mockDeviceName : "MockDevice-" +
                mockNameCounter.getAndIncrement();
        this.mockBluetoothAddr = bluetoothAddr;
        this.wirelessArea = wirelessArea;
        mockBluetoothServer = new MockBluetoothServer(this);
        wirelessArea.addDevice(this);
    }

    public MockNetworkManager(String bluetoothAddr, MockWirelessArea wirelessArea) {
        this(bluetoothAddr, wirelessArea, null);
    }

    @Override
    public void init(Object mContext) {
        super.init(mContext);
        updateClientServices();
        updateSupernodeServices();
    }

    public void startTestControlServer() {
        mockRemoteDeviceControlHttpd = new RemoteTestServerHttpd(0, this);
        try {
            mockRemoteDeviceControlHttpd.start();
        }catch(IOException e) {
            throw  new RuntimeException(e);
        }
    }

    public int getTestControlServerPort() {
        return mockRemoteDeviceControlHttpd.getListeningPort();
    }

    public void stopTestControlServer() {
        if(mockRemoteDeviceControlHttpd != null) {
            mockRemoteDeviceControlHttpd.stop();
        }
    }

    public MockBluetoothServer getMockBluetoothServer() {
        return mockBluetoothServer;
    }

    /**
     * Utility method to generate mock ip addresses which are unique: but in reality are all loopback
     * IP addresses.
     *
     * @return A new unique IP address somewhere on the between 127.0.0.2 and 127.254.254.254
     */
    public static String makeNextMockIpAddr() {
        int nextInt = ipAddrCounter.getAndIncrement();
        int[] ipSections = new int[3];
        int remainder = nextInt;
        int colVal;
        String ipAddr = "127.";
        for(int i = ipSections.length-1; i >= 0; i--) {
            colVal = (int)Math.pow(255, i);
            ipSections[i] = remainder / colVal;
            remainder %= colVal;
            ipAddr += ipSections[i];
            if(i > 0)
                ipAddr += '.';
        }

        return ipAddr;
    }

    @Override
    public void handleEntriesStatusUpdate(NetworkNode node, List<String> fileIds, List<Boolean> status) {
        super.handleEntriesStatusUpdate(node, fileIds, status);
    }

    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
        this.supernodeEnabled = enabled;
        updateClientServices();
        updateSupernodeServices();
    }

    @Override
    public void updateSupernodeServices() {
        boolean shouldHaveLocalP2PService = true;
        if(shouldHaveLocalP2PService && wifiDirectBroadcastTimer == null) {
            HashMap<String, String> txtRecords = new HashMap<>();
            txtRecords.put(NetworkManager.SD_TXT_KEY_BT_MAC, mockBluetoothAddr);
            txtRecords.put(NetworkManager.SD_TXT_KEY_IP_ADDR,
                    MockNetworkManager.this.getDeviceIPAddress());
            txtRecords.put(NetworkManager.SD_TXT_KEY_PORT, String.valueOf(getHttpListeningPort()));
            wifiDirectBroadcastTimer = new Timer();
            wifiDirectBroadcastTimer.scheduleAtFixedRate(new WifiDirectBroadcastTimerTask(txtRecords),
                    WIFI_DIRECT_BROADCAST_DELAY, WIFI_DIRECT_BROADCAST_INTERVAL);
        }else if(!shouldHaveLocalP2PService && wifiDirectBroadcastTimer != null) {
            wifiDirectBroadcastTimer.cancel();
            wifiDirectBroadcastTimer = null;
        }
    }

    @Override
    public void updateClientServices() {
        boolean shouldRunP2PDiscovery = true;

        if(shouldRunP2PDiscovery && !isWifiDirectDiscoveryEnabled()) {
            setWifiDirectDiscoveryEnabled(true);
        }else if(!shouldRunP2PDiscovery && isWifiDirectDiscoveryEnabled()){
            setWifiDirectDiscoveryEnabled(false);
        }

        boolean shouldRunNsd = true;
        if(shouldRunNsd && !isNetworkServiecDiscoveryEnabled()) {
            setNetworkServiecDiscoveryEnabled(true);
        }else if(!shouldRunNsd && isNetworkServiecDiscoveryEnabled()) {
            setNetworkServiecDiscoveryEnabled(false);
        }
    }


    @Override
    public synchronized boolean isSuperNodeEnabled() {
        return supernodeEnabled;
    }

    @Override
    public boolean isBroadcastEnabled() {
        //TODO: implement
        return true;
    }

    @Override
    public boolean isDiscoveryEnabled() {
        //TODO: implement
        return true;
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
        MockNetworkManager remoteDevice = wirelessArea.getDeviceByBluetoothAddr(deviceAddress);
        if(remoteDevice != null){
            PipedOutputStream outToServer = new PipedOutputStream();
            PipedInputStream inFromServer = new PipedInputStream();
            remoteDevice.getMockBluetoothServer().connectMockClient(mockBluetoothAddr,
                    inFromServer, outToServer);
            handler.onBluetoothConnected(inFromServer, outToServer);
            return;
        }else {
            handler.onBluetoothConnectionFailed(new IOException("No such mock bluetooth device"));
        }
    }



    @Override
    public int addNotification(int notificationType, String title, String message) {
        return 0;
    }

    @Override
    public void updateNotification(int notificationType, int progress, String title, String message) {

    }

    @Override
    public void removeNotification(int notificationType) {

    }

    @Override
    public String getDeviceIPAddress() {
        return mockIpAddr;
    }


    public void setMockDeviceIpAddress(String mockIpAddr) {
        this.mockIpAddr = mockIpAddr;
    }



    public MockWirelessArea getWirelessArea() {
        return wirelessArea;
    }

    public void setWirelessArea(MockWirelessArea wirelessArea) {
        this.wirelessArea = wirelessArea;
    }

    public String getWifiDirectMacAddr() {
        if(wifiDirectMac == null) {
            wifiDirectMac = Integer.toHexString(macAddrCounter.getAndIncrement());
        }

        return wifiDirectMac;
    }

    public void setWifiDirectMacAddr(String wifiDirectMacAddr) {
        this.wifiDirectMac = wifiDirectMacAddr;
    }

    @Override
    public void createWifiDirectGroup() {
        mockWifiDirectStatus = WIFI_DIRECT_GROUP_STATUS_UNDER_CREATION;
        new Thread(new Runnable() {
            @Override
            public void run() {
                formNewMockWifiDirectGroup();
                fireWifiDirectGroupCreated(MockNetworkManager.this.mockWifiDirectGroup, null);
            }
        }).start();
    }

    private void formNewMockWifiDirectGroup() {
        String wirelessId = "DIRECT-" + MockNetworkManager.this.mockDeviceName + '-' +
                MockNetworkManager.this.mockNetworkCounter.getAndIncrement();
        String passphrase = new BigInteger(130, passphraseSecureRandom).toString(32);
        mockWifiDirectGroup = new MockWifiDirectGroup(this, wirelessId, passphrase);
        mockWifiDirectIpAddr = MockNetworkManager.makeNextMockIpAddr();
        mockWifiDirectGroupNetwork = new MockWifiNetwork(wirelessId,passphrase);
        mockWifiDirectStatus = WIFI_DIRECT_GROUP_STATUS_ACTIVE;
        wirelessArea.addWifiNetwork(mockWifiDirectGroupNetwork);
        mockWifiDirectGroup.setOwner(true);
    }

    @Override
    public void removeWiFiDirectGroup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(MockNetworkManager.this.mockWifiDirectGroup != null) {
                    //TODO: disconnect all clients of this network if there are any left
                    wirelessArea.removeWifiNetwork(mockWifiDirectGroupNetwork);
                    mockWifiDirectGroup = null;
                    mockWifiDirectStatus = WIFI_DIRECT_GROUP_STATUS_INACTIVE;

                    fireWifiDirectGroupRemoved(true, null);
                }
            }
        }).start();
    }

    @Override
    public WiFiDirectGroup getWifiDirectGroup() {
        if(!isMangleWifiDirectGroup() || mockWifiDirectGroup == null) {
            return mockWifiDirectGroup;
        }else {
            return new WiFiDirectGroup(mockWifiDirectGroup.getSsid() + "-mangle", mockWifiDirectGroup.getPassphrase());
        }
    }

    public void setWifiDirectGroup(MockWifiDirectGroup group) {
        this.mockWifiDirectGroup = group;
    }

    @Override
    public String getWifiDirectIpAddress() {
        return mockWifiDirectIpAddr;
    }

    @Override
    public int getWifiDirectGroupStatus() {
        return mockWifiDirectStatus;
    }

    @Override
    public void connectWifi(final String connectSsid, final String passPhrase) {
        final int connectNum = connectionChangeAtomicInteger.getAndIncrement();
        UstadMobileSystemImpl.l(UMLog.INFO, 323, "Mock network manager: ("+connectNum+") request connection to: " +
                connectSsid + " passphrase " + passPhrase);
        new Thread(new Runnable() {
            @Override
            public void run() {
                UstadMobileSystemImpl.l(UMLog.INFO, 323, "Mock network manager: ("+connectNum+") request connection to: " +
                        connectSsid + " passphrase " + passPhrase+ " ... waiting");
                try { Thread.sleep(MOCK_WIFI_CONNECTION_DELAY ); }
                catch(InterruptedException e) {}
                synchronized (wifiLockObj) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 323, "Mock network manager: ("+connectNum+") request connection to: " +
                            connectSsid + " passphrase " + passPhrase+ " ... continue");

                    if(wifiNetworkServiceBroadcastTimer != null){
                        wifiNetworkServiceBroadcastTimer.cancel();
                        wifiNetworkServiceBroadcastTimer = null;
                    }

                    if(connectedWifiNetwork != null) {
                        connectedWifiNetwork.disconnect(MockNetworkManager.this);
                    }

                    mockIpAddr = wirelessArea.connectDeviceToWifiNetwork(MockNetworkManager.this,
                            connectSsid, passPhrase);
                    UstadMobileSystemImpl.l(UMLog.INFO, 324, "Mock network manager ("+connectNum+") got IP address: "
                            + mockIpAddr + " on network: " + connectSsid);
                    if(mockIpAddr != null) {
                        UstadMobileSystemImpl.l(UMLog.INFO, 322, "Mock network manager ("+connectNum+") connected to: " +
                                connectSsid);
                        savedNetworks.put(connectSsid, passPhrase);
                        connectedWifiNetwork = wirelessArea.getWifiNetwork(connectSsid);
                        wifiNetworkServiceBroadcastTimer = new Timer();
                        wifiNetworkServiceBroadcastTimer.scheduleAtFixedRate(new WifiNetworkBroadcastTimer(),
                                WIFI_DIRECT_BROADCAST_DELAY, WIFI_DIRECT_BROADCAST_INTERVAL);
                        handleWifiConnectionChanged(connectSsid, true, true);
                    }
                }
            }
        }).start();
    }

    @Override
    public void connectToWifiDirectGroup(String ssid, String passphrase) {
        temporaryWifiDirectSsids.add(ssid);
        super.connectToWifiDirectGroup(ssid, passphrase);
    }

    @Override
    public void restoreWifi() {
        //delete the temporary wifi ssid from the list
        Iterator<String> ssidIterator = temporaryWifiDirectSsids.iterator();
        String nextSsid;
        while(ssidIterator.hasNext()) {
            nextSsid = ssidIterator.next();
            savedNetworks.remove(nextSsid);
            ssidIterator.remove();
        }

        Iterator<String> knownNetworks = savedNetworks.keySet().iterator();
        while(knownNetworks.hasNext()) {
            nextSsid = knownNetworks.next();
            if(wirelessArea.getWifiNetwork(nextSsid) != null){
                //try and connect to it
                connectWifi(nextSsid, savedNetworks.get(nextSsid));
                return;
            }
        }
    }

    @Override
    public void disconnectWifi() {
        //TODO: implement me
    }

    @Override
    public String getCurrentWifiSsid() {
        if(connectedWifiNetwork != null) {
            return connectedWifiNetwork.getSsid();
        }else{
            return null;
        }

    }

    public String getMockBluetoothAddr() {
        return !isMangleBluetoothAddr() ? mockBluetoothAddr : "00:11:22:33:44:55";
    }


    class MockBluetoothServer extends BluetoothServer implements Runnable {

        private PipedInputStream serverIn;

        private PipedOutputStream serverOut;

        private String remoteAddr;

        public MockBluetoothServer(NetworkManager manager) {
            super(manager);
        }

        protected void connectMockClient(String remoteAddr, PipedInputStream clientIn, PipedOutputStream clientOut) {
            try {
                serverIn = new PipedInputStream();
                serverIn.connect(clientOut);
                serverOut = new PipedOutputStream();
                serverOut.connect(clientIn);
                this.remoteAddr = remoteAddr;
                new Thread(this).start();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void start() {

        }

        public void run() {
            try {
                handleNodeConnected(remoteAddr, serverIn, serverOut);
                serverIn.close();
                serverOut.close();
                remoteAddr = null;
            }catch(IOException e) {

            }
        }


        @Override
        public void stop() {

        }

    }

    public boolean isWifiDirectDiscoveryEnabled() {
        return wifiDirectDiscoveryEnabled;
    }

    public void setWifiDirectDiscoveryEnabled(boolean wifiDirectDiscoveryEnabled) {
        this.wifiDirectDiscoveryEnabled = wifiDirectDiscoveryEnabled;
    }

    public boolean isNetworkServiecDiscoveryEnabled() {
        return networkServiecDiscoveryEnabled;
    }

    public void setNetworkServiecDiscoveryEnabled(boolean networkServiecDiscoveryEnabled) {
        this.networkServiecDiscoveryEnabled = networkServiecDiscoveryEnabled;
    }

    @Override
    public void shareAppSetupFile(String filePath, String shareTitle) {
        //TODO: implement this.test
    }

    @Override
    public boolean setBluetoothEnabled(boolean enabled) {
        return false;
    }

    @Override
    public boolean setWifiEnabled(boolean enabled) {
        return false;
    }

    @Override
    public int getWifiConnectionTimeout() {
        return 20000;
    }

    @Override
    public void connectToWifiDirectNode(final String deviceAddress) {
        TimerTask connectTask = new TimerTask(){
            @Override
            public void run() {
                wifiDirectNodeConnectionPendingTasks.remove(this);
                MockNetworkManager otherNode = wirelessArea.getDeviceByWifiDirectMacAddr(deviceAddress);
                if(otherNode == null) {
                    return;
                }

                if(mockWifiDirectGroup == null) {
                    formNewMockWifiDirectGroup();
                }

                mockWifiDirectGroup.addClient(otherNode);
                otherNode.fireWifiDirectGroupCreated(mockWifiDirectGroup, null);
                fireWifiDirectGroupCreated(MockNetworkManager.this.mockWifiDirectGroup, null);

                fireWifiP2pConnectionChanged(true);
                otherNode.fireWifiP2pConnectionChanged(true);
            }
        };
        wifiDirectNodeConnectionPendingTasks.add(connectTask);
        wifiDirectNodeConnectionTimer.schedule(connectTask, 1000);
    }

    @Override
    public NetworkNode getThisWifiDirectDevice() {
        NetworkNode node = new NetworkNode(getWifiDirectMacAddr(), null);
        node.setDeviceWifiDirectName(mockDeviceName);
        return node;
    }

    @Override
    public String getWifiDirectGroupOwnerIp() {
        if(mockWifiDirectGroup != null) {
            return mockWifiDirectGroup.getGroupOwnerIp();
        }else {
            return null;
        }
    }

    @Override
    public boolean isWifiDirectConnectionEstablished(String otherDevice) {
        return false;
    }

    @Override
    public void cancelWifiDirectConnection() {
        synchronized (wifiDirectNodeConnectionPendingTasks) {
            Iterator<TimerTask> iterator = wifiDirectNodeConnectionPendingTasks.iterator();
            TimerTask task;
            while(iterator.hasNext()) {
                task = iterator.next();
                task.cancel();
                iterator.remove();
            }
            wifiDirectNodeConnectionPendingTasks.clear();
        }
    }
}
