package com.ustadmobile.port.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.p2p.P2PManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.p2p.P2PManagerSharedSE;
import com.ustadmobile.port.sharedse.p2p.P2PNode;
import com.ustadmobile.port.sharedse.p2p.P2PTask;

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

public class P2PManagerAndroid extends P2PManagerSharedSE implements P2PManager {

    /**
     * Manage all P2P service discovery operation, from files to normal service.
     */
    private Context context;

    private Map<Context, ServiceConnection> serviceConnectionMap;

    public static final String SERVICE_NAME = "ustadMobile";

    public static final String PREFKEY_SUPERNODE = "supernode_enabled";
    private static final String NO_PROMPT_NETWORK_PASS = "passphrase",
            NO_PROMPT_NETWORK_NAME = "networkName";


    private P2PServiceAndroid p2pService;
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


    /**
     * WiFi Discovery Service full domain
     */
    private String ustadFullDomain = SERVICE_NAME + "." + ServiceType.PRESENCE_TCP + ".local.";

    public P2PManagerAndroid() {
    }


    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
    }


    /**
     * This needs to be called by P2PServiceAndroid once the p2pservice has started
     * Context is going to be the P2PServiceAndroid
     */
    public void init(Object context) {

        this.p2pService = (P2PServiceAndroid) context;

        WifiInfo wifiInfo=p2pService.getWifiDirectHandlerAPI().getCurrentConnectedWifiInfo();
        if(wifiInfo!=null){
            P2PManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_SSID]=wifiInfo.getSSID();
            P2PManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_NETID]=String.valueOf(wifiInfo.getNetworkId());
            P2PManagerAndroid.this.currentConnectedNetwork[CURRENT_NETWORK_GATWAY_ADDRESS]=wifiInfo.getMacAddress();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.MESSAGE_RECEIVED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION);
        filter.addAction(P2PDownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE);

        LocalBroadcastManager.getInstance(p2pService).registerReceiver(mBroadcastReceiver, filter);
        boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                PREFKEY_SUPERNODE, "false", context));
        setSuperNodeEnabled(context, isSuperNodeEnabled);

    }

    protected P2PServiceAndroid getP2pService() {
        return p2pService;
    }



    /**
     * Handle when the service is going to be shut down
     */
    public void onDestroy() {
        LocalBroadcastManager.getInstance(p2pService).unregisterReceiver(mBroadcastReceiver);

        if(P2PManagerAndroid.this.knownSuperNodes !=null){
            P2PManagerAndroid.this.knownSuperNodes =null;
        }

    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(context,action,Toast.LENGTH_LONG).show();

            if (action.equals(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE)) {
                String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                DnsSdTxtRecord txtRecord = p2pService.getWifiDirectHandlerAPI().getDnsSdTxtRecordMap().get(deviceMac);
                String fullDomain = txtRecord.getFullDomain();



                //TODO Here: Validate the record received
                if (ustadFullDomain.equalsIgnoreCase(fullDomain)) {

                    P2PNode newNode = new P2PNode(deviceMac);
                    int nodeIndex = knownSuperNodes.indexOf(newNode);
                    if (nodeIndex >= 0) {
                        newNode = knownSuperNodes.get(nodeIndex);
                    }

                    newNode.setNetworkSSID(txtRecord.getRecord().get(NO_PROMPT_NETWORK_NAME).toString());
                    newNode.setNetworkPass(txtRecord.getRecord().get(NO_PROMPT_NETWORK_PASS).toString());
                    newNode.setStatus(txtRecord.getDevice().status);

                    if(nodeIndex<0){
                        P2PManagerAndroid.this.knownSuperNodes.add(newNode);
                        handleNodeDiscovered(newNode);
                    }
                }

            } else if (WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION.equals(action)) {

                boolean connected = intent.getBooleanExtra(WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED,false);
                Log.d(WifiDirectHandler.TAG,"noPromptConnection: status "+String.valueOf(connected));
                checkQueue();
            }else if(P2PDownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE.equals(action)){
                Toast.makeText(context,"Download with ID "+intent.getStringExtra(P2PDownloadTaskAndroid.EXTRA_DOWNLOAD_ID)+" Finished",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected P2PTask makeDownloadTask(P2PNode node, String downloadUri) {
        return new P2PDownloadTaskAndroid(node, downloadUri, this);
    }

    public P2PServiceAndroid getService(Object context) {

            if (context instanceof P2PServiceAndroid) {

                return (P2PServiceAndroid) context;
            } else {
                UstadMobileSystemImplAndroid.BaseServiceConnection connection =
                        (UstadMobileSystemImplAndroid.BaseServiceConnection) serviceConnectionMap.get(context);
                P2PServiceAndroid.LocalServiceBinder binder = (P2PServiceAndroid.LocalServiceBinder) connection.getBinder();
                return binder.getService();
            }
        }

        public static ServiceData makeServiceData() {
            HashMap<String, String> record = new HashMap<>();
            record.put("available", "available");
            ServiceData serviceData = new ServiceData(SERVICE_NAME, 8001, record, ServiceType.PRESENCE_TCP);
            return serviceData;
        }




    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
        this.context=(Context) context;
        UstadMobileSystemImpl.getInstance().setAppPref("devices","",context);
        P2PServiceAndroid service = getService(context);
        if(enabled && service.getWifiDirectHandlerAPI().getNoPromptServiceStatus() == WifiDirectHandler.NOPROMPT_STATUS_INACTIVE) {
            service.showNotification();
            service.getWifiDirectHandlerAPI().startAddingNoPromptService(makeServiceData(),service.mNoPromptActionListener);
        }else if(!enabled){
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

    }

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
     * @return all previously connected WiFi Hotspot to keep track, when  the task queue is completed then
     * the device will be connected to this network.
     */

    public String [] getCurrentConnectedNetwork(){
        return P2PManagerAndroid.this.currentConnectedNetwork;
    }

    public void setCurrentConnectedNetwork(String [] currentConnectedNetwork){
        P2PManagerAndroid.this.currentConnectedNetwork=currentConnectedNetwork;
    }

    /**
     *
     * @return HashMap of all requests made so that you can get task properties
     */
    public HashMap<Integer,P2PTask> getDownloadRequest(){
        return P2PManagerAndroid.this.downloadRequests;
    }



}
