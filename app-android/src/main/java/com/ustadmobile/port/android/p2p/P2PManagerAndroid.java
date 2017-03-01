package com.ustadmobile.port.android.p2p;

import android.content.Context;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.p2p.DownloadRequest;
import com.ustadmobile.port.sharedse.p2p.P2PManagerSharedSE;
import com.ustadmobile.port.sharedse.p2p.P2PNode;

import java.util.HashMap;
import java.util.Map;

import edu.rit.se.wifibuddy.FailureReason;
import edu.rit.se.wifibuddy.ServiceData;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class P2PManagerAndroid extends P2PManagerSharedSE  {

    /**
     *
     * Manage all P2P service discovery operation, from files to normal service.
     *

     */
    private Context context;

    private Map<Context, ServiceConnection> serviceConnectionMap;

    public static final String SERVICE_NAME="ustadMobile";

    public static final String PREFKEY_SUPERNODE = "supernode_enabled";


    public Map<Context, ServiceConnection> getServiceConnectionMap() {
        return serviceConnectionMap;
    }

    public void setServiceConnectionMap(Map<Context, ServiceConnection> serviceConnectionMap) {
        this.serviceConnectionMap = serviceConnectionMap;
    }

    /**
     * This needs to be called by P2PServiceAndroid once the p2pservice has started
     */
    public void init(Object context) {
        boolean supernodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                PREFKEY_SUPERNODE, "false", context));
        setSuperNodeEnabled(context, supernodeEnabled);
    }

    public P2PServiceAndroid getService(Object context) {
        if(context instanceof P2PServiceAndroid) {
            //the context itself is the service
            return (P2PServiceAndroid)context;
        }else {
            UstadMobileSystemImplAndroid.BaseServiceConnection connection =
                    (UstadMobileSystemImplAndroid.BaseServiceConnection)serviceConnectionMap.get(context);
            P2PServiceAndroid.LocalServiceBinder binder = (P2PServiceAndroid.LocalServiceBinder)connection.getBinder();
            return binder.getService();
        }
    }

    public static ServiceData makeServiceData() {
        HashMap<String,String> record=new HashMap<>();
        record.put("available","available");
        ServiceData serviceData=new ServiceData(SERVICE_NAME,8001,record, ServiceType.PRESENCE_TCP);
        return serviceData;
    }


    @Override
    public void setSuperNodeEnabled(Object context, boolean enabled) {
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
    public P2PNode[] getSuperNodes(Object context) {
        return new P2PNode[0];
    }

    @Override
    public void setClientEnabled(Object context, boolean enabled) {

    }

    @Override
    public boolean isSuperNodeAvailable(Object context) {

        return false;
    }

    @Override
    public boolean isFileAvailable(Object context, String fileId) {

        return false;
    }

    @Override
    public int requestDownload(Object context, DownloadRequest request) {

        return 0;
    }

    @Override
    public void stopDownload(Object context, int requestId, boolean delete) {

    }

    @Override
    public int[] getRequestStatus(Object context, int requestId) {
        return new int[0];
    }

    @Override
    public int getStatus(Object context) {
        return 0;
    }


}
