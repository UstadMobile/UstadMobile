package com.ustadmobile.port.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.p2p.P2PManagerSharedSE;
import com.ustadmobile.port.sharedse.p2p.P2PNode;
import com.ustadmobile.port.sharedse.p2p.P2PTask;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceData;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class NetworkManagerAndroid extends P2PManagerSharedSE
        implements P2PManager{

    private Map<Context, ServiceConnection> serviceConnectionMap;

    public static final String SERVICE_NAME = "ustadMobile";
    public static final String EXTRA_SERVICE_NAME="extra_test_service_name";
    private static String serviceName;

    public static final String PREF_KEY_SUPERNODE = "supernode_enabled";
    public static final String NO_PROMPT_NETWORK_PASS = "passphrase",
            NO_PROMPT_NETWORK_NAME = "networkName";


    private NetworkServiceAndroid p2pService;
    private NetworkServiceDiscoveryHelper mNetworkServiceDiscovery=null;

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

    private long discoveryStartTime;

    /**
     * Time to wait if connected to an existing wifi network before attempting to get index files
     * from WiFi direct hosts
     */
    public static final int P2P_INDEX_EXCHANGE_WAIT = 30*1000;

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
        mNetworkServiceDiscovery = new NetworkServiceDiscoveryHelper(p2pService.getApplicationContext());
        mNetworkServiceDiscovery.initializeNsd();
        mNetworkServiceDiscovery.setServiceName(service_name);
        startNetworkService(p2pService.getApplicationContext());
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
        filter.addAction(NetworkServiceDiscoveryHelper.ACTION_NSD_SERVICE_REGISTERED);
        filter.addAction(NetworkServiceDiscoveryHelper.ACTION_NSD_NEW_DEVICE_FOUND);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_SERVICE_TIMEOUT_ACTION);

        LocalBroadcastManager.getInstance(p2pService).registerReceiver(mBroadcastReceiver, filter);
        boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                PREF_KEY_SUPERNODE, "false", context));
        setSuperNodeEnabled(context, isSuperNodeEnabled);
        discoveryStartTime = new Date().getTime();
    }

    protected NetworkServiceAndroid getP2pService() {
        return p2pService;
    }



    /**
     * Handle when the service is going to be shut down
     */
    public void onDestroy() {
        LocalBroadcastManager.getInstance(p2pService).unregisterReceiver(mBroadcastReceiver);

        if(NetworkManagerAndroid.this.knownSuperNodes !=null){
            NetworkManagerAndroid.this.knownSuperNodes =null;
        }

        if(mNetworkServiceDiscovery!=null){
            mNetworkServiceDiscovery.stopDiscovery();
            mNetworkServiceDiscovery.tearDown();
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

                        P2PNode newNode = new P2PNode(deviceMac);
                        int nodeIndex = knownSuperNodes.indexOf(newNode);
                        if (nodeIndex >= 0) {
                            newNode = knownSuperNodes.get(nodeIndex);
                        }

                        newNode.setNetworkSSID(txtRecord.getRecord().get(NO_PROMPT_NETWORK_NAME));
                        newNode.setNetworkPass(txtRecord.getRecord().get(NO_PROMPT_NETWORK_PASS));
                        newNode.setStatus(txtRecord.getDevice().status);

                        if (nodeIndex < 0) {
                            NetworkManagerAndroid.this.knownSuperNodes.add(newNode);
                            handleNodeDiscovered(newNode, discoveryStartTime + P2P_INDEX_EXCHANGE_WAIT);
                        }
                    }

                    break;

                case WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION:
                    boolean connected = intent.getBooleanExtra(WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED, false);
                    Log.d(WifiDirectHandler.TAG, "noPromptConnection: status " + String.valueOf(connected));
                    checkQueue();
                    break;

                case DownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE:
                    Toast.makeText(context, "Download with ID " + intent.getStringExtra(DownloadTaskAndroid.EXTRA_DOWNLOAD_ID) + " Finished", Toast.LENGTH_LONG).show();
                    break;

                case NetworkServiceDiscoveryHelper.ACTION_NSD_NEW_DEVICE_FOUND:

                    String ipAddress=intent.getStringExtra(NetworkServiceDiscoveryHelper.EXTRA_NSD_DEVICE_IP_ADDRESS);
                    NsdServiceInfo serviceInfo=mNetworkServiceDiscovery.getFoundNsdServiceInfo().get(ipAddress);
                    P2PNode newNode=new P2PNode(serviceInfo.getHost().getHostAddress(),serviceInfo.getPort());
                    int nodeIndex = knownLocalNodes.indexOf(newNode);
                    if (nodeIndex >= 0) {
                        newNode = knownLocalNodes.get(nodeIndex);
                    }

                    if (nodeIndex < 0) {
                        NetworkManagerAndroid.this.knownLocalNodes.add(newNode);
                    }

                    break;
                case NetworkServiceDiscoveryHelper.ACTION_NSD_SERVICE_REGISTERED:
                    discoverNetworkService(p2pService.getApplicationContext());

            }
        }
    };

    @Override
    public P2PTask makeDownloadTask(P2PNode node, String downloadUri,long startTaskAfter) {
        return new DownloadTaskAndroid(node, downloadUri, this);
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
            return new ServiceData(serviceName, 8001, record, ServiceType.PRESENCE_TCP);
        }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {

        UstadMobileSystemImpl.getInstance().setAppPref("devices","",context);

        NetworkServiceAndroid service = getService(context);
        if(enabled) {
            service.showNotification();
            service.getWifiDirectHandlerAPI().startAddingNoPromptService(makeServiceData(), service.mNoPromptActionListener);
            if(mNetworkServiceDiscovery.isDiscovering()){
                mNetworkServiceDiscovery.stopDiscovery();
                mNetworkServiceDiscovery.tearDown();
            }
        }else {
            service.dismissNotification();
            if(service.getWifiDirectHandlerAPI().isGroupFormed()){
                service.getWifiDirectHandlerAPI().removeGroup();
            }


            service.getWifiDirectHandlerAPI().continuouslyDiscoverServices();
        }
    }


    @Override
    public List<P2PNode> getSuperNodes(Object context) {
        return knownSuperNodes;
    }

    @Override
    public void setClientEnabled(Object context, boolean enabled) {

    }

    @Override
    public boolean isSuperNodeAvailable(Object context) {
        return knownSuperNodes.size()>0;
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

    /**
     *
     * @return collection of all super nodes around
     */

    public List<P2PNode> getNodeList(){
        return knownSuperNodes;
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

    public HashMap<P2PNode, UstadJSOPDSFeed> getAvailableIndexes(){
        return availableIndexes;
    }


    @Override
    public void startNetworkService(Object context) {
        mNetworkServiceDiscovery.registerService();
    }

    @Override
    public void discoverNetworkService(Object context) {
        mNetworkServiceDiscovery.discoverServices();
    }


    public NetworkServiceDiscoveryHelper networkServiceDiscoveryHelper(){
        return mNetworkServiceDiscovery;
    }

    /**
     * Get the source to get file from.(Cloud,Local Network or P2P connection)
     * @return
     */
    public int getCurrentDownloadSource(){
        return this.currentDownloadSource;
    }


}
