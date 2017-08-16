package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
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

    public SendCoursePresenter(Object context, Hashtable args, SendCourseView view) {
        super(context);
        this.view = view;
    }

    public void onCreate(Hashtable savedState) {

    }

    public void onStart() {
        NetworkManager networkManager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        networkManager.addWifiDirectPeersListener(this);
        networkManager.addWifiDirectGroupListener(this);
        networkManager.setSendingOn(true);

    }

    public void onStop() {
        NetworkManager networkManager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        networkManager.removeWifiDirectPeersListener(this);
        networkManager.setSendingOn(false);
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
    }

    @Override
    public void groupCreated(WiFiDirectGroup group, Exception err) {
        //check if the client is in the list
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                "Group created: ", AppView.LENGTH_LONG);
    }

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }

    public void handleClickReceiver(String id) {
        UstadMobileSystemImplSE.getInstanceSE().getNetworkManager().connectToWifiDirectNode(id);
    }
}
