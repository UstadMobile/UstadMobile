package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroupListener;
import com.ustadmobile.port.sharedse.networkmanager.WifiP2pListener;
import com.ustadmobile.port.sharedse.view.SendCourseView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by mike on 8/15/17.
 */

public class SendCoursePresenter extends UstadBaseController implements WifiP2pListener, WiFiDirectGroupListener {

    private SendCourseView view;

    private String sendTitle;

    private String[] sharedEntries;

    public static final String ARG_SEND_TITLE = "title";

    public static final String ARG_ENTRY_IDS = "entries";

    private String chosenMacAddr = null;

    private boolean invitationCancelled = false;

    public SendCoursePresenter(Object context, Hashtable args, SendCourseView view) {
        super(context);
        this.view = view;
        sendTitle = args.containsKey(ARG_SEND_TITLE) ? args.get(ARG_SEND_TITLE).toString() : "Shared courses";
        sharedEntries = (String[])args.get(ARG_ENTRY_IDS);
    }

    public void onCreate(Hashtable savedState) {

    }

    public void onStart() {
        NetworkManager networkManager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        peersChanged(networkManager.getKnownWifiDirectPeers());
        networkManager.addWifiDirectPeersListener(this);
        networkManager.addWifiDirectGroupListener(this);
        networkManager.setSharedFeed(sharedEntries, sendTitle);
    }

    public void onStop() {
        NetworkManager networkManager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        networkManager.removeWifiDirectPeersListener(this);
        networkManager.removeWifiDirectGroupListener(this);
        networkManager.setSharedFeed(null);
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void peersChanged(List<NetworkNode> peers) {
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for(NetworkNode peer : peers){
            ids.add(peer.getDeviceWifiDirectMacAddress());
            names.add(peer.getDeviceWifiDirectName());
        }

        view.setReceivers(ids, names);
        for(NetworkNode peer : peers) {
            view.setReceiverStatus(peer.getDeviceWifiDirectMacAddress(),
                    peer.getWifiDirectDeviceStatus());


            if(chosenMacAddr != null
                    && chosenMacAddr.equalsIgnoreCase(peer.getDeviceWifiDirectMacAddress())) {

                switch(peer.getWifiDirectDeviceStatus()) {
                    case NetworkNode.STATUS_FAILED:
                    case NetworkNode.STATUS_UNAVAILABLE:
                    case NetworkNode.STATUS_AVAILABLE:
                        //connection has actually failed
                        handleAttemptFailed(invitationCancelled);
                        break;
                }
            }
        }
    }

    @Override
    public void wifiP2pConnectionChanged(boolean connected) {

    }

    @Override
    public void groupCreated(WiFiDirectGroup group, Exception err) {
        if(chosenMacAddr != null && group.groupIncludes(chosenMacAddr)){
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            impl.getAppView(getContext()).showNotification(
                    impl.getString(MessageID.sent, getContext()), AppView.LENGTH_LONG);
            view.dismiss();
        }
    }

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }

    public void handleClickReceiver(String id) {
        if(chosenMacAddr == null) {
            UstadMobileSystemImplSE instanceSE = UstadMobileSystemImplSE.getInstanceSE();
            chosenMacAddr = id;
            view.setStatusText(instanceSE.getString(MessageID.inviting, getContext()));
            view.setReceiversListEnabled(false);
            UstadMobileSystemImplSE.getInstanceSE().getNetworkManager().connectToWifiDirectNode(id);
        }
    }

    @Override
    public void wifiP2pConnectionResult(String macAddr, boolean connected) {
        if(chosenMacAddr != null && chosenMacAddr.equalsIgnoreCase(macAddr)) {
            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            if(connected) {
                UstadMobileSystemImpl.l(UMLog.INFO, 300, "SendCourse: wifi direct connection result: success");
            }else {
                handleAttemptFailed(false);
            }
        }
    }

    protected void handleAttemptFailed(boolean wasCancelled){
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!wasCancelled)
            impl.getAppView(getContext()).showNotification(
                    impl.getString(MessageID.error, getContext()), AppView.LENGTH_LONG);

        view.setReceiversListEnabled(true);
        view.setStatusText(impl.getString(MessageID.scanning, getContext()));
        chosenMacAddr = null;
        invitationCancelled = false;
    }

    public void handleClickCancelInvite(String deviceId) {
        UstadMobileSystemImplSE implSe = UstadMobileSystemImplSE.getInstanceSE();
        implSe.getNetworkManager().cancelWifiDirectConnection();
        invitationCancelled = true;
        view.setReceiversListEnabled(true);
        view.setStatusText(implSe.getString(MessageID.scanning, getContext()));
    }
}
