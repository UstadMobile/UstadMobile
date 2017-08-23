package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WifiP2pListener;
import com.ustadmobile.port.sharedse.view.ReceiveCourseView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by mike on 8/22/17.
 */

public class ReceiveCoursePresenter extends UstadBaseController implements WifiP2pListener {

    private ReceiveCourseView view;

    private UstadJSOPDSFeed sharedFeed;

    private NetworkManager networkManager;

    public ReceiveCoursePresenter(Object context, ReceiveCourseView view) {
        super(context);
        this.view = view;
    }



    @Override
    public void onStart() {
        networkManager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        networkManager.setReceivingOn(true);
        networkManager.addWifiDirectPeersListener(this);
        NetworkNode thisNode = networkManager.getThisWifiDirectDevice();
        if(thisNode != null) {
            view.setDeviceName(thisNode.getDeviceWifiDirectName());
        }
        view.setMode(ReceiveCourseView.MODE_WAITING);

        super.onStart();
        if(networkManager.getWifiDirectGroupOwnerIp() != null) {
            loadSharedCourse();
        }
    }

    @Override
    public void onStop() {
        networkManager.setReceivingOn(false);
        networkManager.removeWifiDirectPeersListener(this);
        super.onStop();
    }

    public void loadSharedCourse() {
        view.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setWaitingStatusText(MessageID.loading);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sharedFeed = networkManager.getOpdsFeedSharedByWifiP2pGroupOwner();
                    view.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setSharedCourseName(sharedFeed.title);
                            view.setMode(ReceiveCourseView.MODE_ACCEPT_DECLINE);
                        }
                    });
                }catch(IOException e) {
                    e.printStackTrace();
                }catch(XmlPullParserException x) {
                    x.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void setUIStrings() {

    }

    @Override
    public void peersChanged(List<NetworkNode> peers) {

    }

    @Override
    public void wifiP2pConnectionChanged(boolean connected) {
        if(connected)
            loadSharedCourse();
    }

    public void handleClickAccept() {
        String destinationDir= UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogController.SHARED_RESOURCE, getContext())[0].getDirURI();
        sharedFeed.addLink(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
                "application/dir", destinationDir);
        networkManager.requestAcquisition(sharedFeed, true, true);
    }

    public void handleClickDecline() {
        networkManager.removeWiFiDirectGroup();
    }


}
