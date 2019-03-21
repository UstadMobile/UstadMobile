package com.ustadmobile.port.android.netwokmanager;

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
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.net.ConnectivityManagerCompat;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.AsyncServiceManager;
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static android.os.Looper.getMainLooper;

/**
 * This class provides methods to perform android network related communications.
 * All Bluetooth Low Energy and WiFi direct communications will be handled here.
 * Also, this is maintained as a singleton by all activities binding to NetworkServiceAndroid,
 * which is responsible to call the onCreate method of this class.
 *
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

    /* Cast as required to avoid ClassNotFoundException on Android versions that dont support this */
    private Object gattServerAndroid;

    private Context mContext;

    private ParcelUuid parcelServiceUuid = new ParcelUuid(USTADMOBILE_BLE_SERVICE_UUID);

    private WifiP2pManager.Channel wifiP2pChannel;

    private WifiP2pManager wifiP2pManager;

    private EmbeddedHTTPD httpd;

    private UmAppDatabase umAppDatabase;

    private ConnectivityManager connectivityManager;

    /**
     * When we use BLE for advertising and scanning, we need wait a little bit after one starts
     * before the other can start
     */
    public static final int BLE_ADVERTISING_WAIT = 2000;

    private AtomicBoolean wifiP2PCapable = new AtomicBoolean(false);

    private AtomicReference<WifiManager.WifiLock> wifiLockReference = new AtomicReference<>();

    private WifiP2PGroupServiceManager wifiP2pGroupServiceManager;

    /**
     * A list of wifi direct ssids that are connected to using connectToWifiDirectGroup, the
     * WiFI configuration for these items should be deleted once we are done so they do not appear
     * on the user's list of remembered networks
     */
    private List<String> temporaryWifiDirectSsids = new ArrayList<>();

    private volatile long bleScanningLastStartTime;

    /**
     *
     */
    private BroadcastReceiver mBluetoothAndWifiStateChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkP2PBleServices();
        }
    };


    private static class WifiDirectGroupAndroid extends WiFiDirectGroupBle {
        private WifiDirectGroupAndroid(WifiP2pGroup group) {
            super(group.getNetworkName(), group.getPassphrase());
        }
    }


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

    private Handler delayedExecutionHandler = new Handler();

    private AsyncServiceManager scanningServiceManager = new AsyncServiceManager(
            AsyncServiceManager.STATE_STOPPED,
            ((runnable, delay) -> delayedExecutionHandler.postDelayed(runnable, delay))) {
        @Override
        public void start() {
            if(isBleCapable()){
                notifyStateChanged(STATE_STARTED);
                bleScanningLastStartTime = System.currentTimeMillis();
                bluetoothAdapter.startLeScan(new UUID[] {parcelServiceUuid.getUuid()},
                        leScanCallback);
                UstadMobileSystemImpl.l(UMLog.DEBUG,689,
                        "BLE Scanning started ");
            }else{
                notifyStateChanged(STATE_STOPPED);
                UstadMobileSystemImpl.l(UMLog.ERROR,689,
                        "Scanning already started or not BLE capable, no need to start");
            }
        }

        @Override
        public void stop() {
            bluetoothAdapter.stopLeScan(leScanCallback);
            notifyStateChanged(STATE_STOPPED);
        }
    };

    private AsyncServiceManager advertisingServiceManager = new AsyncServiceManager(
            AsyncServiceManager.STATE_STOPPED,
            ((runnable, delay) -> delayedExecutionHandler.postDelayed(runnable, delay))) {
        @Override
        public void start() {
            if(canDeviceAdvertise()){
                gattServerAndroid = new BleGattServerAndroid(mContext,
                        NetworkManagerAndroidBle.this);
                bleServiceAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

                BluetoothGattService service = new BluetoothGattService(parcelServiceUuid.getUuid(),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);

                BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                        parcelServiceUuid.getUuid(), BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

                service.addCharacteristic(writeCharacteristic);
                if(gattServerAndroid == null
                    || ((BleGattServerAndroid)gattServerAndroid).getGattServer() == null
                    || bleServiceAdvertiser == null) {
                    notifyStateChanged(STATE_STOPPED);
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
                            @Override
                            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                                super.onStartSuccess(settingsInEffect);
                                notifyStateChanged(STATE_STARTED);
                                UstadMobileSystemImpl.l(UMLog.DEBUG,689,
                                        "Service advertised successfully");
                            }

                            @Override
                            public void onStartFailure(int errorCode) {
                                super.onStartFailure(errorCode);
                                notifyStateChanged(STATE_STOPPED);
                                UstadMobileSystemImpl.l(UMLog.ERROR,689,
                                        "Service could'nt start, with error code "+errorCode);
                            }
                        });
            }else {
                notifyStateChanged(STATE_STOPPED);
            }
        }

        @Override
        public void stop() {
            BluetoothGattServer mGattServer = ((BleGattServerAndroid)gattServerAndroid).getGattServer();
            mGattServer.clearServices();
            mGattServer.close();
            gattServerAndroid = null;
            notifyStateChanged(STATE_STOPPED);
        }
    };

    private class WifiP2PGroupServiceManager extends AsyncServiceManager {

        private AtomicReference<WiFiDirectGroupBle> wiFiDirectGroup = new AtomicReference<>();

        private BroadcastReceiver wifiP2pBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(getState() == STATE_STARTING || getState() == STATE_STOPPING) {
                    wifiP2pManager.requestGroupInfo(wifiP2pChannel, (group) -> {
                        wiFiDirectGroup.set(group != null ? new WifiDirectGroupAndroid(group) : null);
                        notifyStateChanged(group != null ? STATE_STARTED : STATE_STOPPED);
                    });
                }
            }
        };

        private WifiP2PGroupServiceManager() {
            super(STATE_STOPPED,
                    (runnable, delay) -> delayedExecutionHandler.postDelayed(runnable, delay));
        }

        protected BroadcastReceiver getWifiP2pBroadcastReceiver() {
            return wifiP2pBroadcastReceiver;
        }

        @Override
        public void start() {
            wifiP2pManager.requestGroupInfo(wifiP2pChannel,
                    (wifiP2pGroup) -> {
                        if(wifiP2pGroup != null) {
                            wiFiDirectGroup.set(new WifiDirectGroupAndroid(wifiP2pGroup));
                            notifyStateChanged(STATE_STARTED);
                        }else {
                            createNewGroup();
                        }
                    });
        }

        private void createNewGroup() {
            wifiP2pManager.createGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    UstadMobileSystemImpl.l(UMLog.INFO,692, "Group created successfully");
                    /* wait for the broadcast. OnSuccess might be called before the group is really ready */
                }

                @Override
                public void onFailure(int reason) {
                    UstadMobileSystemImpl.l(UMLog.ERROR,692,
                            "Failed to create a group with error code "+reason);
                    notifyStateChanged(STATE_STOPPED);
                }
            });
        }

        @Override
        public void stop() {
            wifiP2pManager.removeGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    UstadMobileSystemImpl.l(UMLog.ERROR,693,
                            "Group removed successfully");
                    notifyStateChanged(STATE_STOPPED);
                }

                @Override
                public void onFailure(int reason) {
                    UstadMobileSystemImpl.l(UMLog.ERROR,693,
                            "Failed to remove a group with error code " + reason);
                    //check if wifi is enabled here
                    notifyStateChanged(STATE_STARTED);
                }
            });
        }

        public WiFiDirectGroupBle getGroup() {
            return wiFiDirectGroup.get();
        }
    }

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
        wifiP2pGroupServiceManager = new WifiP2PGroupServiceManager();


        if(wifiP2PCapable.get()) {
            wifiP2pChannel = wifiP2pManager.initialize(mContext, getMainLooper(), null);
            mContext.registerReceiver(wifiP2pGroupServiceManager.getWifiP2pBroadcastReceiver(),
                    new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
        }

        if(isBleCapable()){
            //setting up bluetooth connection listener
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            mContext.registerReceiver(mBluetoothAndWifiStateChangeBroadcastReceiver, intentFilter);

            bluetoothManager =  mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = ((BluetoothManager)bluetoothManager).getAdapter();

            /**
             * Android will not send an initial state on startup as happens for most other
             * receivers, so we have to do that ourselves
             */
            if(bluetoothAdapter != null) {
                Intent initialBluetoothIntent = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
                initialBluetoothIntent.putExtra(BluetoothAdapter.EXTRA_STATE,
                        bluetoothAdapter.getState());
                mBluetoothAndWifiStateChangeBroadcastReceiver.onReceive(mContext, initialBluetoothIntent);
            }
        }

        super.onCreate();
    }

    /**
     * Check that the required
     */
    public void checkP2PBleServices() {
        boolean scanningEnabled = isBluetoothEnabled() && isBleCapable() && wifiManager.isWifiEnabled();
        boolean advertisingEnabled = scanningEnabled & canDeviceAdvertise()
                && (System.currentTimeMillis() - bleScanningLastStartTime) > BLE_ADVERTISING_WAIT;
        scanningServiceManager.setEnabled(scanningEnabled);
        advertisingServiceManager.setEnabled(advertisingEnabled);

        if(scanningEnabled && canDeviceAdvertise() && !advertisingEnabled) {
            new Handler().postDelayed(this::checkP2PBleServices, BLE_ADVERTISING_WAIT);
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


    @Override
    public WiFiDirectGroupBle awaitWifiDirectGroupReady(long timeout, TimeUnit timeoutUnit) {
        wifiP2pGroupServiceManager.setEnabled(true);
        wifiP2pGroupServiceManager.await(state ->
                        state == AsyncServiceManager.STATE_STARTED
                        || state == AsyncServiceManager.STATE_STOPPED,
                timeout, timeoutUnit);
        return wifiP2pGroupServiceManager.getGroup();
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

        try{
            Class<?> actionLister = Class.forName("android.net.wifi.WifiManager$ActionListener");
            Object proxyInstance = Proxy.newProxyInstance(actionLister.getClassLoader(),
                    new Class[] {actionLister}, new WifiConnectInvocationProxyHandler());

            Method connectMethod = wifiManager.getClass().getMethod("connect",
                    int.class, actionLister);
            connectMethod.invoke(wifiManager,netId,proxyInstance);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
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
    public EmbeddedHTTPD getHttpd() {
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
        scanningServiceManager.setEnabled(false);
        advertisingServiceManager.setEnabled(false);
        wifiP2pGroupServiceManager.setEnabled(false);

        if(isBleCapable()) {
            mContext.unregisterReceiver(mBluetoothAndWifiStateChangeBroadcastReceiver);
        }

        if(wifiP2PCapable.get()) {
            mContext.unregisterReceiver(wifiP2pGroupServiceManager.getWifiP2pBroadcastReceiver());
        }

        super.onDestroy();
    }

    /**
     * This class is used when creating a ActionListener proxy to be used on
     * WifiManager#connect(int,ActionListener) invocation through reflection.
     */
    public class WifiConnectInvocationProxyHandler implements InvocationHandler{

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 699,
                    "Method was invoked using reflection  "+method.getName());
            return null;
        }
    }
}
