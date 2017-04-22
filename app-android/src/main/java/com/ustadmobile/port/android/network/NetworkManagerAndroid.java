package com.ustadmobile.port.android.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.p2p.P2PManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.network.BluetoothTask;
import com.ustadmobile.port.sharedse.network.NetworkManagerSharedSE;
import com.ustadmobile.port.sharedse.network.NetworkNode;
import com.ustadmobile.port.sharedse.network.P2PTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.network.BluetoothConnectionManager.BLUETOOTH_ADDRESS;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.COMMAND_TAG_FILE_AVAILABLE_CHECK;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.FILE_AVAILABLE_COMMAND_SEPARATOR;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_CONNECTED;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class NetworkManagerAndroid extends NetworkManagerSharedSE
        implements P2PManager{

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

    public static List<String> FILE_IDSTO_PROCESS=null;


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

    public static String DEVICE_BLUETOOTH_ADDRESS =null;
    public static final int START_QUE_INDEX=0;

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
        startInsecureBluetoothService();

        WifiInfo wifiInfo=p2pService.getWifiDirectHandlerAPI().getCurrentConnectedWifiInfo();

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
        filter.addAction(BluetoothConnectionManager.ACTION_FILE_CHECKING_COMPLETED);
        filter.addAction(BluetoothConnectionManager.ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE);

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

                case WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION:
                    boolean connected = intent.getBooleanExtra(WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED, false);
                    Log.d(WifiDirectHandler.TAG, "noPromptConnection: status " + String.valueOf(connected));
                    checkDownloadQueue();
                    break;

                case DownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE:
                    Toast.makeText(context, "Download with ID " + intent.getStringExtra(DownloadTaskAndroid.EXTRA_DOWNLOAD_ID) + " Finished", Toast.LENGTH_LONG).show();
                    break;

                case BluetoothConnectionManager.ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE:
                    int state=intent.getIntExtra(BluetoothConnectionManager.EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG,
                            BluetoothConnectionManager.STATE_NONE);
                    if(state==STATE_CONNECTED){
                        if(!mBluetoothManager.getBluetoothMacAddress().equals(currentBluetoothTask.getNode().getNodeBluetoothAddress()) &&
                                FILE_IDSTO_PROCESS!=null && FILE_IDSTO_PROCESS.size()>0){
                            String checkCommand= COMMAND_TAG_FILE_AVAILABLE_CHECK +
                                    FILE_AVAILABLE_COMMAND_SEPARATOR +mBluetoothManager.idsToString(FILE_IDSTO_PROCESS);
                            mBluetoothManager.sendCommandMessage(checkCommand);
                        }
                    }
            }
        }
    };

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


        public static HashMap<String, String> serviceData(){
            HashMap<String, String> record = new HashMap<>();
            record.put(SERVICE_DEVICE_AVAILABILITY, "available");
            record.put(BLUETOOTH_ADDRESS, DEVICE_BLUETOOTH_ADDRESS);
            return record;
        }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {

        UstadMobileSystemImpl.getInstance().setAppPref("devices","",context);

        NetworkServiceAndroid service = getService(context);
        if(enabled) {
            service.showNotification();
            service.getWifiDirectHandlerAPI().stopServiceDiscovery();
            service.getWifiDirectHandlerAPI().addLocalService(currentServiceName,serviceData(),null);
        }else {
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
        FILE_IDSTO_PROCESS=fileIds;
        checkBluetoothQueue();
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

    /**
     *
     * @return HashMap of all requests made so that you can get task properties
     */
    public HashMap<Integer,P2PTask> getDownloadRequest(){
        return NetworkManagerAndroid.this.downloadRequests;
    }

    /**
     * Get the source to get file from.(Cloud,Local Network or P2P connection)
     * @return
     */
    public int getCurrentDownloadSource(){
        return this.currentDownloadSource;
    }

    @Override
    public void taskEnded(P2PTask task) {

    }


    public HashMap<String,HashMap<String,String>> getAvailableFiles(){
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
}
