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
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.ForeignCollection;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiUserEntity;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
 * <h1>NetworkManagerAndroid</h1>
 *
 * This is a class wrapper which defines all the platform dependent operation
 * on android platform. It is responsible for registering all network listeners, register all services,
 * getting right device address like MAC address and IP address, handle bluetooth
 * and WiFi direct connections e.t.c.
 *
 * @see com.ustadmobile.port.sharedse.networkmanager.NetworkManager
 *
 *
 * @author kileha3
 */



public class NetworkManagerAndroid extends NetworkManager{

    public static final String TAG="NetworkManagerAndroid";

    private Map<Context, ServiceConnection> serviceConnectionMap;

    private static  Long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    private final static int WAITING_TIME_TO_UPDATE = 500;

    private static final String DEFAULT_BLUETOOTH_ADDRESS="02:00:00:00:00:00";

    /**
     * Tag for the real device bluetooth address from
     * its adapter when default address is returned.
     */
    public static final String DEVICE_BLUETOOTH_ADDRESS = "bluetooth_address";

    /**
     * Shared preference key for the supernode status value.
     */
    public static final String PREF_KEY_SUPERNODE = "supernode_enabled";

    private static final String SERVICE_DEVICE_AVAILABILITY = "av";

    private int nodeStatus = -1;

    private static final int NODE_STATUS_SUPERNODE_RUNNING = 1;

    private static final int NODE_STATUS_CLIENT_RUNNING = 2;

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


    private int currentWifiDirectGroupStatus= WIFI_DIRECT_GROUP_STATUS_INACTIVE;

    private int previousConnectedNetId=-1;

    /**
     * Assets are served over http that are used to interact with the content (e.g. to inject a
     * javascript into the content that handles autoplay).
     */
    private String httpAndroidAssetsPath;

    /**
     * All activities bind to NetworkServiceAndroid. NetworkServiceAndroid will call this init
     * method from it's onCreate
     *
     * @param context Context object: on Android always the NetworkServiceAndroid instance
     */
    @Override
    public void init(Object context) {
        super.init(context);
        networkService = (NetworkServiceAndroid)context;
        bluetoothServerAndroid=new BluetoothServerAndroid(this);
        nsdHelperAndroid=new NSDHelperAndroid(this);

        wifiManager= (WifiManager) networkService.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager= (ConnectivityManager) networkService.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        /*Check if the device is currently connected to the WiFi ,
        if yes get network information and notify other part of the app.*/
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getTypeName().equalsIgnoreCase("WIFI")) {
                previousConnectedNetId=wifiManager.getConnectionInfo().getNetworkId();
            }
        }

        /*Register all network listeners*/
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_GROUP_CREATION_ACTION);
        LocalBroadcastManager.getInstance(networkService).registerReceiver(mBroadcastReceiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        networkService.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                boolean isConnected = info.isConnected();
                boolean isConnecting = info.isConnectedOrConnecting();
                String ssid = wifiManager.getConnectionInfo() != null ?
                        wifiManager.getConnectionInfo().getSSID() : null;
                ssid = ssid != null ? ssid.replace("\"", "") : null;
                //TODO: handle when this has failed: this will result in info.isConnected being false
                Log.i(NetworkManagerAndroid.TAG, "Network State Changed Action - ssid: " + ssid +
                        " connected:" + isConnected + " connectedorConnecting: " + isConnecting);
                if(isConnected){
                    Log.i(NetworkManagerAndroid.TAG, "Handle connection changed");
                    handleWifiDirectConnectionChanged(ssid);
                }
            }
        }, intentFilter);

        httpAndroidAssetsPath = "/assets-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + '/';
        httpd.addRoute(httpAndroidAssetsPath +"(.)+",  AndroidAssetsHandler.class, this);
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

    /**
     * This method is responsible for setting up the right services connection
     * @param serviceConnectionMap Map of all services connection made within the app.
     */
    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
    }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
        if(isBluetoothEnabled() && isWiFiEnabled()){
            if(enabled && nodeStatus != NODE_STATUS_SUPERNODE_RUNNING){
                startSuperNode();
            }else if(!enabled && nodeStatus != NODE_STATUS_CLIENT_RUNNING){
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
           bluetoothServerAndroid.start();
           if(nsdHelperAndroid.isDiscoveringNetworkService()){
               nsdHelperAndroid.stopNSDiscovery();
           }
           nsdHelperAndroid.registerNSDService();
           addNotification(NOTIFICATION_TYPE_SERVER,serverNotificationTitle, serverNotificationMessage);
           isSuperNodeEnabled=true;
           nodeStatus = NODE_STATUS_SUPERNODE_RUNNING;
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
            bluetoothServerAndroid.stop();
            isSuperNodeEnabled=false;
            nodeStatus = NODE_STATUS_CLIENT_RUNNING;
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


    /**
     * @exception IOException
     */
    @Override
    public void connectBluetooth(final String deviceAddress, final BluetoothConnectionHandler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice bluetoothDevice= null;

                InputStream in = null;
                OutputStream out = null;
                BluetoothSocket socket = null;
                boolean connected = false;

                try {
                    bluetoothDevice = adapter.getRemoteDevice(deviceAddress);
                    socket=bluetoothDevice.
                            createInsecureRfcommSocketToServiceRecord(BluetoothServer.SERVICE_UUID);
                    socket.connect();
                    if(socket.isConnected()) {
                        in = socket.getInputStream();
                        out = socket.getOutputStream();
                        connected = true;
                        handler.onConnected(socket.getInputStream(), socket.getOutputStream());

                    }
                }catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeOutputStream(out);
                    UMIOUtils.closeInputStream(in);
                    if(socket != null) {
                        try { socket.close(); }
                        catch(IOException e) {e.printStackTrace();}
                    }
                }

                //TODO:Handle on connection failure here

            }
        }).start();
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
    public void updateNotification(int notificationType, int progress, String title, String message) {
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
        mNotifyManager.notify(notificationType, mBuilder.build());
    }

    @Override
    public void removeNotification(int notificationType) {
        mBuilder.setProgress(0, 0, false);
        mBuilder.setOngoing(false);
        mNotifyManager.notify(notificationType, mBuilder.build());
        mNotifyManager.cancel(notificationType);
    }


    /**
     * This constructs a map of DNS-Text records to be associated with the service
     * @return HashMap : Constructed DNS-Text records.
     */
    private HashMap<String,String> localService(){
        boolean isConnected= connectivityManager!=null &&
                connectivityManager.getActiveNetworkInfo().getType()
                == ConnectivityManager.TYPE_WIFI;
        String deviceBluetoothMacAddress=getBluetoothMacAddress();
        String deviceIpAddress=isConnected ? getDeviceIPAddress():"";
        HashMap<String,String> record=new HashMap<>();
        record.put(SERVICE_DEVICE_AVAILABILITY,"available");
        record.put(SD_TXT_KEY_PORT,String.valueOf(getHttpListeningPort()));
        record.put(SD_TXT_KEY_BT_MAC, deviceBluetoothMacAddress);
        record.put(SD_TXT_KEY_IP_ADDR, deviceIpAddress);
        return record;
    }

    /**
     * Get bluetooth address of the device, in android 6 and above this tends to return
     * default bluetooth address which is referenced as DEFAULT_BLUETOOTH_ADDRESS.
     * After getting this we have to resolve it to get the real bluetooth Address.
     * @return String: Device bluetooth address
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

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(networkService).unregisterReceiver(mBroadcastReceiver);
        if(bluetoothServerAndroid !=null){
            bluetoothServerAndroid.stop();
        }

        if(nsdHelperAndroid!=null){
            nsdHelperAndroid.unregisterNSDService();
        }
        super.onDestroy();

    }

    @Override
    public void shareSetupFile(String filePath, String shareTitle) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
        getContext().startActivity(Intent.createChooser(intent,shareTitle));
    }


    /**
     * Method to get platform dependent application context
     * which may be referenced from different parts of the app.
     * @return Context: Application context
     */
    public Context getContext(){
        return networkService.getApplicationContext();
    }

    /**
     * @exception NullPointerException
     * @exception SocketException
     */
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

    /**
     * @exception InterruptedException
     */

    @Override
    public void connectWifi(String ssid, String passPhrase) {
        /*
         * Android 4.4 has been observed on Samsung Galaxy Ace (Andriod 4.4.2 - SM-G313F) to refuse to connect
         * to any wifi access point after wifi direct service discovery has started. It will connect
         * again only after wifi has been disabled, and then re-enabled.
         *
         * Our workaround is to programmatically disable and then re-enable the wifi on Android
         * versions that could be effected.
         */
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT && networkService.getWifiDirectHandlerAPI() != null){
            networkService.getWifiDirectHandlerAPI().stopServiceDiscovery();

            wifiManager.setWifiEnabled(false);
            try { Thread.sleep(100); }
            catch(InterruptedException e) {}

            wifiManager.setWifiEnabled(true);
            long waitTime = 0;
            do{
                try {Thread.sleep(300); }
                catch(InterruptedException e) {}
            }while(waitTime < 10000 && wifiManager.getConfiguredNetworks() == null);
        }


        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\""+ ssid +"\"";
        wifiConfig.priority=(getMaxConfigurationPriority(wifiManager)+1);
        wifiConfig.preSharedKey = "\""+ passPhrase +"\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.priority = getMaxConfigurationPriority(wifiManager);

        int netId = wifiManager.addNetwork(wifiConfig);

        /*
         * Note: calling disconnect or reconnect should not be required. enableNetwork(net, true)
         * second parameter is defined as boolean enableNetwork (int netId, boolean attemptConnect).
         *
         * It is however required on certain devices and does not seem to cause any harm to the connection
         * process on other devices.
         */
        wifiManager.disconnect();
        boolean successful = wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        Log.i(NetworkManagerAndroid.TAG, "Connecting to wifi: " + ssid + " passphrase: '" + passPhrase +"', " +
                "successful?"  + successful +  " priority = " + wifiConfig.priority);

        System.out.println("connectwifi");
    }

    /**
     * Get maximum priority assigned to a network configuration.
     * This helps to prioritize which network to connect to.
     *
     * @param wifiManager
     * @return int: Maximum configuration priority number.
     */
    private int getMaxConfigurationPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int maxPriority = 0;
        for(final WifiConfiguration config : configurations) {
            if(config.priority > maxPriority)
                maxPriority = config.priority;
        }

        return maxPriority;
    }

    @Override
    public synchronized void createWifiDirectGroup() {
        if(currentWifiDirectGroupStatus == WIFI_DIRECT_GROUP_STATUS_INACTIVE) {
            currentWifiDirectGroupStatus = WIFI_DIRECT_GROUP_STATUS_UNDER_CREATION;

            networkService.getWifiDirectHandlerAPI().setAddLocalServiceAfterGroupCreation(false);
            ServiceData serviceData= new ServiceData(NETWORK_SERVICE_NAME, getHttpListeningPort(),
                    new HashMap<String,String>() ,ServiceType.PRESENCE_TCP);
            networkService.getWifiDirectHandlerAPI().startAddingNoPromptService(serviceData, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_ACTIVE;

                }

                @Override
                public void onFailure(int reason) {
                    currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_INACTIVE;
                }
            });
        }
    }

    //TODO: Add a status flag for removal requested
    @Override
    public synchronized void removeWiFiDirectGroup() {
        if(currentWifiDirectGroupStatus == WIFI_DIRECT_GROUP_STATUS_ACTIVE) {
            networkService.getWifiDirectHandlerAPI().removeGroup(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    currentWifiDirectGroupStatus=WIFI_DIRECT_GROUP_STATUS_INACTIVE;
                    handleWifiDirectGroupRemoved(true);
                }

                @Override
                public void onFailure(int reason) {
                    handleWifiDirectGroupRemoved(false);
                }
            });
        }

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

    @Override
    public void reconnectPreviousNetwork() {
        if(previousConnectedNetId!=-1){
            wifiManager.enableNetwork(previousConnectedNetId, true);
            wifiManager.reconnect();
        }

    }

    /**
     * Method to get HTTP asserts URL.
     * @return String : Default HTTP android assert URL.
     */
    public String getHttpAndroidAssetsUrl() {
        return UMFileUtil.joinPaths(new String[]{"http://127.0.0.1:" + httpd.getListeningPort()
            + "/" + httpAndroidAssetsPath});
    }
}
