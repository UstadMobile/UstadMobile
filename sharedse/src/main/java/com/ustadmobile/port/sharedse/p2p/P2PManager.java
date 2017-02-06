package com.ustadmobile.port.sharedse.p2p;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.util.HashMap;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class P2PManager implements Runnable{


    /**
     * Map of available supernodes mapped as node to index file
     */
    private HashMap<P2PNode, UstadJSOPDSFeed> availableIndexes;

    private Thread runThread;

    private boolean running;

    public static final String P2P_SERVICE_NAME = "_ustadnode";

    public static final String P2P_REGISTRATION_TYPE = P2P_SERVICE_NAME + "._tcp";


    /**
     * start appropriate threads
     * */
    public void start() {
        if(runThread == null) {
            running = true;
            runThread = new Thread(this);
            runThread.start();
        }
    }

    /**
     * stop appropriate threads
     * */
    public void stop() {
        if(runThread != null) {
            running = false;
        }
    }

    public void run() {
        init(new P2PActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int errorCode) {

            }
        });


        if(running) {

        }
    }

    protected abstract void init(P2PActionListener listener);

    protected abstract void addLocalService(P2PActionListener listener,int nodeType);

    protected abstract void removeLocalService(P2PActionListener listener);

    protected abstract void prepareServiceDiscovery(P2PActionListener listener);

    protected abstract void startServiceDiscovery(P2PActionListener listener);

    protected abstract void stopServiceDiscovery(P2PActionListener listener);

    protected abstract void discoverPeers();//change this to have a suitable listener


    /**
     * check if there is super node around
     * */
    public abstract boolean IsSuperNodeAvailable();

    /**
     * check if the file is available locally
     * */
    public abstract boolean isFileAvailable(String fileId);

    /**
     * request to download a file from super node
     * */
    public abstract int requestDownload(DownloadRequest request);

    /**
     * stop the download once has been started
     * */
    public abstract void stopDownload(int requestId, boolean delete);

    /**
     * request for the download status
     * */
    public abstract int[] getRequestStatus(int requestId);

    /**
     * request to download state status
     * */
    public abstract int getStatus();

    /**
     * opt for the client or server mode
     * */
    public abstract void setSuperNodeEnabled(boolean enabled);
}
