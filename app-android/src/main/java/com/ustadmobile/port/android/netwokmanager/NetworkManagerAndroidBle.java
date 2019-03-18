package com.ustadmobile.port.android.netwokmanager;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.net.ConnectivityManagerCompat;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroupBle;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static android.os.Looper.getMainLooper;

/**
 * This class provides methods to perform android network related communications.
 * All Bluetooth Low Energy and WiFi direct communications will be handled here.
 * Also, this is maintained as a singleton by all activities binding to NetworkServiceAndroid,
 * which is responsible to call the onCreate method of this class.
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

    private AtomicReference<WifiManager.WifiLock> wifiLockReference = new AtomicReference<>();

    /**
     * A list of wifi direct ssids that are connected to using connectToWifiDirectGroup, the
     * WiFI configuration for these items should be deleted once we are done so they do not appear
     * on the user's list of remembered networks
     */
    private List<String> temporaryWifiDirectSsids = new ArrayList<>();

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

            boolean isMeteredConnection =
                    ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager);
            int state = isMeteredConnection ?
                    ConnectivityStatus.STATE_METERED : ConnectivityStatus.STATE_UNMETERED;

            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            UstadMobileSystemImpl.l(UMLog.VERBOSE, 42, "NetworkCallback: onAvailable" +
                    prettyPrintNetwork(networkInfo));

            String ssid = networkInfo != null ? normalizeAndroidWifiSsid(networkInfo.getExtraInfo()) : null;
            ConnectivityStatus status = new ConnectivityStatus(state, true,
                    ssid);
            connectivityStatusRef.set(status);

            //get network SSID
            if(ssid != null && ssid.startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX)){
                status.setConnectivityState(ConnectivityStatus.STATE_CONNECTED_LOCAL);
                localConnectionOpener = network::openConnection;
            }


            umAppDatabase.getConnectivityStatusDao().insert(status, null);
        }

        private void handleDisconnected() {
            localConnectionOpener = null;
            UstadMobileSystemImpl.l(UMLog.VERBOSE, 42, "NetworkCallback: handleDisconnected");
            connectivityStatusRef.set(new ConnectivityStatus(ConnectivityStatus.STATE_DISCONNECTED,
                    false, null));
            umAppDatabase.getConnectivityStatusDao()
                    .updateState(ConnectivityStatus.STATE_DISCONNECTED, null);
        }



        @Override
        public void onLost(Network network) {
            super.onLost(network);
            UstadMobileSystemImpl.l(UMLog.VERBOSE, 42, "NetworkCallback: onAvailable" +
                    prettyPrintNetwork(connectivityManager.getNetworkInfo(network)));
            handleDisconnected();
        }

        @Override
        public void onUnavailable(){
            UstadMobileSystemImpl.l(UMLog.VERBOSE, 42, "NetworkCallback: onUnavailable");
            super.onUnavailable();
            handleDisconnected();
        }
    }


    private String prettyPrintNetwork(NetworkInfo networkInfo){
        String val = "Network : ";
        if(networkInfo != null) {
            val += " type: " + networkInfo.getTypeName();
            val += " extraInfo: " + networkInfo.getExtraInfo();
        }else {
            val += " (null network info)";
        }

        return val;
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

            if(((BleGattServerAndroid)gattServerAndroid).getGattServer() == null)
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
                UstadMobileSystemImpl.l(UMLog.INFO,692,
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
    public void connectToWiFi(String ssid, String passphrase, int timeout) {
        deleteTemporaryWifiDirectSsids();
        endAnyLocalSession();

        if(ssid.startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX)) {
            temporaryWifiDirectSsids.add(ssid);
        }

        long connectionDeadline = System.currentTimeMillis() + timeout;

        boolean connectedOrFailed = false;

        boolean networkEnabled = false;

        do{
            UstadMobileSystemImpl.l(UMLog.INFO, 693, "Trying to connect to " + ssid);
            if(!networkEnabled){
                enableWifiNetwork(ssid,passphrase);
                UstadMobileSystemImpl.l(UMLog.INFO, 693,
                        "Network changed  to "+ssid);
                networkEnabled = true;
            }else if(isConnectedToRequiredWiFi(ssid)){
                UstadMobileSystemImpl.l(UMLog.INFO, 693,
                        "ConnectToWifi: Already connected to WiFi with ssid =" + ssid);
                break;
            } else {
                DhcpInfo routeInfo = wifiManager.getDhcpInfo();
                if (routeInfo != null && routeInfo.gateway > 0) {
                    @SuppressLint("DefaultLocale")
                    String gatewayIp = String.format("%d.%d.%d.%d",
                            (routeInfo.gateway & 0xff),
                            (routeInfo.gateway >> 8 & 0xff),
                            (routeInfo.gateway >> 16 & 0xff),
                            (routeInfo.gateway >> 24 & 0xff));
                    UstadMobileSystemImpl.l(UMLog.INFO, 693,
                            "Trying to ping gateway IP address " + gatewayIp);
                    if(ping(gatewayIp, 1000)){
                        UstadMobileSystemImpl.l(UMLog.INFO, 693,
                                "Ping successful!" + ssid);
                        connectedOrFailed = true;
                    }else {
                        UstadMobileSystemImpl.l(UMLog.INFO, 693,
                                "ConnectToWifi: ping to " + gatewayIp + " failed on " + ssid);
                    }
                }else{
                    UstadMobileSystemImpl.l(UMLog.INFO, 693,
                            "ConnectToWifi: No DHCP gateway yet on " + ssid);
                }
            }


            if(!connectedOrFailed && System.currentTimeMillis() > connectionDeadline){
                UstadMobileSystemImpl.l(UMLog.INFO, 693, " TIMEOUT: failed to connect " + ssid);
                break;
            }
            SystemClock.sleep(1000);

        } while (!connectedOrFailed);
    }

    private boolean isConnectedToWifi() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        return info != null
                && info.getType() == ConnectivityManager.TYPE_WIFI
                && info.isConnected();
    }

    private boolean ping(String ipAddress, long timeout) {
        try {
            return InetAddress.getByName(ipAddress).isReachable((int) timeout);
        } catch (IOException e) {
            //ping did not succeed
        }
        return false;
    }

    private void disableCurrentWifiNetwork() {
        //This may or may not be allowed depending on the version of android we are using.
        //Sometimes Android will feel like reconnecting to the last network, even though we told
        //it what network to connect with.

        //TODO: we must track any networks we successfully disable, so that we can reenable them
        if(isConnectedToWifi() && wifiManager.disconnect()) {
            wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        }
    }

    private boolean isConnectedToRequiredWiFi(String ssid){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null && normalizeAndroidWifiSsid(wifiInfo.getSSID())
                .equals(normalizeAndroidWifiSsid(ssid));
    }


    /**
     * Connect to a given WiFi network. Here we are assuming that the security is WPA2 PSK as
     * per the WiFi Direct spec. In theory, it should be possible to leave these settings to
     * autodetect. In reality, we should specify these to reduce the chance of the connection
     * timing out.
     *
     * @param ssid ssid to use
     * @param passphrase network passphrase
     */
    private void enableWifiNetwork(String ssid, String passphrase){
        if(isConnectedToWifi()) {
            disableCurrentWifiNetwork();
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\""+ ssid +"\"";
        config.preSharedKey = "\""+ passphrase +"\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        int netId = wifiManager.addNetwork(config);
        wifiManager.disconnect();
        boolean requestAccepted = wifiManager.enableNetwork(netId, true);
        UstadMobileSystemImpl.l(UMLog.INFO, 693, "Network: Connecting to wifi: "
                + ssid + " passphrase: '" + passphrase +"', " + "request submitted ?"
                + requestAccepted);
        wifiManager.reconnect();
    }


    @Override
    public void restoreWifi() {
        UstadMobileSystemImpl.l(UMLog.INFO, 339, "NetworkManager: restore wifi");
        endAnyLocalSession();
        wifiManager.disconnect();
        deleteTemporaryWifiDirectSsids();
        wifiManager.reconnect();
    }

    /**
     * Send an http request to the server so it knows we are done
     */
    private void endAnyLocalSession() {
        if(connectivityStatusRef.get() == null
                || connectivityStatusRef.get().getWifiSsid() == null
                || !connectivityStatusRef.get().getWifiSsid().startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX))
            return;

        String endpoint = umAppDatabase.getNetworkNodeDao().getEndpointUrlByGroupSsid(
                connectivityStatusRef.get().getWifiSsid());
        if(endpoint == null){
            UstadMobileSystemImpl.l(UMLog.ERROR, 699,
                    "ERROR: No endpoint url for ssid" +
                            connectivityStatusRef.get().getWifiSsid());
            return;
        }

        try {
            String endSessionUrl = endpoint + "endsession";
            UmHttpResponse response = UstadMobileSystemImpl.getInstance().makeRequestSync(new UmHttpRequest(mContext,
                    endSessionUrl));
            UstadMobileSystemImpl.l(UMLog.INFO, 699, "Send end of session request " +
                    endSessionUrl);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Android normally but not always surrounds an SSID with quotes on it's configuration objects.
     * This method simply removes the quotes, if they are there. Will also handle null safely.
     *
     * @param ssid network ssid to be normalized
     * @return normalized network ssid
     */
    private static String normalizeAndroidWifiSsid(String ssid) {
        if(ssid == null)
            return ssid;
        else
            return ssid.replace("\"", "");
    }

    private void deleteTemporaryWifiDirectSsids() {
        if(temporaryWifiDirectSsids.isEmpty())
            return;

        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        String ssid;
        for(WifiConfiguration config : configuredNetworks) {
            if(config.SSID == null)
                continue;

            ssid = normalizeAndroidWifiSsid(config.SSID);
            if(temporaryWifiDirectSsids.contains(ssid)){
                boolean removedOk = wifiManager.removeNetwork(config.networkId);
                if(removedOk) {
                    temporaryWifiDirectSsids.remove(ssid);
                    if(temporaryWifiDirectSsids.isEmpty())
                        return;
                }

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BleEntryStatusTask makeEntryStatusTask(Object context, List<Long> entryUidsToCheck,
                                                     NetworkNode peerToCheck) {
        if(isBleDeviceSDKVersion()){
            BleEntryStatusTaskAndroid entryStatusTask = new BleEntryStatusTaskAndroid(
                    (Context)context,this, entryUidsToCheck,peerToCheck);
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
            BleEntryStatusTaskAndroid task = new BleEntryStatusTaskAndroid((
                    Context)context,this,message, peerToSendMessageTo, responseListener);
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

    @Override
    public void lockWifi(Object lockHolder) {
        super.lockWifi(lockHolder);

        if(wifiLockReference.get() == null) {
            wifiLockReference.set(wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    "UstadMobile-Wifi-Lock-Tag"));
            UstadMobileSystemImpl.l(UMLog.INFO, 699, "WiFi lock acquired for "
                    + lockHolder);
        }
    }

    @Override
    public void releaseWifiLock(Object lockHolder) {
        super.releaseWifiLock(lockHolder);

        WifiManager.WifiLock lock = wifiLockReference.get();
        if(wifiLockHolders.isEmpty() && lock != null){
            wifiLockReference.set(null);
            lock.release();
            UstadMobileSystemImpl.l(UMLog.ERROR, 699,
                    "WiFi lock released from object "+lockHolder);
        }

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
