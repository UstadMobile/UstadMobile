package com.ustadmobile.port.android.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.network.BluetoothTask;
import com.ustadmobile.port.sharedse.network.DownloadTask;
import com.ustadmobile.port.sharedse.network.FileCheckResponse;
import com.ustadmobile.port.sharedse.network.NetworkManagerSharedSE;
import com.ustadmobile.port.sharedse.network.NetworkNode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceData;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static android.content.Context.WIFI_SERVICE;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.BLUETOOTH_ADDRESS;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.BLUETOOTH_TASK_TYPE_ACQUIRE;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.BLUETOOTH_TASK_TYPE_STATUS_CHECK;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.COMMAND_FILE_AVAILABLE_CHECK_TAG;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.COMMAND_TAG_FILE_ACQUIRE_FEEDBACK;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.FILE_AVAILABLE_COMMAND_SEPARATOR;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_CONNECTED;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.ACTION_DOWNLOAD_STARTING;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.COMMUNICATION_PORT;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.EXTRA_DOWNLOAD_SOURCE_ADDRESS;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.SERVER_ADDRESS;
import static edu.rit.se.wifibuddy.WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class NetworkManagerAndroid extends NetworkManagerSharedSE implements P2PManager{

    private Map<Context, ServiceConnection> serviceConnectionMap;

    public static final String SERVICE_NAME = "ustadMobile";
    public static final String SERVICE_DEVICE_AVAILABILITY = "available";
    /**
     * tag to get the service name if it
     * was passed as an extra during service binding
     */
    public static final String EXTRA_SERVICE_NAME="extra_test_service_name";
    private static String currentServiceName;

    public static final String PREF_KEY_SUPERNODE = "supernode_enabled";
    public static final String NO_PROMPT_NETWORK_PASS = "passphrase",
            NO_PROMPT_NETWORK_NAME = "networkName";

    public static final String NETWORK_CONNECTION_TYPE="WIFI";
    public static final String NETWORK_TYPE_WIFI_DIRECT="wifi_direct";
    public static final String NETWORK_TYPE_NORMAL_WIFI="normal_wifi";
    public static final String NETWORK_TYPE_UNKNOWN="unknown";

    /**
     * Time passed before retrying the connection
     */
    private static  Long TIME_PASSED_FOR_CONNECTION_RETRY = Calendar.getInstance().getTimeInMillis();

    /**
     * Time to wait before updating the progressbar
     */
    private final static int WAITING_TIME_TO_RETRY_CONNECTION = 5000;

    /**
     * Valid file age - greater than this we will need to check the file again.
     */
    public static final int AVERAGE_FILE_AGE_BEFORE_CHECK =60000;


    /**
     * List of all file ID's to be processed (Checking if they are available locally or not)
     */
    public static List<String> FILE_IDS_TO_PROCESS =null;


    private NetworkServiceAndroid p2pService=null;
    private BluetoothConnectionManager mBluetoothManager =null;
    private DownloadManagerAndroid downloadManagerAndroid=null;

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

    public static String DEVICE_BLUETOOTH_ADDRESS =null;
    private int retryCount=0;
    private int currentNetworkId=0;
    private boolean isGroupInfoSent=false;

    private int downloadSource=-2;

    private Context mContext;


    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
    }
    /**
     * This needs to be called by NetworkServiceAndroid once the p2pservice has started
     * Context is going to be the NetworkServiceAndroid
     */
    public void init(Object context,String service_name) {
        currentServiceName =service_name;
        this.p2pService = (NetworkServiceAndroid) context;
        mContext=p2pService.getApplicationContext();
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
        filter.addAction(BluetoothConnectionManager.ACTION_FILE_CHECKING_TASK_COMPLETED);
        filter.addAction(BluetoothConnectionManager.ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE);

        LocalBroadcastManager.getInstance(p2pService).registerReceiver(mBroadcastReceiver, filter);
        boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                PREF_KEY_SUPERNODE, "false", context));
        setSuperNodeEnabled(context, isSuperNodeEnabled);
        setCurrentNetworkId();
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
            WifiDirectHandler wifiDirectHandler=getP2pService().getWifiDirectHandlerAPI();

            switch (action) {
                case WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE:
                    String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                    DnsSdTxtRecord txtRecord = p2pService.getWifiDirectHandlerAPI().getDnsSdTxtRecordMap().get(deviceMac);
                    String fullDomain = txtRecord.getFullDomain();
                    String ustadFullDomain = currentServiceName + "." + ServiceType.PRESENCE_TCP + ".local.";

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
                            Log.d(BluetoothConnectionManager.TAG,"Node count "+knownNodes.size()+" "+txtRecord.getDevice().deviceName);
                        }
                    }

                    break;


                case BluetoothConnectionManager.ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE:
                    int state=intent.getIntExtra(BluetoothConnectionManager.EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG,
                            BluetoothConnectionManager.STATE_NONE);


                    if(state==STATE_CONNECTED){
                        String taskType=intent.getStringExtra(BluetoothConnectionManager.EXTRA_BLUETOOTH_TASK_TYPE);

                        if(FILE_IDS_TO_PROCESS !=null && FILE_IDS_TO_PROCESS.size()>0  &&
                                BLUETOOTH_TASK_TYPE_STATUS_CHECK.equals(taskType)){

                            String checkCommand= BLUETOOTH_TASK_TYPE_STATUS_CHECK +
                                    FILE_AVAILABLE_COMMAND_SEPARATOR+ COMMAND_FILE_AVAILABLE_CHECK_TAG +
                                    FILE_AVAILABLE_COMMAND_SEPARATOR +mBluetoothManager.idsToString(FILE_IDS_TO_PROCESS);
                            mBluetoothManager.sendCommandMessage(checkCommand);
                        }
                    }
                    break;

                case WifiDirectHandler.Action.NOPROMPT_GROUP_CREATION_ACTION:
                    /**
                     * Using WiFi direct group technique (server side device)
                     */
                    if(wifiDirectHandler.isGroupOwner() && !isGroupInfoSent) {
                        //Send Group information to the client side
                        String ssid = wifiDirectHandler.getNoPromptServiceData().getRecord().get(NO_PROMPT_NETWORK_NAME);
                        String passPhrase = wifiDirectHandler.getNoPromptServiceData().getRecord().get(NO_PROMPT_NETWORK_PASS);
                        String sendGroupInformation = BLUETOOTH_TASK_TYPE_ACQUIRE +
                                    FILE_AVAILABLE_COMMAND_SEPARATOR + COMMAND_TAG_FILE_ACQUIRE_FEEDBACK +
                                    FILE_AVAILABLE_COMMAND_SEPARATOR + ssid + FILE_AVAILABLE_COMMAND_SEPARATOR + passPhrase;
                            mBluetoothManager.sendCommandMessage(sendGroupInformation);
                        isGroupInfoSent=true;
                        }


                    break;
                case WifiDirectHandler.Action.WIFI_DIRECT_CONNECTION_CHANGED:

                    /**
                     * Using normal WiFi-direct technique (Server side device)
                     */
                   /* if(!wifiDirectHandler.isGroupOwner()
                            && mBluetoothManager.getConnectedDeviceAddress()!=null &&
                            !wifiDirectHandler.getThisDevice().deviceAddress.equals(mBluetoothManager.getConnectedDeviceAddress())){

                        String ipAddress=mBluetoothManager.getClientIpAddress();
                        Log.d(BluetoothConnectionManager.TAG,"Device IP address "+ipAddress);

                        String checkCommand= BLUETOOTH_TASK_TYPE_ACQUIRE +
                                FILE_AVAILABLE_COMMAND_SEPARATOR+COMMAND_TAG_FILE_ACQUIRE_FEEDBACK +
                                FILE_AVAILABLE_COMMAND_SEPARATOR +ipAddress;
                        mBluetoothManager.sendCommandMessage(checkCommand);
                    }*/

                   break;

                case WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION:
                    boolean isConnected=intent.getBooleanExtra(EXTRA_NOPROMPT_NETWORK_SUCCEEDED,false);
                    /**
                     * Device has been connected to the group network - on the client side device
                     * (Notify the Download manager to start the actual download)
                     */
                    if(mBluetoothManager.getConnectedDeviceAddress()==null && isConnected){
                        Intent startDownloadIntent = new Intent(ACTION_DOWNLOAD_STARTING);
                        startDownloadIntent.putExtra(EXTRA_DOWNLOAD_SOURCE_ADDRESS,"");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(startDownloadIntent);
                        mBluetoothManager.start();
                    }else{
                        //the connection was not made, wait for few seconds and retry
                        Long time_now = Calendar.getInstance().getTimeInMillis();
                        if(((time_now - TIME_PASSED_FOR_CONNECTION_RETRY) < WAITING_TIME_TO_RETRY_CONNECTION) && retryCount<=3) {
                            return;
                        }
                        wifiDirectHandler.connectToNoPromptService(mBluetoothManager.getTxtRecord());
                        retryCount++;
                    }

                    break;
                case WifiDirectHandler.Action.NOPROMPT_SERVICE_CREATED_ACTION:

            }
        }
    };

    @Override
    protected BluetoothTask makeBlueToothTask(NetworkNode node) {
        return new BluetoothConnectionTask(node,this);
    }

    @Override
    protected DownloadTask makeDownloadTask(UstadJSOPDSFeed feed) {
        downloadManagerAndroid= new DownloadManagerAndroid(feed,this);
        return downloadManagerAndroid;
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


        public static HashMap<String, String> localServiceData(){
            HashMap<String, String> record = new HashMap<>();
            record.put(SERVICE_DEVICE_AVAILABILITY, "available");
            record.put(BLUETOOTH_ADDRESS, DEVICE_BLUETOOTH_ADDRESS);
            return record;
        }

        public static ServiceData serviceData(){
            HashMap<String, String> record = new HashMap<>();
            record.put("available", "available");
            return new ServiceData(currentServiceName, COMMUNICATION_PORT, record, ServiceType.PRESENCE_TCP);
        }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {



        UstadMobileSystemImpl.getInstance().setAppPref("devices","",context);

        NetworkServiceAndroid service = getService(context);
        if(enabled) {
            if(this.p2pService.isBluetoothEnabled()){
                startInsecureBluetoothService();
                service.showNotification();
                service.getWifiDirectHandlerAPI().stopServiceDiscovery();
                service.getWifiDirectHandlerAPI().setStopDiscoveryAfterGroupFormed(true);
                service.getWifiDirectHandlerAPI().addLocalService(currentServiceName, localServiceData(),null);
            }
        }else {
            startInsecureBluetoothService();
            service.dismissNotification();
            service.getWifiDirectHandlerAPI().removeService();
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
    public void checkLocalFilesAvailability(Object context, List<String> fileIds) {
        FILE_IDS_TO_PROCESS =fileIds;
        mBluetoothManager.start();
        checkBluetoothQueue();
    }


    @Override
    public void stopDownload(Object context, int requestId, boolean delete) {


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
        statusVal[DOWNLOAD_BYTES_DOWNLOADED_SOFAR]=0;
        statusVal[DOWNLOAD_FILE_TOTAL_BYTES]=0;
        statusVal[DOWNLOAD_STATUS]=0;
        return statusVal;
    }

    @Override
    public int getStatus(Object context) {
        return 0;
    }


    @Override
    public void startInsecureBluetoothService() {
        mBluetoothManager =new BluetoothConnectionManager(p2pService.getApplicationContext(),this);
        DEVICE_BLUETOOTH_ADDRESS =mBluetoothManager.getBluetoothMacAddress();
        mBluetoothManager.start();
    }

    @Override
    public void stopInsecureBluetoothService() {
        mBluetoothManager.stop();
    }




    public BluetoothConnectionManager getBluetoothConnectionManager(){
        return this.mBluetoothManager;
    }

    protected NetworkServiceAndroid getP2pService() {
        return p2pService;
    }


    public DownloadManagerAndroid getDownloadManager(){
        return this.downloadManagerAndroid;
    }


    public HashMap<String,List<FileCheckResponse>> getAvailableFiles(){
        return NetworkManagerAndroid.this.availableFiles;
    }

    /**
     * Get the current running Bluetooth task
     * @return
     */
    public BluetoothTask getCurrentBluetoothTask(){
        return NetworkManagerAndroid.this.currentBluetoothTask;
    }

    /**
     * Get bluetooth tasks queued to be processed
     * @return
     */
    public Vector<BluetoothTask> getBluetoothTaskQueue(){
        return NetworkManagerAndroid.this.bluetoothQueue;
    }

   public int getCurrentTaskIndex(){
       return NetworkManagerAndroid.this.currentTaskIndex;
   }

   public void setCurrentTaskIndex(int index){
       NetworkManagerAndroid.this.currentTaskIndex=index;
   }



   private void setCurrentNetworkId(){
       WifiInfo wifiInfo=getP2pService().getWifiDirectHandlerAPI().getCurrentConnectedWifiInfo();
       if(wifiInfo!=null){
           currentNetworkId=wifiInfo.getNetworkId();
       }
   }

   public void reconnectToThePreviousNetwork(){
       WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
       wifiManager.enableNetwork(currentNetworkId,true);
       wifiManager.reconnect();

   }

    public FileCheckResponse fileCheckResponse(String fileID){
        List<FileCheckResponse> responses=this.availableFiles.get(fileID);
        if(responses!=null){
            long lastChecked=Calendar.getInstance().getTimeInMillis();
            for(FileCheckResponse checkResponse: responses){
                if(checkResponse.isFileAvailable() &&
                        (((int)lastChecked-checkResponse.getLastChecked()) < AVERAGE_FILE_AGE_BEFORE_CHECK)){
                    return  checkResponse;
                }
            }
        }

        return null;
    }

    /**
     * Get currently connected network TYPE,
     * This helps to know if the device is already connected on the Wi-Fi direct network or not.
     * @return
     */

    public String getNetworkType() {

        ConnectivityManager connectivity = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        if (connectivity != null) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();

            if (info != null && info.isConnected()) {

                if (info.getState() == NetworkInfo.State.CONNECTED) {

                    if(info.getTypeName().equalsIgnoreCase(NETWORK_CONNECTION_TYPE)){

                        WifiInfo wifiInfo=wManager.getConnectionInfo();

                        String address = Formatter.formatIpAddress(wifiInfo.getIpAddress());
                        String defaultAddress=SERVER_ADDRESS;

                        String serverAddress[]=address.split("\\.");
                        String defaultServerAddress[]=defaultAddress.split("\\.");


                        if(serverAddress[0].equals(defaultServerAddress[0])
                                && serverAddress[1].equals(defaultServerAddress[1])
                                && serverAddress[2].equals(defaultServerAddress[2])){
                            return NETWORK_TYPE_WIFI_DIRECT;
                        }else{
                            return NETWORK_TYPE_NORMAL_WIFI;
                        }
                    }

                }
            }
        }

        return NETWORK_TYPE_UNKNOWN;
    }


    public void setDownloadSource(int source){
        this.downloadSource=source;
    }

    public int getDownloadSource(){
        return this.downloadSource;
    }


}
