package com.ustadmobile.port.android.netwokmanager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.net.ConnectivityManagerCompat;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroupBle;

import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static android.os.Looper.getMainLooper;

/**
 * This class provides methods to perform android network related communications.
 * All Bluetooth Low Energy and WiFi direct communications will be handled here.
 * Also, this is maintained as a singleton by all activities binding to NetworkServiceAndroid,
 * which is responsible to call the init method of this class.
 *
 * <p>
 * Use {@link NetworkManagerAndroidBle#startAdvertising()} to start advertising
 * BLE services to the nearby peers.
 *<p>
 * Use {@link NetworkManagerAndroidBle#stopAdvertising()} to stop advertising
 * BLE services to the nearby peers.
 *<p>
 * Use {@link NetworkManagerAndroidBle#startScanning()} to start scanning for the BLE
 * services advertised from peer devices.
 *<p>
 * Use {@link NetworkManagerAndroidBle#stopScanning()} to stop scanning for the BLE
 * services advertised from peer devices.
 *<p>
 * Use {@link NetworkManagerAndroidBle#createWifiDirectGroup} to create WiFi direct
 * group for peer content downloading.
 *<p>
 * <b>Note:</b> Most of the scan / advertise methods here require
 * {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
 *
 * @see NetworkManagerBle
 *
 *  @author kileha3
 */
public class NetworkManagerAndroidBle extends NetworkManagerBle{

    private WifiManager wifiManager;

    private Object bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;

    private Object bleServiceAdvertiser;

    private Object gattServerAndroid;

    private Context mContext;

    private ParcelUuid parcelServiceUuid = new ParcelUuid(USTADMOBILE_BLE_SERVICE_UUID);

    private WifiP2pManager.Channel wifiP2pChannel;

    private WifiP2pManager wifiP2pManager;

    private WiFiDirectGroupBle wiFiDirectGroupBle;

    private EmbeddedHTTPD httpd;

    private UmAppDatabase umAppDatabase;

    private ConnectivityManager connectivityManager;

    public static final String ACTION_UM_P2P_SERVICE_STATE_CHANGED =
            "ACTION_UM_P2P_SERVICE_STATE_CHANGED";

    public static final String UM_P2P_SERVICE_EXTRA = "UM_P2P_SERVICE_EXTRA";

    public static final int EXTRA_UM_P2P_STATE_ON = 1;

    public static final int EXTRA_UM_P2P_STATE_OFF = 2;


    private AtomicBoolean bluetoothEnabled = new AtomicBoolean(false);

    private AtomicBoolean wifiP2PCapable = new AtomicBoolean(false);

    private AtomicBoolean bluetoothP2pRunning = new AtomicBoolean(false);

    /**
     * Listeners for the WiFi-Direct group connections / states,
     * invoked when WiFi Direct state/connection has changed
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null){

                switch (intent.getAction()){
                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                        NetworkInfo networkInfo =
                                intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                        if(wifiDirectGroupChangeStatus == WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS){
                            requestGroupInfo();
                        }else if(networkInfo.isConnected()){
                            requestConnectionInfo();
                        }
                        break;

                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        sendP2PStateChangeBroadcast();
                        break;
                }
            }
        }
    };

    /**
     * Since bluetooth state change broadcast doesn't receive a first broadcast when registered,
     * and it is protected with system access then this broadcast is a reflection of the system bluetooth
     * broadcast which can be sent manually.
     */
    private BroadcastReceiver umP2PReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                int state =  intent.getIntExtra(UM_P2P_SERVICE_EXTRA,EXTRA_UM_P2P_STATE_OFF);

                switch (state){
                    case EXTRA_UM_P2P_STATE_ON:
                        bluetoothEnabled.set(true);
                        break;
                    case EXTRA_UM_P2P_STATE_OFF:
                        bluetoothEnabled.set(false);
                        break;
                }

                //check if location permission is granted
                boolean permissionGranted = ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if(permissionGranted && bluetoothEnabled.get() && wifiP2PCapable.get()){

                    initGattServer();

                    if(!bluetoothP2pRunning.get()){
                        startAdvertising();
                    }

                    new Handler().postDelayed(() ->
                            startScanning(),TimeUnit.SECONDS.toMillis(2));

                }else{
                    stopScanning();
                    stopAdvertising();
                }

            }
        }
    };


    /**
     * Callback for BLE service scans for devices with
     * Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
     *
     * @see android.bluetooth.BluetoothAdapter.LeScanCallback
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {
        NetworkNode networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress(device.getAddress());
        handleNodeDiscovered(networkNode);
    };

    /**
     * Callback for the network connectivity changes
     */
    private class UmNetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            String WiFiSSID = null;
            boolean isMeteredConnection =
                    ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager);

            //get network SSID
            if(!isMeteredConnection){
                WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                        .getSystemService (Context.WIFI_SERVICE);
                WiFiSSID = wifiManager.getConnectionInfo()
                        .getSSID().replace("\"", "");
            }
            int state = isMeteredConnection ?
                    ConnectivityStatus.STATE_METERED : ConnectivityStatus.STATE_UNMETERED;
            umAppDatabase.getConnectivityStatusDao()
                    .update( state, WiFiSSID,true,null);
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            umAppDatabase.getConnectivityStatusDao()
                    .updateState(ConnectivityStatus.STATE_DISCONNECTED, null);
        }

        @Override
        public void onUnavailable(){
            super.onUnavailable();
            umAppDatabase.getConnectivityStatusDao()
                    .updateState(ConnectivityStatus.STATE_DISCONNECTED, null);
        }
    }

    /**
     * Constructor to be used when creating new instance
     *
     * @param context Platform specific application context
     */
    public NetworkManagerAndroidBle(Object context, EmbeddedHTTPD httpd) {
        super(context);
        mContext = ((Context) context);
        this.httpd = httpd;
        this.umAppDatabase = UmAppDatabase.getInstance(context);
        startMonitoringNetworkChanges();
    }


    @Override
    public void onCreate() {
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2PCapable.set(wifiP2pManager != null);

        if(wifiP2PCapable.get()){
            wifiP2pChannel = wifiP2pManager.initialize(mContext, getMainLooper(), null);

            if(isBleCapable()){

                //setting up WiFi Direct & bluetooth connection listener
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                mContext.registerReceiver(mReceiver, intentFilter);

                //Register bluetooth reflection local broadcast
                LocalBroadcastManager.getInstance(mContext).registerReceiver(umP2PReceiver,
                        new IntentFilter(ACTION_UM_P2P_SERVICE_STATE_CHANGED));

                bluetoothManager =  mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = ((BluetoothManager)bluetoothManager).getAdapter();
                initGattServer();
                sendP2PStateChangeBroadcast();
            }

        }
        super.onCreate();
    }

    private void initGattServer(){
        if(isBluetoothEnabled()){
            gattServerAndroid = new BleGattServerAndroid(mContext,this);
        }
    }



    @Override
    public void sendP2PStateChangeBroadcast() {
        Intent intent = new Intent(ACTION_UM_P2P_SERVICE_STATE_CHANGED);
        intent.putExtra(UM_P2P_SERVICE_EXTRA, (isBluetoothEnabled()
                ? EXTRA_UM_P2P_STATE_ON : EXTRA_UM_P2P_STATE_OFF));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWiFiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBleCapable() {
        return BluetoothAdapter.getDefaultAdapter() != null && isBleDeviceSDKVersion()
                && mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled()
                && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDeviceAdvertise() {
        return isAdvertiser() && bluetoothAdapter.isMultipleAdvertisementSupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAdvertising() {
        if(canDeviceAdvertise()){
            bleServiceAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

            BluetoothGattService service = new BluetoothGattService(parcelServiceUuid.getUuid(),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                    parcelServiceUuid.getUuid(), BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

            service.addCharacteristic(writeCharacteristic);
            if(gattServerAndroid == null)
                return;

            ((BleGattServerAndroid)gattServerAndroid).getGattServer().addService(service);

            if (bleServiceAdvertiser == null)
                return;

            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                    .build();

            AdvertiseData data = new AdvertiseData.Builder()
                    .addServiceUuid(parcelServiceUuid).build();

            ((BluetoothLeAdvertiser)bleServiceAdvertiser).startAdvertising(settings, data,
                    new AdvertiseCallback() {
                @Override public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    bluetoothP2pRunning.set(true);
                    UstadMobileSystemImpl.l(UMLog.DEBUG,689,
                            "Service advertised successfully");
                }

                @Override public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    bluetoothP2pRunning.set(false);
                    UstadMobileSystemImpl.l(UMLog.ERROR,689,
                            "Service could'nt start, with error code "+errorCode);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAdvertising() {
        if(bleServiceAdvertiser == null || ((BleGattServerAndroid)gattServerAndroid) == null)
            return;

        BluetoothGattServer mGattServer = ((BleGattServerAndroid)gattServerAndroid).getGattServer();
        if (isBleDeviceSDKVersion() && mGattServer != null) {
            mGattServer.clearServices();
            mGattServer.close();
            bluetoothP2pRunning.set(false);
            gattServerAndroid = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startScanning() {
        if(isBleCapable() && !bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.startLeScan(new UUID[] { parcelServiceUuid.getUuid()}, leScanCallback);
        }else{
            UstadMobileSystemImpl.l(UMLog.ERROR,689,
                    "Scanning already started, no need to start it again");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopScanning() {
        if(isBleCapable() && bluetoothP2pRunning.get() && !bluetoothEnabled.get())
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openBluetoothSettings() {
        mContext.startActivity(new Intent(
                android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setWifiEnabled(boolean enabled) {
        return wifiManager.setWifiEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createWifiDirectGroup() {
        if(wifiDirectGroupChangeStatus == WIFI_DIRECT_GROUP_INACTIVE_STATUS){
            wifiDirectGroupChangeStatus = WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS;
            if(isWiFiEnabled()){
                requestGroupInfo();
            }else{
                if(setWifiEnabled(true)){
                    startCreatingAGroup();
                }else{
                    UstadMobileSystemImpl.l(UMLog.DEBUG,692,
                            "Wifi is not enabled, enabling failed");
                }
            }
        }else{
            if(wifiDirectGroupChangeStatus == WIFI_DIRECT_GROUP_ACTIVE_STATUS){
                fireWiFiDirectGroupChanged(true, getWifiDirectGroup());
            }else {
                UstadMobileSystemImpl.l(UMLog.DEBUG,692,
                        "Wifi is being created, please wait for the callback");
            }
        }
    }

    @Override
    public WiFiDirectGroupBle getWifiDirectGroup() {
        return wiFiDirectGroupBle;
    }

    /**
     * Create a WiFi direct group
     */
    private void startCreatingAGroup(){
        wifiP2pManager.createGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                UstadMobileSystemImpl.l(UMLog.ERROR,692,
                        "Group created successfully");
                wifiDirectGroupChangeStatus = WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS;
            }

            @Override
            public void onFailure(int reason) {
                UstadMobileSystemImpl.l(UMLog.ERROR,692,
                        "Failed to create a group with error code "+reason);
                wifiDirectGroupChangeStatus = WIFI_DIRECT_GROUP_INACTIVE_STATUS;
            }
        });
    }

    /**
     * Request WiFi direct group information
     */
    private void requestGroupInfo(){
        wifiP2pManager.requestGroupInfo(wifiP2pChannel, group -> {
            if(group != null){
                wifiDirectGroupChangeStatus = WIFI_DIRECT_GROUP_ACTIVE_STATUS;
                wiFiDirectGroupBle = new WiFiDirectGroupBle(group.getNetworkName(),
                        group.getPassphrase());
                fireWiFiDirectGroupChanged(true, wiFiDirectGroupBle);
            }else{
                startCreatingAGroup();
            }
        });
    }

    private void requestConnectionInfo(){
        wifiP2pManager.requestConnectionInfo(wifiP2pChannel, info -> {
            if(info.groupFormed){
                //TODO: Handle this properly according to connection change on device
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWifiDirectGroup() {
        wifiP2pManager.removeGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                fireWiFiDirectGroupChanged(false,null);
                UstadMobileSystemImpl.l(UMLog.ERROR,693,
                        "Group removed successfully");
                wifiDirectGroupChangeStatus = WIFI_DIRECT_GROUP_INACTIVE_STATUS;
                requestGroupInfo();
            }

            @Override
            public void onFailure(int reason) {
                fireWiFiDirectGroupChanged(false,null);
                UstadMobileSystemImpl.l(UMLog.ERROR,693,
                        "Failed to remove a group with error code "+reason);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToWiFi(String ssid, String passphrase) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\""+ ssid +"\"";
        wifiConfig.priority = (getMaxWiFiConfigurationPriority(wifiManager)+1);
        wifiConfig.preSharedKey = "\""+ passphrase +"\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.priority = getMaxWiFiConfigurationPriority(wifiManager);

        int netId = wifiManager.addNetwork(wifiConfig);
        boolean isConnected = wifiManager.enableNetwork(netId, true);
        UstadMobileSystemImpl.l(UMLog.INFO, 648, "Network: Connecting to wifi: "
                + ssid + " passphrase: '" + passphrase +"', " + "successful?"  + isConnected
                +  " priority = " + wifiConfig.priority);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BleEntryStatusTask makeEntryStatusTask(Object context, List<Long> entryUidsToCheck,
                                                     NetworkNode peerToCheck) {
        if(isBleDeviceSDKVersion()){
            BleEntryStatusTaskAndroid entryStatusTask =
                    new BleEntryStatusTaskAndroid((Context)context,entryUidsToCheck,peerToCheck);
            entryStatusTask.setBluetoothManager((BluetoothManager)bluetoothManager);
            return entryStatusTask;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BleEntryStatusTask makeEntryStatusTask(Object context, BleMessage message,
                                                  NetworkNode peerToSendMessageTo,
                                                  BleMessageResponseListener responseListener) {
        if(isBleDeviceSDKVersion()){
            BleEntryStatusTaskAndroid task =
                    new BleEntryStatusTaskAndroid((Context)context,message,
                            peerToSendMessageTo, responseListener);
            task.setBluetoothManager((BluetoothManager)bluetoothManager);
            return task;
        }
        return null;
    }

    @Override
    public DeleteJobTaskRunner makeDeleteJobTask(Object object, Hashtable args) {
        return new DeleteJobTaskRunnerAndroid(object,args);
    }


    /**
     * Start monitoring network changes
     */
    private void startMonitoringNetworkChanges() {
        NetworkRequest networkRequest  = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        connectivityManager = (ConnectivityManager)mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
       if(connectivityManager != null){
           connectivityManager.requestNetwork(networkRequest,new UmNetworkCallback());
       }
    }

    /**
     * Check if the device needs runtime-permission
     * @return True if needed else False
     */
    private boolean isBleDeviceSDKVersion(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * Check if the device can advertise BLE service
     * @return True if can advertise else false
     */
    private boolean isAdvertiser(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Get maximum priority assigned to a network configuration.
     * This helps to prioritize which network to connect to.
     *
     * @param wifiManager WifiManager instance
     * @return Maximum configuration priority number.
     */
    private int getMaxWiFiConfigurationPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int maxPriority = 0;
        for(final WifiConfiguration config : configurations) {
            if(config.priority > maxPriority)
                maxPriority = config.priority;
        }

        return maxPriority;
    }

    @Override
    public RouterNanoHTTPD getHttpd() {
        return httpd;
    }


    /**
     * Get bluetooth manager instance
     * @return Instance of a BluetoothManager
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    BluetoothManager getBluetoothManager(){
        return  ((BluetoothManager)bluetoothManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        stopAdvertising();
        stopScanning();
        bluetoothP2pRunning.set(false);
        bluetoothEnabled.set(false);
        wifiP2PCapable.set(false);
        if(wifiP2pManager != null)
            try{ mContext.unregisterReceiver(mReceiver); }catch (Exception ignored){}
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(umP2PReceiver);
        super.onDestroy();
    }
}
