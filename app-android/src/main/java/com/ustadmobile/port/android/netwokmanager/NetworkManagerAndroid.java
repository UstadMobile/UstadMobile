package com.ustadmobile.port.android.netwokmanager;

import android.app.Activity;
import android.app.Application;
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
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.app.NotificationCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.DefaultURLConnectionOpener;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.URLConnectionOpener;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


import javax.net.SocketFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * <h1>NetworkManagerAndroid</h1>
 *
 * This is a class wrapper which defines all the platform dependent operation
 * on android platform. It is responsible for registering all network listeners, register all services,
 * getting right device address like MAC address and IP address, handle bluetooth
 * and WiFi direct connections etc.
 *
 * Android implementation notes:
 *  - This is maintained as a singleton by all activities binding to NetworkServiceAndroid, which
 *    is responsible to call the init method of this class.
 *
 *  - The wifi performance lock is maintained during any active transfer. When something requires
 *    the wifi lock, it must call addActiveWifiObject and call removeActiveWifiObject when finished.
 *    When there are no active wifi objects left, the lock is released.
 *
 *  - Broadcast and discovery will run
 *    - When an activity from our application is in the foreground, and for 10 minutes thereafter
 *    - Continuously if supernode (e.g. always share) is enabled.
 *
 * @see com.ustadmobile.port.sharedse.networkmanager.NetworkManager
 *
 * @author kileha3
 * @author mike
 */
public class NetworkManagerAndroid extends NetworkManager implements EmbeddedHTTPD.ResponseListener, Application.ActivityLifecycleCallbacks{

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

    private int nodeStatus = -1;

    public static final int LOCAL_SERVICE_STATUS_INACTIVE = 0;

    public static final int LOCAL_SERVICE_STATUS_REQUESTED = 1;

    public static final int LOCAL_SERVICE_STATUS_ADDED = 3;

    private int p2pLocalServiceStatus = LOCAL_SERVICE_STATUS_INACTIVE;

    private int nsdLocalServiceStatus = LOCAL_SERVICE_STATUS_INACTIVE;


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

    private INsdHelperAndroid nsdHelperAndroid;

    private int currentWifiDirectGroupStatus= WIFI_DIRECT_GROUP_STATUS_INACTIVE;

    /**
     * Assets are served over http that are used to interact with the content (e.g. to inject a
     * javascript into the content that handles autoplay). Includes the a preceding '/'
     */
    private String httpAndroidAssetsPath;

    /**
     * A list of wifi direct ssids that are connected to using connectToWifiDirectGroup
     */
    private List<String> temporaryWifiDirectSsids = new ArrayList<>();


    /**
     * The time to wait after WiFi is enabled before attempting to start any discovery or service
     * broadcasting tasks
     */
    public static final int P2P_STARTUP_AFTER_WIFI_ENABLED_WAIT = 25000;

    /**
     * A set of objects that is actively using the wifi connections, maintained to track if we need
     * the wifi performance lock (or not). This includes download (e.g. acquisition) tasks and
     * http responses being sent to other nodes.
     */
    private Set activeWifiObjects = new HashSet();

    private WifiManager.WifiLock wifiLock;

    private Object receiveWifiLockObj = new Object();

    private Handler timeoutCheckHandler;

    /**
     * A set of objects that require wifi service broadcast and discovery to be kept active. This can
     * include activities, sync tasks, or download tasks waiting for a connection etc.
     */
    private Set activeP2pNetworkObjects = new HashSet();

    /**
     * Boolean indicating if p2p is active. P2P is active if from when a the first object is added
     * through addActiveP2pNetworkObject until p2pSwitchOffLagTime after the last object was removed
     */
    private volatile boolean p2pActive = false;

    private int p2pSwitchOffLagTime = (10 * 60 * 1000);

    private NetworkRequestHelper networkRequestHelper;

    //Used to create a SocketFactory which is bound to this network
    private SocketFactory currentWifiNetworkSocketFactory;

    /**
     * As per the wifi direct spec these are primary device types we are not interested in
     */
    private static final List<String> HIDDEN_WIFI_P2P_DEVICE_TYPES = Arrays.asList(
            "2",//input device
            "3",//printers, scanners, fax machines
            "4",//camera
            "5",//storage
            "6",//network infrastructure e.g. access points
            "7",//displays
            "8",//multimedia devices
            "9",//gaming devices
            "11"//audio devices
    );

    protected volatile boolean waitingForWifiUrlConnectionOpener = false;

    private final Object wifiUrlConnectionOpenerLock = new Object();

    private final ReentrantLock wifiNetworkObjLock = new ReentrantLock();

    private final Condition wifiNetworkReadyCondition = wifiNetworkObjLock.newCondition();

    private Runnable checkWifiLocksRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (activeWifiObjects) {
                if(activeWifiObjects.isEmpty() && wifiLock != null) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 364, "NetworkManager:WifiLock : release lock");
                    wifiLock.release();
                    wifiLock = null;
                }
            }
        }
    };

    private class CheckActiveP2pObjectsRunnable implements Runnable {
        @Override
        public void run() {
            synchronized (activeP2pNetworkObjects) {
                if(activeP2pNetworkObjects.isEmpty()) {
                    p2pActive = false;
                    UstadMobileSystemImpl.l(UMLog.INFO, 348,
                            "NetworkManagerAndroid: track active p2p objects: Runnable check: no active objects left");
                    updateNetworkServices();
                }
            }
        }
    }

    private Runnable checkActiveP2pObjectsRunnable;

    /**
     * All activities bind to NetworkServiceAndroid. NetworkServiceAndroid will call this init
     * method from it's onCreate
     *
     * @param context System context
     */
    @Override
    public void init(Object context) {
        super.init(context);
        networkService = (NetworkServiceAndroid)context;
        bluetoothServerAndroid=new BluetoothServerAndroid(this);
        boolean useJmDns = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

        nsdHelperAndroid= useJmDns ? new JmDnsHelperAndroid(networkService, this)
                : new NSDHelperAndroid(this);

        wifiManager= (WifiManager) networkService.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager= (ConnectivityManager) networkService.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        networkService.registerReceiver(mWifiBroadcastReceiver, intentFilter);

        IntentFilter connectivityIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkService.registerReceiver(mConnectivityBroadcastReceiver, connectivityIntentFilter);


        httpAndroidAssetsPath = "/assets-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + '/';
        httpd.addRoute(httpAndroidAssetsPath +"(.)+",  AndroidAssetsHandler.class, context);
        timeoutCheckHandler = new Handler();
        httpd.addResponseListener(this);

        networkService.getApplication().registerActivityLifecycleCallbacks(this);
    }

    /**
     * Android normally but not always surrounds an SSID with quotes on it's configuration objects.
     * This method simply removes the quotes, if they are there. Will also handle null safely.
     *
     * @param ssid
     * @return
     */
    public static String normalizeAndroidWifiSsid(String ssid) {
        if(ssid == null)
            return ssid;
        else
            return ssid.replace("\"", "");
    }


    private BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    boolean isConnected = info.isConnected();
                    boolean isConnecting = info.isConnectedOrConnecting();

                    //TODO: probably better to use the EXTRA_ info here
                    String ssid = wifiManager.getConnectionInfo() != null ?
                            wifiManager.getConnectionInfo().getSSID() : null;


                    ssid = normalizeAndroidWifiSsid(ssid);
                    String stateName = "";
                    switch(info.getState()){
                        case CONNECTED:
                            stateName = "connected";
                            break;

                        case CONNECTING:
                            stateName = "connecting";
                            break;

                        case DISCONNECTED:
                            stateName = "disconnected";
                            break;

                        case DISCONNECTING:
                            stateName = "disconnecting";
                            break;

                        case SUSPENDED:
                            stateName = "suspended";
                            break;

                        case UNKNOWN:
                            stateName = "unknown";
                            break;
                    }
                    UstadMobileSystemImpl.l(UMLog.DEBUG, 647, "Network: State Changed Action: " +
                            stateName + " - ssid: "  + ssid + " connected:" + isConnected +
                            " connectedorConnecting: " + isConnecting);
                    handleWifiConnectionChanged(ssid, isConnected, isConnecting);

                    if(Build.VERSION.SDK_INT >= 21) {
                        if(isConnected){
                            NetworkRequest.Builder builder = new NetworkRequest.Builder();
                            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                            networkRequestHelper = new NetworkRequestHelper(NetworkManagerAndroid.this,
                                    connectivityManager, ssid);
                            connectivityManager.registerNetworkCallback(builder.build(),
                                    networkRequestHelper);
                        }else {
                            synchronized (wifiUrlConnectionOpenerLock){
                                wifiUrlConnectionOpener = null;
                            }
                        }
                    }

                    break;

                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    if(intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR)) {
                        int errorState = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                        String message = null;
                        if(errorState != -1) {
                            message = SupplicantState.values()[errorState].name();
                        }

                        UstadMobileSystemImpl.l(UMLog.WARN, 214, "Network: Supplicant state change: error:"
                                + message);
                    }
                    break;

                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN);
                    switch(state) {
                        case WifiManager.WIFI_STATE_DISABLED:
                            cancelUpdateServicesTask();
                            updateClientServices();
                            updateSupernodeServices();
                            currentWifiDirectGroupStatus = WIFI_DIRECT_GROUP_STATUS_INACTIVE;
                            break;

                        case WifiManager.WIFI_STATE_ENABLED:
                            submitUpdateServicesTask(P2P_STARTUP_AFTER_WIFI_ENABLED_WAIT);
                            break;
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mConnectivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(
                    connectivityManager, intent);

            UstadMobileSystemImpl.l(UMLog.DEBUG, 0, "ConnectivityAction: network: " +
                networkInfoToString(networkInfo));

            if(networkInfo == null || !networkInfo.isConnected()){
                handleConnectivityChanged(CONNECTIVITY_STATE_DISCONNECTED);
            }else {
                handleConnectivityChanged(
                        ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager) ?
                        CONNECTIVITY_STATE_METERED : CONNECTIVITY_STATE_UNMETERED);
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


    public void updateNetworkServices() {
        updateClientServices();
        updateSupernodeServices();
    }

    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
        this.isSuperNodeEnabled = enabled;
        updateClientServices();
        updateSupernodeServices();
    }


    public synchronized void updateClientServices() {
        //starting and stopping NSD when the wifi is enabled or disabled was causing issues on 4.4.
        // For now run discovery as long as client mode is enabled
//        boolean shouldRunNsdDiscovery = discoveryEnabled;
        boolean shouldRunNsdDiscovery = isDiscoveryEnabled();
        if(shouldRunNsdDiscovery && !nsdHelperAndroid.isDiscoveringNetworkService()) {
            UstadMobileSystemImpl.l(UMLog.INFO, 301, "NetworkManager: start network service discovery");
            nsdHelperAndroid.startNSDiscovery();
        }else if(!shouldRunNsdDiscovery && nsdHelperAndroid.isDiscoveringNetworkService()) {
            nsdHelperAndroid.stopNSDiscovery();
        }
    }

    public synchronized void updateSupernodeServices() {
        boolean broadcastEnabled = isBroadcastEnabled();

        //TODO: If bluetooth is required to complete the transaction, we should check that is also enabled.
//        boolean shouldHaveLocalP2PService = broadcastEnabled && isWiFiEnabled() && networkService.getWifiDirectHandlerAPI() != null;

        //Starting/stopping NSD when wifi was enabled or disabled was causing issues for connecting
        //to networks on Android 4.4.
        boolean shouldHaveLocalNsdService = broadcastEnabled;
        if(shouldHaveLocalNsdService && nsdLocalServiceStatus ==LOCAL_SERVICE_STATUS_INACTIVE) {
            nsdHelperAndroid.registerNSDService();
            nsdLocalServiceStatus = LOCAL_SERVICE_STATUS_ADDED;
        }else if(!shouldHaveLocalNsdService && nsdLocalServiceStatus != LOCAL_SERVICE_STATUS_INACTIVE) {
            nsdHelperAndroid.unregisterNSDService();
            nsdLocalServiceStatus = LOCAL_SERVICE_STATUS_INACTIVE;
        }

        boolean shouldRunBluetoothServer = broadcastEnabled && isBluetoothEnabled();
        if(shouldRunBluetoothServer && !bluetoothServerAndroid.isRunning()) {
            bluetoothServerAndroid.start();
        }else if(!shouldRunBluetoothServer && bluetoothServerAndroid.isRunning()) {
            bluetoothServerAndroid.stop();
        }

        if(isSuperNodeEnabled()) {
            addNotification(NOTIFICATION_TYPE_SERVER,serverNotificationTitle, serverNotificationMessage);
        }else if(mBuilder!=null && mNotifyManager!=null){
            removeNotification(NOTIFICATION_TYPE_SERVER);
        }
    }

    @Override
    public boolean isSuperNodeEnabled() {
        return isSuperNodeEnabled;
    }

    @Override
    public boolean isBroadcastEnabled() {
        return p2pActive || isSuperNodeEnabled;
    }

    @Override
    public boolean isDiscoveryEnabled() {
        return p2pActive || isSuperNodeEnabled;
    }

    @Override
    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    @Override
    public boolean setBluetoothEnabled(boolean enabled) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
            return false;

        if(enabled){
            return adapter.enable();
        }else {
            return adapter.disable();
        }
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
    public boolean setWifiEnabled(boolean enabled) {
        return wifiManager.setWifiEnabled(enabled);
    }



    /**
     * @exception IOException
     */
    @Override
    public void connectBluetooth(final String deviceAddress, final BluetoothConnectionHandler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IOException ioe = null;
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
                        handler.onBluetoothConnected(socket.getInputStream(), socket.getOutputStream());

                    }
                }catch(IOException e) {
                    ioe = e;
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeOutputStream(out);
                    UMIOUtils.closeInputStream(in);
                    if(socket != null) {
                        try { socket.close(); }
                        catch(IOException e) {e.printStackTrace();}
                    }
                }

                if(!connected) {
                    handler.onBluetoothConnectionFailed(ioe);
                }
            }
        }).start();
    }

    @Override
    public int addNotification(int notificationType, String title, String message) {

        mNotifyManager = (NotificationManager) networkService.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(networkService, "um_download");
        mBuilder.setContentTitle(title)
                .setColor(ContextCompat.getColor(getContext(), R.color.accent))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification_icon);
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
        if(mBuilder != null){
            mBuilder.setProgress(0, 0, false);
            mBuilder.setOngoing(false);

            mNotifyManager.notify(notificationType, mBuilder.build());
            mNotifyManager.cancel(notificationType);
        }
    }


    /**
     * This constructs a map of DNS-Text records to be associated with the service
     * @return HashMap : Constructed DNS-Text records.
     */
    private HashMap<String,String> localService(){
        boolean isConnected= connectivityManager!=null
                && connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().getType()
                == ConnectivityManager.TYPE_WIFI;

        String deviceBluetoothMacAddress=getBluetoothMacAddress();
        String deviceIpAddress=isConnected ? getDeviceIPAddress():"";
        HashMap<String,String> record=new HashMap<>();
        record.put(SD_TXT_KEY_PORT,String.valueOf(getHttpListeningPort()));

        if(deviceBluetoothMacAddress != null)
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
        if(BluetoothAdapter.getDefaultAdapter() == null)
            return null;

        String address = BluetoothAdapter.getDefaultAdapter().getAddress();
        if (address.equals(DEFAULT_BLUETOOTH_ADDRESS)) {
            try {
                ContentResolver mContentResolver = networkService.getApplicationContext().getContentResolver();
                address = Settings.Secure.getString(mContentResolver, DEVICE_BLUETOOTH_ADDRESS);
                Log.d(NetworkManagerAndroid.TAG,"Bluetooth Address - Resolved: " + address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(NetworkManagerAndroid.TAG,"Bluetooth Address-No resolution: " + address);
        }
        return address;
    }

    @Override
    public void onDestroy() {
        if(bluetoothServerAndroid !=null){
            bluetoothServerAndroid.stop();
        }

        if(nsdHelperAndroid!=null){
            nsdHelperAndroid.unregisterNSDService();
            nsdHelperAndroid.stopNSDiscovery();
            nsdHelperAndroid.onDestroy();
        }

        networkService.unregisterReceiver(mWifiBroadcastReceiver);
        networkService.unregisterReceiver(mConnectivityBroadcastReceiver);
        networkService.getApplication().unregisterActivityLifecycleCallbacks(this);

        super.onDestroy();
    }

    @Override
    public void shareAppSetupFile(String filePath, String shareTitle) {
        String applicationId = getContext().getPackageName();
        Uri sharedUri = FileProvider.getUriForFile(getContext(), applicationId+".fileprovider",
                new File(filePath));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setPackage("com.android.bluetooth");

        if(shareIntent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(shareIntent);
        }else{
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Toast.makeText(getContext(),
                            R.string.error_this_device_doesnt_support_bluetooth_sharing,
                            Toast.LENGTH_LONG).show();
                }
            }.execute();
        }
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
    public void connectWifi(String ssid, String passphrase) {
        resetWifiIfRequired();
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\""+ ssid +"\"";
        wifiConfig.priority=(getMaxConfigurationPriority(wifiManager)+1);
        wifiConfig.preSharedKey = "\""+ passphrase +"\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.priority = getMaxConfigurationPriority(wifiManager);

        int netId = wifiManager.addNetwork(wifiConfig);

        /*
         * Note: calling disconnect or reconnect should not be required. enableNetwork(net, true)
         * second parameter is defined as boolean enableNetwork (int netId, boolean attemptConnect).
         *
         * Testing on Android Moto E (2nd gen) Andriod 6.0 the .reconnect() call would cause it to
         * reconnect ot the 'normal' wifi because connecting to the group is a bit slow.
         *
         * This method works without the .reconnect() as tested on Android 4.4.2 Samsung Galaxy ACE
         * and Moto E (2nd gen).
         */
        wifiManager.disconnect();
        boolean successful = wifiManager.enableNetwork(netId, true);
        UstadMobileSystemImpl.l(UMLog.INFO, 648, "Network: Connecting to wifi: " + ssid + " passphrase: '" + passphrase +"', " +
                "successful?"  + successful +  " priority = " + wifiConfig.priority);
    }



    private void resetWifiIfRequired(){
        /*
         * Android 4.4 has been observed on Samsung Galaxy Ace (Andriod 4.4.2 - SM-G313F) to refuse to connect
         * to any wifi access point after wifi direct service discovery has started. It will connect
         * again only after wifi has been disabled, and then re-enabled.
         *
         * Our workaround is to programmatically disable and then re-enable the wifi on Android
         * versions that could be affected.
         */
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT/* && networkService.getWifiDirectHandlerAPI() != null*/){
            wifiManager.setWifiEnabled(false);
            try { Thread.sleep(100); }
            catch(InterruptedException e) {}

            wifiManager.setWifiEnabled(true);
            long waitTime = 0;
            do{
                try {Thread.sleep(300); }
                catch(InterruptedException e) {}
                waitTime += 300;
            }while(waitTime < 10000 && wifiManager.getConfiguredNetworks() == null);
        }
    }

    @Override
    public void connectToWifiDirectGroup(String ssid, String passphrase) {
        temporaryWifiDirectSsids.add(ssid);
        super.connectToWifiDirectGroup(ssid, passphrase);
    }

    @Override
    public SocketFactory getWifiSocketFactory() {
        return currentWifiNetworkSocketFactory;
    }

    protected void setWifiSocketFactory(SocketFactory factory){
        this.currentWifiNetworkSocketFactory = factory;
    }

    /**
     * Gets a URL connection opener that is tied to the wifi network. When we connect to another
     * node using a wifi direct legacy connection (group ssid + passphrase), this is a 'normal' AP
     * mode connection that has no Internet. Android 21+ will not route any traffic via a network
     * that has no Internet connection unless we obtain the corresponding Network object and use
     * it to open a URL connection or to create a socket factory.
     *
     * The URLConnectionOpener is set by NetworkRequestHelper which is triggered by the
     * WIFI_STATE_CHANGED broadcast. This method will block for up to 10 seconds until it is available.
     *
     * @return URLConnectionOpener tied to the wifi network
     */
    @Override
    public URLConnectionOpener getWifiUrlConnectionOpener() {
        if(Build.VERSION.SDK_INT < 21) {
            return new DefaultURLConnectionOpener();
        }

        wifiNetworkObjLock.lock();
        if(wifiUrlConnectionOpener == null) {
            try {
                try {wifiNetworkReadyCondition.await(10, TimeUnit.SECONDS);}
                catch (InterruptedException e) {}
            } finally {
                wifiNetworkObjLock.unlock();
            }
        }


        return wifiUrlConnectionOpener;
    }

    /**
     * Set the Wifi bound URLConnectionOpener
     *
     * @param connectionOpener
     */
    protected void setWifiUrlConnectionOpener(URLConnectionOpener connectionOpener) {
        wifiNetworkObjLock.lock();
        try {
            this.wifiUrlConnectionOpener = connectionOpener;
            wifiNetworkReadyCondition.signalAll();
        }finally {
            wifiNetworkObjLock.unlock();
        }

    }


    @Override
    public void connectToWifiDirectNode(String deviceAddress) {
        /*
         * Our wifi direct send/receive relies on the sender being the group owner, and the receiver
         * being the group client. If there is a persistent group, it might remember the previous
         * roles and use those instead.
         */
    }

    @Override
    public void setReceivingOn(boolean receivingOn) {
        super.setReceivingOn(receivingOn);
        /*
         * Our wifi direct send/receive relies on the sender being the group owner, and the receiver
         * being the group client. If there is a persistent group, it might remember the previous
         * roles and use those instead.
         */
    }

    @Override
    public void cancelWifiDirectConnection() {

    }

    private void deleteTemporaryWifiDirectSsids() {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        String ssid;
        for(WifiConfiguration config : configuredNetworks) {
            if(config.SSID == null)
                continue;

            ssid = normalizeAndroidWifiSsid(config.SSID);
            if(temporaryWifiDirectSsids.contains(ssid)){
                boolean removedOk = wifiManager.removeNetwork(config.networkId);
                if(removedOk)
                    temporaryWifiDirectSsids.remove(ssid);
            }
        }
    }

    @Override
    public void restoreWifi() {
        //TODO: An improvement would be to note the network connected to before and connect to exactly that one : this may or may not be allowed by Android security on recent versions
        UstadMobileSystemImpl.l(UMLog.INFO, 339, "NetworkManager: restore wifi");
        wifiManager.disconnect();
        deleteTemporaryWifiDirectSsids();
        resetWifiIfRequired();
        wifiManager.reconnect();
    }

    @Override
    public void disconnectWifi() {
        wifiManager.disconnect();
        deleteTemporaryWifiDirectSsids();
    }

    @Override
    public String getCurrentWifiSsid() {
        WifiManager wifiManager=(WifiManager)getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        /*get current connection information (Connection might be Cellular/WiFi)*/
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            /*Check connection if is of type WiFi*/
            if (info.getTypeName().equalsIgnoreCase("WIFI")) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();    //get connection details using info object.
                return normalizeAndroidWifiSsid(wifiInfo.getSSID());
            }
        }

        return null;
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
        }
    }


    //TODO: Add a status flag for removal requested
    @Override
    public synchronized void removeWiFiDirectGroup() {

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
        return currentWifiDirectGroupStatus;
    }


    /**
     * Method to get HTTP asserts URL.
     * @return String : Default HTTP android assert URL.
     */
    public String getHttpAndroidAssetsUrl() {
        return UMFileUtil.joinPaths(new String[]{"http://127.0.0.1:" + httpd.getListeningPort()
            + httpAndroidAssetsPath});
    }

    @Override
    public int getWifiConnectionTimeout() {
        return 75000;
    }

    @Override
    public NetworkNode getThisWifiDirectDevice() {
        return null;
    }


    @Override
    public String getWifiDirectGroupOwnerIp() {
        return null;
    }

    @Override
    public boolean isWifiDirectConnectionEstablished(String otherDevice) {

        return false;
    }

    @Override
    public void networkTaskStatusChanged(NetworkTask task) {
        if(task.getTaskType() == QUEUE_ENTRY_ACQUISITION) {
            switch(task.getStatus()) {
                case NetworkTask.STATUS_RUNNING:
                    addActiveWifiObject(task);
                    break;

                default:
                    //it's not running - lock not needed
                    removeActiveWifiObject(task);
                    break;
            }
        }

        super.networkTaskStatusChanged(task);
    }

    @Override
    public void responseStarted(NanoHTTPD.Response response) {
        addActiveWifiObject(response);
    }

    @Override
    public void responseFinished(NanoHTTPD.Response response) {
        removeActiveWifiObject(response);
    }

    /**
     * Control use of the Android WiFi lock to keep wifi in high performance mode when operations
     * are ongoing (even if the screen dims, etc). When an object is added, if the Android WiFi lock
     * has not already been acquired, it will be acquired then. It will be released when there are
     * no active objects left.
     *
     * When an element of the app wants to conduct a wifi operation that requires high performance
     * mode to remain on, it should call addActiveWifiObject. When it is finished, it should call
     * removeActiveWifiObject with that same object reference.
     *
     * @param lockObject
     */
    public void addActiveWifiObject(Object lockObject) {
        synchronized (activeWifiObjects) {
            UstadMobileSystemImpl.l(UMLog.INFO, 356, "NetworkManager:WifiLock: "
                    + lockObject.toString() + " to active wifi objects");
            activeWifiObjects.add(lockObject);
            if(wifiLock == null) {
                UstadMobileSystemImpl.l(UMLog.INFO, 352, "NetworkManager:WifiLock Acquire wifi lock");
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);
                wifiLock.acquire();
            }
        }
    }

    /**
     * Control use of the Android WiFi lock as per addActiveWifiObject. If there are no active objects
     * left a 10 second timer is started. If at the end of that 10 second timer there are still no
     * active objects left, the lock is released.
     *
     * @param lockObject
     */
    public void removeActiveWifiObject(Object lockObject) {
        synchronized (activeWifiObjects) {
            UstadMobileSystemImpl.l(UMLog.INFO, 358, "NetworkManager:WifiLock: Remove "
                + lockObject.toString() + " from active wifi objects");
            activeWifiObjects.remove(lockObject);
            if(activeWifiObjects.isEmpty())
                timeoutCheckHandler.postDelayed(checkWifiLocksRunnable, 10000);
        }
    }

    /**
     * Add an object to the list of those that require wifi p2p services to be maintained. When no
     * objects are left, after a timeout period, p2p services will be switched off to save power.
     *
     * @param object Object that requires p2p broadcast and discovery services to be maintained e.g.
     *               an activity in this application, a download task, a sync task, etc.
     */
    public void addActiveP2pNetworkObject(Object object) {
        synchronized (activeP2pNetworkObjects) {
            if(activeP2pNetworkObjects.contains(object))
                return;

            p2pActive = true;
            cancelCheckActiveP2pObjects();
            UstadMobileSystemImpl.l(UMLog.INFO, 346,
                    "NetworkManagerAndroid: track active p2p objects: activity started:"
                    + object.toString());
            activeP2pNetworkObjects.add(object);
            if(activeP2pNetworkObjects.size() == 1)
                updateNetworkServices();
        }
    }


    /**
     * Remove an object from the list of those that require wifi p2p services to be maintained. When
     * no objects are left, after a timeout period, p2p services will be switched off to save power.
     *
     * @param object Object that requires p2p broadcast and discovery services to be maintained e.g.
     *               an activity in this application, a download task, a sync task, etc.
     */
    public void removeActiveP2pNetworkObject(Object object) {
        synchronized (activeP2pNetworkObjects) {
            if(!activeP2pNetworkObjects.contains(object))
                return;

            activeP2pNetworkObjects.remove(object);
            UstadMobileSystemImpl.l(UMLog.INFO, 347,
                    "NetworkManagerAndroid: track active p2p objects: remove object:"
                    + object.toString());
            if(activeP2pNetworkObjects.isEmpty()) {
                cancelCheckActiveP2pObjects();
                checkActiveP2pObjectsRunnable =new CheckActiveP2pObjectsRunnable();
                timeoutCheckHandler.postDelayed(checkActiveP2pObjectsRunnable, p2pSwitchOffLagTime);
                UstadMobileSystemImpl.l(UMLog.INFO, 347, "NetworkManagerAndroid: track foreground: no active activities");
            }
        }
    }

    /**
     * Used to determine if there are active objects using the p2p framework.
     * @return
     */
    protected boolean isP2pActive() {
        return p2pActive;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    /**
     * We register for lifecycle callbacks when NetworkServiceAndroid is created. By this point it
     * is possible that an activity was already started. Therefor we use the
     * BaseServiceConnection.onServiceConnected to check if the activity is started, and if so, it
     * will call this method itself. This method therefor can be called twice.
     *
     * @param activity The activity that has been started
     */
    @Override
    public void onActivityStarted(Activity activity) {
        addActiveP2pNetworkObject(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        removeActiveP2pNetworkObject(activity);
    }

    private void cancelCheckActiveP2pObjects() {
        if(checkActiveP2pObjectsRunnable != null) {
            timeoutCheckHandler.removeCallbacks(checkActiveP2pObjectsRunnable);
            checkActiveP2pObjectsRunnable = null;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static String networkInfoToString(NetworkInfo networkInfo) {
        if(networkInfo == null)
            return null;

        String str = "";
        switch(networkInfo.getType()){
            case ConnectivityManager.TYPE_WIFI:
                str += "WiFi ";
                break;

            case ConnectivityManager.TYPE_ETHERNET:
                str += "Ethernet ";
                break;

            case ConnectivityManager.TYPE_MOBILE:
                str += "Mobile ";
                break;

        }

        str += " connected: " + networkInfo.isConnected() + " connectedOrConnecting: "
                + networkInfo.isConnectedOrConnecting();

        return str;
    }
}
