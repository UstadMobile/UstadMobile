package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
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

public class ReceiveCoursePresenter extends UstadBaseController implements WifiP2pListener, Runnable {

    private ReceiveCourseView view;

    private UstadJSOPDSFeed sharedFeed;

    private NetworkManager networkManager;

    public static final int MAX_LOAD_COURSE_ATTEMPTS = 3;

    public static final int LOAD_COURSE_RETRY_INTERVAL = 2000;

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

        new Thread(this).start();
    }

    public void run() {
        UstadJSOPDSFeed loadedFeed = null;
        for(int i = 0; loadedFeed == null && i < MAX_LOAD_COURSE_ATTEMPTS; i++) {
            try {
                loadedFeed = networkManager.getOpdsFeedSharedByWifiP2pGroupOwner();
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 665, "ReceiveCoursePresenter: exception", e);
            }catch(XmlPullParserException x) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 665, "ReceiveCoursePresenter: exception", x);
            }

            if(loadedFeed == null) {
                try { Thread.sleep(LOAD_COURSE_RETRY_INTERVAL); }
                catch(InterruptedException e) {}
            }
        }

        if(loadedFeed != null) {
            sharedFeed = loadedFeed;
            view.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setSharedCourseName(sharedFeed.getTitle());
                    view.setMode(ReceiveCourseView.MODE_ACCEPT_DECLINE);
                }
            });
        }else {
            view.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setMode(ReceiveCourseView.MODE_CONNECTED_BUT_NOT_SHARING);
                }
            });
        }
    }


    @Override
    public void setUIStrings() {

    }

    @Override
    public void peersChanged(List<NetworkNode> peers) {

    }

    @Override
    public void wifiP2pConnectionChanged(boolean connected) {
        if(connected && networkManager.getWifiDirectGroupOwnerIp() != null)
            loadSharedCourse();
    }

    @Override
    public void wifiP2pConnectionResult(String macAddr, boolean connected) {

    }

    public void handleClickAccept() {
        String destinationDir= UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, getContext())[0].getDirURI();
        sharedFeed.addLink(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
                "application/dir", destinationDir);
        networkManager.requestAcquisition(sharedFeed, true, true);
    }

    public void handleClickDecline() {
        networkManager.removeWiFiDirectGroup();
    }

    public void handleClickDisconnect() {
        networkManager.removeWiFiDirectGroup();
        view.setWaitingStatusText(MessageID.waiting_for_sender);
        view.setMode(ReceiveCourseView.MODE_WAITING);
    }

}
