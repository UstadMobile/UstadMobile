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
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.networkmanager.AcquisitionTask;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.EntryCheckResponse;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class NetworkManagerAndroid extends NetworkManager{

    public static final String TAG="NetworkManagerAndroid";

    private Map<Context, ServiceConnection> serviceConnectionMap;

    public HashMap<String, Long> entryIdToDownloadIdMap =new HashMap<>();

    public HashMap<Long, String> downloadIdToEntryIdMap =new HashMap<>();

    public HashMap<Long,int[]> downloadIdToDownloadStatusMap=new HashMap<>();


    public static final String SERVICE_NAME = "ustadMobile";
    public static final String EXTRA_SERVICE_NAME="extra_test_service_name";

    private static  Long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    private final static int WAITING_TIME_TO_UPDATE = 500;

    public static final int AVERAGE_FILE_AGE_BEFORE_CHECK =60000;

    private static final String DEFAULT_BLUETOOTH_ADDRESS="02:00:00:00:00:00";

    public static final String DEVICE_BLUETOOTH_ADDRESS = "bluetooth_address";

    public static final String DEVICE_IP_ADDRESS = "device_ip_address";

    public static final String PREF_KEY_SUPERNODE = "supernode_enabled";
    private int downloadSource=-2;

    private static final String SERVICE_DEVICE_AVAILABILITY = "available";


    private static String currentServiceName;

    private boolean isSuperNodeEnabled=false;

    private BluetoothServerAndroid bluetoothServerAndroid;

    private WifiDirectHandler wifiDirectHandler;

    private NotificationManager mNotifyManager;

    private NotificationCompat.Builder mBuilder;
    private NetworkServiceAndroid networkService;

    private Context mContext;


    @Override
    public void init(Object context, String serviceName) {

        currentServiceName=serviceName;
        networkService = getService(context);
        mContext= networkService.getApplicationContext();
        wifiDirectHandler= networkService.getWifiDirectHandlerAPI();
        bluetoothServerAndroid=new BluetoothServerAndroid((Context) context);

        //listen for the intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.MESSAGE_RECEIVED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_DIRECT_CONNECTION_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_GROUP_CREATION_ACTION);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_SERVICE_TIMEOUT_ACTION);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_SERVICE_CREATED_ACTION);

        LocalBroadcastManager.getInstance(networkService).registerReceiver(mBroadcastReceiver, filter);
        boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                PREF_KEY_SUPERNODE, "false", context));
        setSuperNodeEnabled(context, isSuperNodeEnabled);

    }


    private BroadcastReceiver mBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){

                case WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE:

                    String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                    DnsSdTxtRecord txtRecord = networkService.getWifiDirectHandlerAPI().
                            getDnsSdTxtRecordMap().get(deviceMac);
                    String fullDomain = txtRecord.getFullDomain();
                    String ustadFullDomain = currentServiceName + "." + ServiceType.PRESENCE_TCP + ".local.";

                    if (ustadFullDomain.equalsIgnoreCase(fullDomain)) {

                        NetworkNode networkNode=new NetworkNode(deviceMac);

                        int nodeIndex = knownNetworkNodes.indexOf(networkNode);
                        if (nodeIndex >= 0) {
                            networkNode = knownNetworkNodes.get(nodeIndex);
                        }

                        networkNode.setDeviceBluetoothMacAddress(txtRecord.getRecord().
                                get(DEVICE_BLUETOOTH_ADDRESS));
                        networkNode.setDeviceIpAddress(txtRecord.getRecord().get(DEVICE_IP_ADDRESS));
                        networkNode.setDeviceWifiDirectMacAddress(deviceMac);

                        if (nodeIndex < 0) {
                            knownNetworkNodes.add(networkNode);
                            handleNodeDiscovered(networkNode);

                        }
                    }

                    break;
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
       if(wifiDirectHandler!=null){
           wifiDirectHandler.stopServiceDiscovery();
           wifiDirectHandler.setStopDiscoveryAfterGroupFormed(true);
           wifiDirectHandler.addLocalService(currentServiceName, localServiceData(),null);
           Log.d(TAG,"Service Information: "+localServiceData().toString());
           isSuperNodeEnabled=true;
       }
    }

    @Override
    public void stopSuperNode() {
        if(wifiDirectHandler!=null){
            wifiDirectHandler.removeService();
            wifiDirectHandler.continuouslyDiscoverServices();
            isSuperNodeEnabled=false;
        }
    }

    public  HashMap<String, String> localServiceData(){
        HashMap<String, String> record = new HashMap<>();
        record.put(SERVICE_DEVICE_AVAILABILITY, "available");
        record.put(DEVICE_BLUETOOTH_ADDRESS, getBluetoothMacAddress());
        record.put(DEVICE_IP_ADDRESS, getIpAddress());
        return record;
    }

    @Override
    public boolean isSuperNodeEnabled() {
        return isSuperNodeEnabled;
    }


    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothServerAndroid != null && bluetoothServerAndroid.isEnabled();
    }

    @Override
    public void startBluetoothServer() {
        bluetoothServerAndroid.start();
    }

    @Override
    public void stopBluetoothServer() {
        bluetoothServerAndroid.stop();
    }

    @Override
    public boolean isWiFiEnabled() {
        return wifiDirectHandler.isWifiEnabled();
    }

    @Override
    public NetworkTask createFileStatusTask(String[] entryIds,Object context) {
        return null;
    }

    @Override
    public NetworkTask createAcquisitionTask(UstadJSOPDSFeed feed,Object context) {
        AcquisitionTask task= new DownloadManagerAndroid(feed, this, networkService.getApplicationContext());
        task.setTaskType(QUEUE_ENTRY_ACQUISITION);
        queueTask(task);
        return task;
    }


    @Override
    public void connectBluetooth(String deviceAddress, BluetoothConnectionHandler handler) {
        BluetoothAdapter adapter=bluetoothServerAndroid.getBluetoothAdapter();
        bluetoothServerAndroid.setBluetoothConnectionHandler(handler);
        BluetoothDevice bluetoothDevice=adapter.getRemoteDevice(deviceAddress);
        try{
            BluetoothSocket bluetoothSocket=bluetoothDevice.
                    createInsecureRfcommSocketToServiceRecord(bluetoothServerAndroid.getInsecureUUID());
            bluetoothServerAndroid.setBluetoothState(BluetoothServer.BLUETOOTH_STATE_CONNECTING);
            bluetoothSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void handleEntriesStatusUpdate(NetworkNode node, String[] fileIds, boolean[] status) {

        List<EntryCheckResponse> responseList=null;
        Long time_now=Calendar.getInstance().getTimeInMillis();
        for (int position=0;position<fileIds.length;position++){
            EntryCheckResponse checkResponse=new EntryCheckResponse(node);
            checkResponse.setFileAvailable(status[position]);
            checkResponse.setLastChecked(time_now);
            responseList=this.entryResponses.get(fileIds[position]);
            if(responseList.isEmpty()){
                responseList=new ArrayList<>();
            }
            responseList.add(checkResponse);
            this.entryResponses.put(fileIds[position],responseList);
        }
    }

    @Override
    public int addNotification(int notificationType, String title, String message) {

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
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

    public NetworkServiceAndroid getService(Object context) {

        if (context instanceof NetworkServiceAndroid) {
            return (NetworkServiceAndroid) context;
        } else {
            UstadMobileSystemImplAndroid.BaseServiceConnection connection =
                    (UstadMobileSystemImplAndroid.BaseServiceConnection) serviceConnectionMap.get((Context)context);
            NetworkServiceAndroid.LocalServiceBinder binder = (NetworkServiceAndroid.LocalServiceBinder) connection.getBinder();
            return binder.getService();
        }
    }

    public EntryCheckResponse entryCheckResponse(String fileID){
        List<EntryCheckResponse> responses=this.entryResponses.get(fileID);
        if(responses!=null){
            long lastChecked=Calendar.getInstance().getTimeInMillis();
            for(EntryCheckResponse checkResponse: responses){
                if(checkResponse.isFileAvailable() &&
                        (((int)lastChecked-checkResponse.getLastChecked()) < AVERAGE_FILE_AGE_BEFORE_CHECK)){
                    return  checkResponse;
                }
            }
        }

        return null;
    }


    public void setDownloadSource(int source){
        this.downloadSource=source;
    }


    public int getDownloadSource(){
        return this.downloadSource;
    }

    public HashMap<String,Long> getEntryIdToDownloadIdMap(){
        return this.entryIdToDownloadIdMap;
    }

    public HashMap<Long,String> getDownloadIdToEntryIdMap(){
        return this.downloadIdToEntryIdMap;
    }

    public HashMap<Long,int[]> getDownloadIdToDownloadStatusMap(){
        return this.downloadIdToDownloadStatusMap;
    }

    public Vector<AcquisitionTask> getAcquisitionTaskQueue(){
        return acquisitionTaskQueue;
    }

    public String getBluetoothMacAddress(){

        if (bluetoothServerAndroid.getBluetoothAdapter() == null) {
            Log.d(WifiDirectHandler.TAG, "device does not support bluetooth");
            return null;
        }
        String address = bluetoothServerAndroid.getBluetoothAdapter().getAddress();
        if (address.equals(DEFAULT_BLUETOOTH_ADDRESS)) {
            try {
                ContentResolver mContentResolver = mContext.getApplicationContext().getContentResolver();
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


    public String getIpAddress(){
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
        return null;
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(networkService).unregisterReceiver(mBroadcastReceiver);
        if(bluetoothServerAndroid !=null){
            bluetoothServerAndroid.stop();
        }

    }

}
