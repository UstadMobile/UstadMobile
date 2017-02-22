package com.ustadmobile.port.android.p2p;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.p2p.DownloadRequest;
import com.ustadmobile.port.sharedse.p2p.P2PActionListener;
import com.ustadmobile.port.sharedse.p2p.P2PManagerSharedSE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kileha3 on 05/02/2017.
 */

class P2PManagerAndroid extends P2PManagerSharedSE {

    /**
     *
     * Manage all P2P service discovery operation, from files to normal service.
     *
     */


    private static final String TXT_RECORD_PROP_AVAILABLE = "available";

    private static final String SERVICE_INSTANCE = "ustadMobile.com";

    private static final String SERVICE_REG_TYPE = "ustadmobile._tcp";

    private static final String NODE_TYPE = "node_type";

    private static  int CURRENT_NODE_TYPE;
    private static final String FULL_DOMAIN="ustadmobile.com.ustadmobile._tcp.local.";

    private WifiP2pManager manager;

    private WifiP2pManager.Channel channel;

    private WifiP2pServiceRequest mWifiP2pServiceRequest;

    private int SERVICE_DISCOVERING_INTERVAL=2000;

    private static final int SERVICE_BROADCASTING_INTERVAL = 20000;


    private Handler mServiceDiscoveringHandler=new Handler();

    private Handler mServiceBroadcastingHandler = new Handler();

    private Context context;

    private P2PActionListener p2PActionListener;

    private JSONArray nodeListArray = new JSONArray();

    private JSONObject nodeListObject= new JSONObject();



    private Runnable mServiceBroadcastingRunnable = new Runnable() {

        @Override
        public void run() {

            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    removeLocalService(p2PActionListener);
                }

                @Override
                public void onFailure(int error) {
                }
            });

            mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
        }
    };


    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            stopServiceDiscovery(p2PActionListener);
        }
    };



    P2PManagerAndroid(Context context, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.context = context;
        this.channel=channel;
        this.manager=manager;
        UstadMobileSystemImpl.getInstance().setAppPref("P2PStatus","0",context);
    }


    @Override
    protected void init(P2PActionListener listener) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":init(): - Initializing");

        removeLocalService(listener);
    }





    @Override
    protected void removeLocalService(final P2PActionListener listener) {
        p2PActionListener=listener;
        if(Build.VERSION.SDK_INT>=16){
            manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":removeLocalService(): - local service removed");

                    addLocalService(p2PActionListener);
                }

                @Override
                public void onFailure(int i) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 300, context.getClass().getSimpleName()+":removeLocalService(): - failed to remove local service");
                }
            });
        }
    }
    @Override
    protected void addLocalService(P2PActionListener listener) {
        p2PActionListener=listener;
        if(Build.VERSION.SDK_INT>=16){
            Map<String, String> dataRecord = new HashMap<>();
            dataRecord.put(TXT_RECORD_PROP_AVAILABLE, "visible");
            dataRecord.put(NODE_TYPE, String.valueOf(CURRENT_NODE_TYPE));
            WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, dataRecord);
            manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                    UstadMobileSystemImpl.l(UMLog.INFO, 300, context.getClass().getSimpleName()+":addLocalService(): - new service added type"+CURRENT_NODE_TYPE);
                    UstadMobileSystemImpl.getInstance().setAppPref("P2PStatus","1",context);
                    mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
                }

                @Override
                public void onFailure(int error) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 300, context.getClass().getSimpleName()+":addLocalService(): - failed to add new service");
                }
            });

        }

        prepareServiceDiscovery(p2PActionListener);
    }



    @Override
    protected void prepareServiceDiscovery(P2PActionListener listener) {
        p2PActionListener=listener;
        if(Build.VERSION.SDK_INT>=16){
            mWifiP2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
            manager.setDnsSdResponseListeners(channel,
                    new WifiP2pManager.DnsSdServiceResponseListener() {

                        @Override
                        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                            UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":prepareServiceDiscover(): - DNSService Available");

                        }
                    }, new WifiP2pManager.DnsSdTxtRecordListener() {

                        @Override
                        public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {

                            UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":prepareServiceDiscovery(): - DNS text records Available"+device.deviceName);

                                /*start list broadcasting to the UI handler*/

                            if(fullDomainName.equalsIgnoreCase(FULL_DOMAIN)){


                                try{

                                    if(!isInformationExist(nodeListArray,device.deviceAddress,"id")){
                                        JSONObject deviceJson = new JSONObject();
                                        deviceJson.put("name", device.deviceName);
                                        deviceJson.put("id", device.deviceAddress);
                                        deviceJson.put("type", record.get(NODE_TYPE));
                                        deviceJson.put ("status",String.valueOf(device.status));
                                        nodeListArray.put(deviceJson);
                                        nodeListObject.put("devices", nodeListArray);

                                        if(device.status==WifiP2pDevice.AVAILABLE){
                                            connectDevice(device.deviceName,device.deviceAddress);
                                        }
                                        UstadMobileSystemImpl.getInstance().setAppPref("devices",nodeListObject.toString(),context);

                                        context.sendBroadcast(new Intent("com.ustadmobile.port.android.p2p.NODE_LIST_CHANGED"));

                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    UstadMobileSystemImpl.l(UMLog.ERROR, 300, context.getClass().getSimpleName()+":pre[areServiceDiscovery(): - failed on getting JSON data",e);
                                }

                            }
                        }
                    });



            stopServiceDiscovery(p2PActionListener);
        }
    }


    @Override
    protected void stopServiceDiscovery(P2PActionListener listener) {
        p2PActionListener=listener;
        if(Build.VERSION.SDK_INT>=16){
            manager.removeServiceRequest(channel, mWifiP2pServiceRequest,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":stopServiceDiscovery(): - successfully stopped service discovery");
                            UstadMobileSystemImpl.getInstance().setAppPref("P2PStatus","2",context);
                            startServiceDiscovery(p2PActionListener);
                        }

                        @Override
                        public void onFailure(int reason) {

                            UstadMobileSystemImpl.l(UMLog.ERROR, 300, context.getClass().getSimpleName()+":stopServiceDiscovery(): - failed to stop service discovery");
                        }
                    });
        }
    }


    private boolean isInformationExist(JSONArray jsonData, String infoToFind, String tag){
        /*check for the certain tag value in the JSON data*/
        return jsonData.toString().contains("\""+tag+"\":\""+infoToFind+"\"");
    }

    @Override
    protected void startServiceDiscovery(P2PActionListener listener) {
        if(Build.VERSION.SDK_INT>=16){

            manager.addServiceRequest(channel, mWifiP2pServiceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                    UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":startServiceDiscovery(): - service request added successfully");
                   discoverPeers();
                }

                @Override
                public void onFailure(int error) {

                    UstadMobileSystemImpl.l(UMLog.ERROR, 300, context.getClass().getSimpleName()+":startServiceDiscovery(): - failed to add new service request");
                }
            });
        }
    }



    @Override
    protected void discoverPeers() {
        if(Build.VERSION.SDK_INT>=16){
            manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":discoverPeers(): - peers discovered successfully");
                    discoverService();
                }

                @Override
                public void onFailure(int error) {

                    UstadMobileSystemImpl.l(UMLog.ERROR, 300, context.getClass().getSimpleName()+":discoverPeers(): - peers discovery failed");
                }
            });
        }
    }


    private void discoverService(){
       if(Build.VERSION.SDK_INT>=16){
           manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
               @Override
               public void onSuccess() {
                   // this is my recursive discovery approach
                   mServiceDiscoveringHandler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERING_INTERVAL);
               }

               @Override
               public void onFailure(int code) {
               }
           });
       }
    }

    @Override
    public boolean isSuperNodeAvailable() {
        try{
            JSONObject devices=new JSONObject(UstadMobileSystemImpl.getInstance().getAppPref("devices",context));

            JSONArray nodes=devices.getJSONArray("devices");

            if(isInformationExist(nodes,"1","type")){

                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isFileAvailable(String fileId) {
        return false;
    }

    @Override
    /**
     * Start a foreground service that downloads the requested file from available peers
     */
    public int requestDownload(DownloadRequest request) {
        return 0;
    }

    @Override
    public void stopDownload(int requestId, boolean delete) {

    }

    @Override
    public int[] getRequestStatus(int requestId) {

        return new int[0];
    }


    void connectDevice(final String deviceName, String deviceAddress){
        WifiP2pConfig config=new WifiP2pConfig();
        config.deviceAddress=deviceAddress;
        manager.connect(channel,config,new P2PActionListenerAndroid(p2PActionListener){
            @Override
            public void onSuccess() {
                super.onSuccess();

                UstadMobileSystemImpl.l(UMLog.DEBUG, 300, context.getClass().getSimpleName()+":connectDevice(): -"+deviceName+" connected");

            }
        });

    }


    @Override
    public int getStatus() {
        return Integer.parseInt(UstadMobileSystemImpl.getInstance().getAppPref("P2PStatus",context));
    }

    @Override
    public void setSuperNodeEnabled(boolean enabled) {
        if(enabled){
            CURRENT_NODE_TYPE=1;
        }else{
            CURRENT_NODE_TYPE=0;
        }

    }
}
