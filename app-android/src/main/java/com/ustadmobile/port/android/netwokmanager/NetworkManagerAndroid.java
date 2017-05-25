package com.ustadmobile.port.android.netwokmanager;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceData;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.core.buildconfig.CoreBuildConfig.NETWORK_SERVICE_NAME;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class NetworkManagerAndroid extends NetworkManager{

    public static final String TAG="NetworkManagerAndroid";

    private Map<Context, ServiceConnection> serviceConnectionMap;


    private static  Long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    private final static int WAITING_TIME_TO_UPDATE = 500;

    private static final String DEFAULT_BLUETOOTH_ADDRESS="02:00:00:00:00:00";

    public static final String DEVICE_BLUETOOTH_ADDRESS = "bluetooth_address";

    public static final String PREF_KEY_SUPERNODE = "supernode_enabled";

    private static final String SERVICE_DEVICE_AVAILABILITY = "av";

    private boolean isSuperNodeEnabled=false;

    private BluetoothServerAndroid bluetoothServerAndroid;

    private NotificationManager mNotifyManager;

    private NotificationCompat.Builder mBuilder;

    private NetworkServiceAndroid networkService;

    private static final String serverNotificationTitle="Super Node Active";

    private static final String serverNotificationMessage ="You can share files with other devices";

    private WifiManager wifiManager;

    private ConnectivityManager connectivityManager;

    private NSDHelperAndroid nsdHelperAndroid;

    private int currentWifiDirectGroupStatus=-1;
    private BluetoothSocket bluetoothSocket=null;



    /**
     * All activities bind to NetworkServiceAndroid. NetworkServiceAndroid will call this init
     * method from it's onCreate
     *
     * @param context Context object: on Android always the NetworkServiceAndroid instance
     */
    @Override
    public void init(Object context) {
        networkService = (NetworkServiceAndroid)context;
        bluetoothServerAndroid=new BluetoothServerAndroid(this);
        nsdHelperAndroid=new NSDHelperAndroid(this);

        wifiManager= (WifiManager) networkService.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager= (ConnectivityManager) networkService.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        //listen for the intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_GROUP_CREATION_ACTION);
        LocalBroadcastManager.getInstance(networkService).registerReceiver(mBroadcastReceiver, filter);

        networkService.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(info.isConnected()){
                    handleWifiDirectConnectionChanged(wifiManager.getConnectionInfo().getSSID());
                }
            }
        },new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }


    /**
     * Broadcast receiver that simply receives broadcasts and passes to the SharedSE network manager
     */
    private BroadcastReceiver mBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(networkService,intent.getAction(),Toast.LENGTH_LONG).show();
            switch (intent.getAction()){

                case WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE:
                    String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                    DnsSdTxtRecord txtRecord = networkService.getWifiDirectHandlerAPI().
                            getDnsSdTxtRecordMap().get(deviceMac);
                    handleWifiDirectSdTxtRecordsAvailable(txtRecord.getFullDomain(),deviceMac, (HashMap<String, String>) txtRecord.getRecord());

                    break;
                case WifiDirectHandler.Action.NOPROMPT_GROUP_CREATION_ACTION:
                    boolean informationAvailable=networkService.getWifiDirectHandlerAPI().getWifiP2pGroup().isGroupOwner();
                    if(informationAvailable){
                        currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_ACTIVE;
                        WifiP2pGroup wifiP2pGroup=networkService.getWifiDirectHandlerAPI().getWifiP2pGroup();
                        handleWifiDirectGroupCreated(new WiFiDirectGroup(wifiP2pGroup.getNetworkName(),
                                wifiP2pGroup.getPassphrase()));
                    }
            }

        }
    };


    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
    }

    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
        if(isBluetoothEnabled() && isWiFiEnabled()){
            if(enabled){
                startSuperNode();
            }else{
                stopSuperNode();
            }
        }else{
            Log.d(TAG,"Either Bluetooth or WiFi is not enabled");
        }
    }

    @Override
    public void startSuperNode() {

       if(networkService.getWifiDirectHandlerAPI()!=null){
           WifiDirectHandler wifiDirectHandler = networkService.getWifiDirectHandlerAPI();
           wifiDirectHandler.stopServiceDiscovery();
           wifiDirectHandler.setStopDiscoveryAfterGroupFormed(false);
           wifiDirectHandler.addLocalService(NETWORK_SERVICE_NAME, localService());
           isSuperNodeEnabled=true;
           bluetoothServerAndroid.start();
           if(nsdHelperAndroid.isDiscoveringNetworkService()){
               nsdHelperAndroid.stopNSDiscovery();
           }
           nsdHelperAndroid.registerNSDService();
           addNotification(NOTIFICATION_TYPE_SERVER,serverNotificationTitle, serverNotificationMessage);
       }
    }

    @Override
    public void stopSuperNode() {
        WifiDirectHandler wifiDirectHandler = networkService.getWifiDirectHandlerAPI();
        if(wifiDirectHandler!=null){
            if(mBuilder!=null && mNotifyManager!=null){
                removeNotification(NOTIFICATION_TYPE_SERVER);
            }
            wifiDirectHandler.removeService();
            wifiDirectHandler.continuouslyDiscoverServices();
            nsdHelperAndroid.startNSDiscovery();
            isSuperNodeEnabled=false;
        }
    }


    @Override
    public boolean isSuperNodeEnabled() {
        return isSuperNodeEnabled;
    }


    @Override
    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    @Override
    public BluetoothServer getBluetoothServer() {
        return null;
    }

    @Override
    public boolean isWiFiEnabled() {
        return wifiManager.isWifiEnabled();
    }



    @Override
    public void connectBluetooth(String deviceAddress, final BluetoothConnectionHandler handler) {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bluetoothDevice=adapter.getRemoteDevice(deviceAddress);
        try{
            bluetoothSocket=bluetoothDevice.
                    createInsecureRfcommSocketToServiceRecord(BluetoothServer.SERVICE_UUID);

            bluetoothSocket.connect();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(bluetoothSocket.isConnected()){
                            handler.onConnected(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            if(bluetoothSocket!=null){
                try {
                    bluetoothSocket.close();
                    bluetoothSocket=null;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }


    }

    @Override
    public void disconnectBluetooth() {
        try {
           if(bluetoothSocket!=null){
               bluetoothSocket.close();
           }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int addNotification(int notificationType, String title, String message) {

        mNotifyManager = (NotificationManager) networkService.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(networkService);
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.launcher_icon);
        if(notificationType==NOTIFICATION_TYPE_ACQUISITION){
            mBuilder.setProgress(100, 0, false);
        }
        mBuilder.setOngoing(true);
        mNotifyManager.notify(notificationType, mBuilder.build());
        return notificationType;
    }

    @Override
    public void updateNotification(int notificationId,int progress, String title, String message) {
        Long time_now = Calendar.getInstance().getTimeInMillis();
        int max_progress_status=100;
        if(((time_now - TIME_PASSED_FOR_PROGRESS_UPDATE) < WAITING_TIME_TO_UPDATE) ||
                (progress < 0 && progress > max_progress_status)) {

            return;
        }
        TIME_PASSED_FOR_PROGRESS_UPDATE = time_now;
        mBuilder.setContentTitle(title)
                .setContentText(message);
        mBuilder.setProgress(100,Math.abs(progress), false);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void removeNotification(int notificationId) {
        mBuilder.setProgress(0, 0, false);
        mBuilder.setOngoing(false);
        mNotifyManager.notify(notificationId, mBuilder.build());
        mNotifyManager.cancel(notificationId);
    }


    /**
     * This constructs a map of DNS-Text records to be associated with the service
     * @return
     */
    private HashMap<String,String> localService(){
        boolean isConnected= connectivityManager!=null &&
                connectivityManager.getActiveNetworkInfo().getType()
                == ConnectivityManager.TYPE_WIFI;
        String deviceBluetoothMacAddress=getBluetoothMacAddress();
        String deviceIpAddress=isConnected ? getDeviceIPAddress():"";
        HashMap<String,String> record=new HashMap<>();
        record.put(SERVICE_DEVICE_AVAILABILITY,"available");
        record.put(SD_TXT_KEY_PORT,String.valueOf(SERVICE_PORT));
        record.put(SD_TXT_KEY_BT_MAC, deviceBluetoothMacAddress);
        record.put(SD_TXT_KEY_IP_ADDR, deviceIpAddress);
        return record;
    }

    /**
     * Get bluetooth address of the device, in android 6 and above this tends to return
     * default bluetooth address which is referenced as DEFAULT_BLUETOOTH_ADDRESS.
     * After getting this we have to resolve it to get the real bluetooth Address.
     * @return
     */
    public String getBluetoothMacAddress(){
        String address = BluetoothAdapter.getDefaultAdapter().getAddress();
        if (address.equals(DEFAULT_BLUETOOTH_ADDRESS)) {
            try {
                ContentResolver mContentResolver = networkService.getApplicationContext().getContentResolver();
                address = Settings.Secure.getString(mContentResolver, DEVICE_BLUETOOTH_ADDRESS);
                Log.d(WifiDirectHandler.TAG,"Bluetooth Address - Resolved: " + address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(WifiDirectHandler.TAG,"Bluetooth Address-No resolution: " + address);
        }
        return address;
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(networkService).unregisterReceiver(mBroadcastReceiver);
        if(bluetoothServerAndroid !=null){
            bluetoothServerAndroid.stop();
        }

        if(nsdHelperAndroid!=null){
            nsdHelperAndroid.unregisterNSDService();
        }

    }

    public Context getContext(){
        return networkService.getApplicationContext();
    }

    @Override
    public String getDeviceIPAddress() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()){
            if(info.getTypeName().equalsIgnoreCase("WIFI")) {
                try {
                    for (Enumeration<NetworkInterface> enumInterface = NetworkInterface.getNetworkInterfaces();
                         enumInterface.hasMoreElements();) {

                        NetworkInterface networkInterface = enumInterface.nextElement();
                        for (Enumeration<InetAddress> enumIPAddress = networkInterface.getInetAddresses();
                             enumIPAddress.hasMoreElements();) {
                            InetAddress inetAddress = enumIPAddress.nextElement();
                            if (!inetAddress.isLoopbackAddress()) {
                                String ipAddress ="";
                                if (inetAddress instanceof Inet4Address) {

                                    for (int i=0; i<inetAddress.getAddress().length; i++) {
                                        if (i > 0) {
                                            ipAddress += ".";
                                        }
                                        ipAddress += inetAddress.getAddress()[i]&0xFF;
                                    }
                                    return ipAddress;
                                }
                            }
                        }
                    }
                } catch (SocketException | NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public void connectWifi(String SSID, String passPhrase) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\""+ SSID +"\"";
        wifiConfig.priority=(getMaxConfigurationPriority(wifiManager)+1);
        wifiConfig.preSharedKey = "\""+ passPhrase +"\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    /**
     * Get maximum priority assigned to a network configuration.
     * This helps to prioritize which network to connect to.
     * @param wifiManager
     * @return
     */
    private int getMaxConfigurationPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        List<Integer> configPriorities=new ArrayList<>();
        for(final WifiConfiguration config : configurations) {
            configPriorities.add(config.priority);
        }
        Collections.sort(configPriorities);
        return configPriorities.get((configPriorities.size()-1));
    }

    @Override
    public void createWifiDirectGroup() {

        networkService.getWifiDirectHandlerAPI().setAddLocalServiceAfterGroupCreation(false);
        ServiceData serviceData= new ServiceData(NETWORK_SERVICE_NAME,SERVICE_PORT,
                new HashMap<String,String>() ,ServiceType.PRESENCE_TCP);
        networkService.getWifiDirectHandlerAPI().startAddingNoPromptService(serviceData, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_UNDER_CREATION;
            }

            @Override
            public void onFailure(int reason) {
                currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_INACTIVE;
            }
        });

    }

    @Override
    public void removeWiFiDirectGroup() {
        networkService.getWifiDirectHandlerAPI().removeGroup(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                handleWifiDirectGroupRemoved(true);
                currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_INACTIVE;
            }

            @Override
            public void onFailure(int reason) {
                handleWifiDirectGroupRemoved(false);
            }
        });
    }

    @Override
    public WiFiDirectGroup getWifiDirectGroup() {
        WifiP2pGroup groupInfo=networkService.getWifiDirectHandlerAPI().getWifiP2pGroup();
        if(groupInfo!=null){

           return new WiFiDirectGroup(groupInfo.getNetworkName(), groupInfo.getPassphrase());
        }else{
            return null;
        }
    }

    @Override
    public String getWifiDirectIpAddress() {
        WifiP2pInfo wifiP2pInfo=networkService.getWifiDirectHandlerAPI().getWifiP2pInfo();
        return String.valueOf(wifiP2pInfo.groupOwnerAddress);
    }

    @Override
    public int getWifiDirectGroupStatus() {
        return currentWifiDirectGroupStatus;
    }
}
