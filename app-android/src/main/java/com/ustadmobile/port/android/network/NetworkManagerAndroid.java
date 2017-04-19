package com.ustadmobile.port.android.network;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.network.BluetoothTask;
import com.ustadmobile.port.sharedse.network.NetworkManagerSharedSE;
import com.ustadmobile.port.sharedse.network.NetworkNode;
import com.ustadmobile.port.sharedse.network.P2PTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceData;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.network.BluetoothConnectionManager.MESSAGE_READ;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.MESSAGE_STATE_CHANGE;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.MESSAGE_WRITE;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_CONNECTED;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_CONNECTING;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_LISTEN;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_NONE;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class NetworkManagerAndroid extends NetworkManagerSharedSE
        implements P2PManager{

    private Map<Context, ServiceConnection> serviceConnectionMap;

    public static final String SERVICE_NAME = "ustadMobile";
    public static final String EXTRA_SERVICE_NAME="extra_test_service_name";
    private static String serviceName;
    private boolean isConnected=false;
    private String [] fileAvailabilityResponse=null;

    public static final String PREF_KEY_SUPERNODE = "supernode_enabled";
    public static final String NO_PROMPT_NETWORK_PASS = "passphrase",
            NO_PROMPT_NETWORK_NAME = "networkName",
            COMMAND_TAG_FILE_AVAILABLE_CHECK = "file_status_check",
            COMMAND_TAG_FILE_AVAILABLE_FEEDBACK = "file_status_feedback",
            COMMAND_TAG_FILE_AVAILABLE_SEPARATOR = ":",
            FILE_IDS_SEPARATOR = "@",
            COMMAND_TAG_FILE_ACQUIRE = "acquire",
            FILE_AVAILABILITY_RESPONSE = "available",
            BLUETOOTH_ADDRESS = "bluetooth_address";

    private String [] FILE_IDSTO_PROCESS=null;


    private NetworkServiceAndroid p2pService;
    private BluetoothConnectionManager mBluetoothManager =null;

    /**
     * This hold a position of the bytes downloaded so far in the status array
     */
    public static final int DOWNLOAD_BYTES_DOWNLOADED_SOFAR = 0;
    /**
     * Holds a position of file total bytes in the status array
     */
    public static final int DOWNLOAD_FILE_TOTAL_BYTES = 1;
    /**
     * Holds a position of the actual downloading status in the status array
     */
    public static final int DOWNLOAD_STATUS = 2;

    private static String bluetoothAddress =null;

    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
    }
    /**
     * This needs to be called by NetworkServiceAndroid once the p2pservice has started
     * Context is going to be the NetworkServiceAndroid
     */
    public void init(Object context,String service_name) {
        serviceName=service_name;
        this.p2pService = (NetworkServiceAndroid) context;
        bluetoothAddress =getBluetoothMacAddress();
        mBluetoothManager =new BluetoothConnectionManager(p2pService.getApplicationContext(),mBluetoothHandler);
        mBluetoothManager.start();

        WifiInfo wifiInfo=p2pService.getWifiDirectHandlerAPI().getCurrentConnectedWifiInfo();
        if(wifiInfo!=null){
            NetworkManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_SSID]=wifiInfo.getSSID();
            NetworkManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_NETID]=String.valueOf(wifiInfo.getNetworkId());
            NetworkManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_GATWAY_ADDRESS]=wifiInfo.getMacAddress();

        }else{
            NetworkManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_SSID]= CURRENT_NETWORK_EMPTY_STATE;
            NetworkManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_NETID]= CURRENT_NETWORK_EMPTY_STATE;
            NetworkManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_GATWAY_ADDRESS]= CURRENT_NETWORK_EMPTY_STATE;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.MESSAGE_RECEIVED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION);
        filter.addAction(DownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_SERVICE_CREATED_ACTION);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_SERVICE_TIMEOUT_ACTION);
        filter.addAction(WifiDirectHandler.Action.NORMAL_WIFIDIRECT_CONNECTIVITY_ACTION);

        LocalBroadcastManager.getInstance(p2pService).registerReceiver(mBroadcastReceiver, filter);
        boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                PREF_KEY_SUPERNODE, "false", context));
        setSuperNodeEnabled(context, isSuperNodeEnabled);
    }





    /**
     * Handle when the service is going to be shut down
     */
    public void onDestroy() {
        LocalBroadcastManager.getInstance(p2pService).unregisterReceiver(mBroadcastReceiver);
        if(mBluetoothManager !=null){
            mBluetoothManager.stop();
        }

    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(context,action,Toast.LENGTH_LONG).show();

            switch (action) {
                case WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE:
                    String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                    DnsSdTxtRecord txtRecord = p2pService.getWifiDirectHandlerAPI().getDnsSdTxtRecordMap().get(deviceMac);
                    String fullDomain = txtRecord.getFullDomain();
                    String ustadFullDomain = serviceName + "." + ServiceType.PRESENCE_TCP + ".local.";

                    if (ustadFullDomain.equalsIgnoreCase(fullDomain)) {

                        NetworkNode newNode = new NetworkNode(deviceMac);
                        int nodeIndex = knownNodes.indexOf(newNode);
                        if (nodeIndex >= 0) {
                            newNode = knownNodes.get(nodeIndex);
                        }

                        newNode.setNetworkSSID(txtRecord.getRecord().get(NO_PROMPT_NETWORK_NAME));
                        newNode.setNetworkPass(txtRecord.getRecord().get(NO_PROMPT_NETWORK_PASS));
                        newNode.setStatus(txtRecord.getDevice().status);
                        newNode.setNodeBluetoothAddress(txtRecord.getRecord().get(BLUETOOTH_ADDRESS));

                        if (nodeIndex < 0) {
                            NetworkManagerAndroid.this.knownNodes.add(newNode);
                            handleNodeDiscovered(newNode);
                        }
                    }

                    break;

                case WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION:
                    boolean connected = intent.getBooleanExtra(WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED, false);
                    Log.d(WifiDirectHandler.TAG, "noPromptConnection: status " + String.valueOf(connected));
                    checkDownloadQueue();
                    break;

                case DownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE:
                    Toast.makeText(context, "Download with ID " + intent.getStringExtra(DownloadTaskAndroid.EXTRA_DOWNLOAD_ID) + " Finished", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    @Override
    public P2PTask makeDownloadTask(NetworkNode node, String downloadUri, long startTaskAfter) {
        return new DownloadTaskAndroid(node, downloadUri, this);
    }

    @Override
    protected BluetoothTask makeBlueToothTask(NetworkNode node) {
        return new BluetoothConnectionTask(node,this);
    }

    public NetworkServiceAndroid getService(Object context) {

            if (context instanceof NetworkServiceAndroid) {
                return (NetworkServiceAndroid) context;
            } else {
                UstadMobileSystemImplAndroid.BaseServiceConnection connection =
                        (UstadMobileSystemImplAndroid.BaseServiceConnection) serviceConnectionMap.get(context);
                NetworkServiceAndroid.LocalServiceBinder binder = (NetworkServiceAndroid.LocalServiceBinder) connection.getBinder();
                return binder.getService();
            }
        }

        public static ServiceData makeServiceData() {
            HashMap<String, String> record = new HashMap<>();
            record.put("available", "available");
            record.put(BLUETOOTH_ADDRESS, bluetoothAddress);
            return new ServiceData(serviceName, 8001, record, ServiceType.PRESENCE_TCP);
        }

        private HashMap<String, String> serviceData(){
            HashMap<String, String> record = new HashMap<>();
            record.put("available", "available");
            record.put(BLUETOOTH_ADDRESS, bluetoothAddress);
            return record;
        }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {

        UstadMobileSystemImpl.getInstance().setAppPref("devices","",context);

        NetworkServiceAndroid service = getService(context);
        if(enabled) {
            service.showNotification();
            service.getWifiDirectHandlerAPI().addLocalService(serviceName,serviceData(),null);
        }else {
            service.dismissNotification();
            if(service.getWifiDirectHandlerAPI().isGroupFormed()){
                service.getWifiDirectHandlerAPI().removeGroup();
            }


            service.getWifiDirectHandlerAPI().continuouslyDiscoverServices();
        }
    }


    @Override
    public void setClientEnabled(Object context, boolean enabled) {

    }

    @Override
    public boolean isSuperNodeAvailable(Object context) {
        return knownNodes.size()>0;
    }


    @Override
    public void stopDownload(Object context, int requestId, boolean delete) {
        P2PTask currentTask=NetworkManagerAndroid.this.downloadRequests.get(requestId);
        File downloadingFile=new File(currentTask.getDestinationPath());
        boolean isStopped=currentTask.stop();
        if(isStopped){
            NetworkManagerAndroid.this.downloadRequests=new HashMap<>();
        }

        if(delete){
            downloadingFile.delete();
        }
        UstadMobileSystemImpl.l(UMLog.DEBUG, 2, "stopDownload(): download process interrupted");

    }

    /**
     * Get status of the currently downloading file
     * @param context
     * @param requestId - download request ID
     * @return
     */
    @Override
    public int[] getRequestStatus(Object context, int requestId) {
        int statusVal[]=new int[3];
        P2PTask currentTask=getDownloadRequest().get(requestId);
        if(currentTask!=null){
            statusVal[DOWNLOAD_BYTES_DOWNLOADED_SOFAR]=currentTask.getBytesDownloadedSoFar();
            statusVal[DOWNLOAD_FILE_TOTAL_BYTES]=currentTask.getDownloadTotalBytes();
            statusVal[DOWNLOAD_STATUS]=currentTask.getDownloadStatus();
        }
        return statusVal;
    }

    @Override
    public int getStatus(Object context) {
        return 0;
    }


    @Override
    public String[] areFilesAvailable(Object context, String[] fileIds) {
        FILE_IDSTO_PROCESS=fileIds;
        String checkCommand= COMMAND_TAG_FILE_AVAILABLE_CHECK +
                COMMAND_TAG_FILE_AVAILABLE_SEPARATOR +idsToString(fileIds);
        mBluetoothManager.sendCommandMessage(checkCommand);
        return fileAvailabilityResponse;
    }


    String getBluetoothMacAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(WifiDirectHandler.TAG, "device does not support bluetooth");
            return null;
        }
        String address = mBluetoothAdapter.getAddress();
        if (address.equals("02:00:00:00:00:00")) {

            //  System.out.println(">>>>>G fail to get mac address " + address);
            try {
                ContentResolver mContentResolver = p2pService.getApplicationContext().getContentResolver();
                address = Settings.Secure.getString(mContentResolver, BLUETOOTH_ADDRESS);
                Log.d(WifiDirectHandler.TAG,"MAC Address - Resolved: " + address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(WifiDirectHandler.TAG,"MAC Address-No resolution: " + address);
        }
        return address;
    }


    /**
     * Handle all outgoing and incoming bluetooth connection message exchange
     */

    private Handler mBluetoothHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String sentCommand = new String(writeBuf);
                    Log.d(BluetoothConnectionManager.TAG,"Sent Command: "+sentCommand);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String receivedCommand[]  = new String(readBuf, 0, msg.arg1).split(COMMAND_TAG_FILE_AVAILABLE_SEPARATOR);
                    Log.d(BluetoothConnectionManager.TAG,"Received Command: command "+receivedCommand[0]+" data: "+receivedCommand[1]);

                    switch (receivedCommand[0]){

                        case COMMAND_TAG_FILE_AVAILABLE_CHECK:

                            String [] responses= mBluetoothManager.checkAvailability(receivedCommand[1].split(FILE_IDS_SEPARATOR));
                            String feedbackCommand= COMMAND_TAG_FILE_AVAILABLE_FEEDBACK +
                                    COMMAND_TAG_FILE_AVAILABLE_SEPARATOR +idsToString(responses);

                            mBluetoothManager.sendCommandMessage(feedbackCommand);
                            break;

                        case COMMAND_TAG_FILE_AVAILABLE_FEEDBACK:
                           if(NetworkManagerAndroid.this.currentBluetoothTask!=null){
                               String serverBluetoothAddress=NetworkManagerAndroid.this.currentBluetoothTask
                                       .getNode().getNodeBluetoothAddress();

                               if(!bluetoothAddress.equals(serverBluetoothAddress)){
                                   try{

                                       String [] feedback=stringsToIds(receivedCommand[1]);
                                       for (int position=0;position<feedback.length;position++){
                                           HashMap<String,String> response=new HashMap<>();
                                           response.put(BLUETOOTH_ADDRESS,serverBluetoothAddress);
                                           response.put(FILE_AVAILABILITY_RESPONSE,feedback[position].replaceAll("\\s+",""));

                                           if(!NetworkManagerAndroid.this.availableFiles.containsKey(FILE_IDSTO_PROCESS[position])){
                                               NetworkManagerAndroid.this.availableFiles.put(FILE_IDSTO_PROCESS[position],response);
                                           }
                                       }
                                   }catch (ArrayIndexOutOfBoundsException e){
                                       e.printStackTrace();
                                   }
                                   NetworkManagerAndroid.this.currentBluetoothTask.fireTaskEnded();
                               }

                           }
                            break;

                        case COMMAND_TAG_FILE_ACQUIRE:

                            String deviceMacAddress=receivedCommand[1];
                            p2pService.getWifiDirectHandlerAPI().connectToNormalWifiDirect(deviceMacAddress);
                            break;

                    }

                    break;
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            isConnected=true;
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Connected");
                            break;
                        case STATE_CONNECTING:
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Connecting");
                            break;
                        case STATE_LISTEN:
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Listening");
                            break;

                        case STATE_NONE:
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Not Connected");
                            break;
                    }

                    break;
            }
        }
    };


    private String idsToString(String [] fileIds){
        return TextUtils.join(FILE_IDS_SEPARATOR, fileIds);
    }


    private String [] stringsToIds(String stringIds){
        return TextUtils.split(stringIds,FILE_IDS_SEPARATOR);
    }




    public BluetoothConnectionManager getBluetoothConnectionManager(){
        return this.mBluetoothManager;
    }

    protected NetworkServiceAndroid getP2pService() {
        return p2pService;
    }


    /**
     *
     * @return all previously connected WiFi Hotspot to keep
     * track, when  the task queue is completed then
     * the device will be connected to this network.
     */

    public String [] getCurrentConnectedNetwork(){
        return NetworkManagerAndroid.this.currentConnectedNetwork;
    }

    /**
     *
     * @param currentConnectedNetwork - set current connected
     *                                network information (SSID,MAC address and NetID)
     */
    public void setCurrentConnectedNetwork(String [] currentConnectedNetwork){
        NetworkManagerAndroid.this.currentConnectedNetwork=currentConnectedNetwork;
    }

    /**
     *
     * @return HashMap of all requests made so that you can get task properties
     */
    public HashMap<Integer,P2PTask> getDownloadRequest(){
        return NetworkManagerAndroid.this.downloadRequests;
    }

    /**
     * Get the map of all found yes available indexes
     * @return
     */

    public HashMap<NetworkNode, UstadJSOPDSFeed> getAvailableIndexes(){
        return availableIndexes;
    }

    /**
     * Get the source to get file from.(Cloud,Local Network or P2P connection)
     * @return
     */
    public int getCurrentDownloadSource(){
        return this.currentDownloadSource;
    }

}
