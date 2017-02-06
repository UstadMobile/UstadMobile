package com.ustadmobile.port.android.p2p;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Build;
import android.os.Handler;

import com.ustadmobile.port.sharedse.p2p.DownloadRequest;
import com.ustadmobile.port.sharedse.p2p.P2PActionListener;
import com.ustadmobile.port.sharedse.p2p.P2PManager;
import com.ustadmobile.port.sharedse.p2p.P2PNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class P2PManagerAndroid extends P2PManager {

    private Context context;

    private WifiP2pManager manager;

    private WifiP2pManager.Channel channel;

    private ArrayList<P2PNode> superNodes,clientNode;

    private WifiP2pServiceRequest mWifiP2pServiceRequest;

    private P2PActionListener actionListenerAndroid;




    private static final int BROADCAST_TIME_INTERVAL = 60000;
    private static final int NODE_TYPE_SERVER=0;
    private static final int NODE_TYPE_CLIENT=1;
    private static int CURRENT_NOTE_TYPE;
    private static final String NODE_TYPE="nodeType";

    private int SERVICE_DISCOVERING_INTERVAL=2000;

    private static  int BROADCAST_DISCOVER_WAIT = 20000;

    public static int SERVICE_BROADCASTING_INTERVAL = 2000;


    private long LAST_SERVERICE_UPDATE_TIME = 0;

    private Handler mServiceDiscoveringHandler=new Handler();

    private Handler mServiceBroadcastingHandler = new Handler();
    /**
    service broadcast thread
     */
    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            long now = new Date().getTime();
            if(now - LAST_SERVERICE_UPDATE_TIME >= BROADCAST_TIME_INTERVAL) {

                //start broadcast service
                removeLocalService(new P2PActionListener() {
                    @Override
                    public void onSuccess() {
                        addLocalService(actionListenerAndroid, CURRENT_NOTE_TYPE);
                    }

                    @Override
                    public void onFailure(int errorCode) {

                    }
                });

            }else {

                discoverPeers();
                mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
            }
        }
    };


    /**
     * service discovery thread
     * */

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {

            //start service discovery
        }
    };

    /**
     *
     * @param context Should be the Android service class itself
     */


    public P2PManagerAndroid(Context context) {
        this.context = context;
        superNodes=new ArrayList<>();
        clientNode=new ArrayList<>();
    }


    @Override
    protected void init(P2PActionListener listener) {
        manager = (WifiP2pManager)context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), null);
        listener.onSuccess();
    }


    @Override
    protected void addLocalService(final P2PActionListener listener,int nodeType) {


            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){

                CURRENT_NOTE_TYPE =nodeType;
                HashMap<String, String> dataRecord = new HashMap<>();
                dataRecord.put("status", "visible");
                dataRecord.put(NODE_TYPE,String.valueOf(CURRENT_NOTE_TYPE));
                actionListenerAndroid=listener;


                WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(P2P_SERVICE_NAME,
                        P2P_REGISTRATION_TYPE, dataRecord);
                manager.addLocalService(channel, service, new P2PActionListenerAndroid(listener){
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable,
                                BROADCAST_DISCOVER_WAIT);
                    }

                    @Override
                    public void onFailure(int i) {
                        super.onFailure(i);
                        //log or re add the service
                    }
                });

            }
        }

    @Override
    protected void removeLocalService(final P2PActionListener listener) {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            manager.clearLocalServices(channel, new P2PActionListenerAndroid(listener));
        }
    }



    @Override
    protected void prepareServiceDiscovery(P2PActionListener listener) {

       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
           mWifiP2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

           manager.setDnsSdResponseListeners(channel,
                   new WifiP2pManager.DnsSdServiceResponseListener() {

                       @Override
                       public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

                          //logging for the service availability
                       }
                   }, new WifiP2pManager.DnsSdTxtRecordListener() {

                       @Override
                       public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {



                           P2PNode deviceInfo = new P2PNode(device.deviceAddress);
                           deviceInfo.setAddress(device.deviceAddress);
                           deviceInfo.setName(device.deviceName);
                           deviceInfo.setStatus(device.status);

                           /**
                            if you need to filter out the services , check if there are super nodes as well as clients
                            */

                            if(record.get(NODE_TYPE).equalsIgnoreCase(String.valueOf(NODE_TYPE_SERVER))){

                                if(superNodes!=null && !superNodes.contains(deviceInfo)){
                                    superNodes.add(deviceInfo);
                                }

                            }else if(record.get(NODE_TYPE).equalsIgnoreCase(String.valueOf(NODE_TYPE_CLIENT))){

                                if(superNodes!=null && !superNodes.contains(deviceInfo)){
                                    clientNode.add(deviceInfo);
                                }
                            }






                       }
                   });

           startServiceDiscovery(actionListenerAndroid);
       }

    }

    @Override
    protected void startServiceDiscovery(final P2PActionListener listener) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){


            stopServiceDiscovery(listener);
            manager.addServiceRequest(channel, mWifiP2pServiceRequest, new P2PActionListenerAndroid(listener){
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){

                        manager.discoverServices(channel,new P2PActionListenerAndroid(listener){
                            @Override
                            public void onSuccess() {
                                super.onSuccess();
                                mServiceDiscoveringHandler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERING_INTERVAL);
                            }

                            @Override
                            public void onFailure(int i) {
                                super.onFailure(i);
                                //log failure note
                            }
                        });
                    }
                }


                @Override
                public void onFailure(int i) {
                    super.onFailure(i);

                    //retry service or log the failure message
                }
            });


        }

    }



    @Override
    protected void stopServiceDiscovery(P2PActionListener listener) {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            manager.removeServiceRequest(channel, mWifiP2pServiceRequest, new P2PActionListenerAndroid(listener));
        }
    }

    @Override
    protected void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int error) {
            }
        });
    }



    @Override
    public boolean IsSuperNodeAvailable() {
        return superNodes.size()>0;
    }

    @Override
    public boolean isFileAvailable(String fileId) {
        return false;
    }

    @Override
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


    @Override
    public int getStatus() {

        return 0;
    }

    @Override
    public void setSuperNodeEnabled(boolean enabled) {

    }

}
