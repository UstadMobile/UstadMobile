package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroupBle;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private Map<Context, ServiceConnection> serviceConnectionMap;

    private Object gattServerAndroid;

    private Context mContext;

    private ParcelUuid parcelServiceUuid = new ParcelUuid(USTADMOBILE_BLE_SERVICE_UUID);

    private WifiP2pManager.Channel wifiP2pChannel;

    private WifiP2pManager wifiP2pManager;

    private WiFiDirectGroupBle wiFiDirectGroupBle;

    /**
     * Listeners for the WiFi-Direct group connections / states,
     * invoked when WiFi Direct state/connection has changed
     */
    private BroadcastReceiver p2pBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null){
                switch (intent.getAction()){
                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                        if(wifiDirectGroupChangeStatus == WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS){
                            requestGroupInfo();
                        }
                        break;
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
     * {@inheritDoc}
     */
    @Override
    public void init(Object context) {
        super.init(context);
        mContext = ((Context) context);
        wifiManager= (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(mContext, getMainLooper(), null);

        //setting up WiFi Direct connection listener
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mContext.registerReceiver(p2pBroadcastReceiver, intentFilter);

        if(isBleDeviceSDKVersion() && isBleCapable()){
            bluetoothManager =  mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = ((BluetoothManager)bluetoothManager).getAdapter();
            gattServerAndroid = new BleGattServerAndroid(((Context) context),this);
        }

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
        return isBleDeviceSDKVersion() &&
                mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
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
        if(isAdvertiser()){
            bleServiceAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

            BluetoothGattService service = new BluetoothGattService(parcelServiceUuid.getUuid(),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                    parcelServiceUuid.getUuid(), BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

            service.addCharacteristic(writeCharacteristic);

            ((BleGattServerAndroid)gattServerAndroid).getGattServer().addService(service);

            if (bleServiceAdvertiser == null) {
                return;
            }

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
                    UstadMobileSystemImpl.l(UMLog.DEBUG,689,
                            "Service advertised successfully");
                }

                @Override public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
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
        if(bleServiceAdvertiser == null) return;
        if (isBleDeviceSDKVersion() &&
                ((BleGattServerAndroid)gattServerAndroid).getGattServer() != null) {
            ((BleGattServerAndroid)gattServerAndroid).getGattServer().clearServices();
            ((BleGattServerAndroid)gattServerAndroid).getGattServer().close();
            gattServerAndroid = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startScanning() {
        if(isBleDeviceSDKVersion() && !bluetoothAdapter.isDiscovering()){
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
        if(isBleDeviceSDKVersion() && bluetoothAdapter.isDiscovering())
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
            if(group!=null){
                wifiDirectGroupChangeStatus = WIFI_DIRECT_GROUP_ACTIVE_STATUS;
                wiFiDirectGroupBle = new WiFiDirectGroupBle(group.getNetworkName(),
                        group.getPassphrase());
                fireWiFiDirectGroupChanged(true, wiFiDirectGroupBle);
            }else{
                startCreatingAGroup();
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
            entryStatusTask.setNetworkManagerBle(this);
            return entryStatusTask;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BleEntryStatusTask makeEntryStatusTask(Object context, BleMessage message,
                                                  NetworkNode peerToSendMessageTo, BleMessageResponseListener responseListener) {
        if(isBleDeviceSDKVersion()){
            BleEntryStatusTaskAndroid task =
                    new BleEntryStatusTaskAndroid((Context)context,message,
                            peerToSendMessageTo, responseListener);
            task.setBluetoothManager((BluetoothManager)bluetoothManager);
            task.setNetworkManagerBle(this);
            return task;
        }
        return null;
    }


    /**
     * Responsible for setting up the right services connection
     * @param serviceConnectionMap Map of all services connection made within the app.
     */
    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
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
        mContext.unregisterReceiver(p2pBroadcastReceiver);
        super.onDestroy();
    }
}
