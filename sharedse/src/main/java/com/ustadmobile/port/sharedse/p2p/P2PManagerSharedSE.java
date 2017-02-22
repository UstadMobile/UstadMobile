package com.ustadmobile.port.sharedse.p2p;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import java.util.HashMap;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class P2PManagerSharedSE implements Runnable, P2PManager {


    /**
     * Map of available supernodes mapped as node to index file
     */
    private HashMap<P2PNode, UstadJSOPDSFeed> availableIndexes;

    private Thread runThread;

    private boolean running;



    /**
     * start appropriate threads
     * */
    public void start() {
        if(runThread == null) {
            running = true;
            runThread = new Thread(this);
            runThread.start();
            System.out.print("Service started");
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

            System.out.print("Service started is running");
        }
    }

    protected abstract void init(P2PActionListener listener);

    protected abstract void addLocalService(P2PActionListener listener);

    protected abstract void removeLocalService(P2PActionListener listener);

    protected abstract void prepareServiceDiscovery(P2PActionListener listener);

    protected abstract void startServiceDiscovery(P2PActionListener listener);

    protected abstract void stopServiceDiscovery(P2PActionListener listener);

    protected abstract void discoverPeers();

    /**
     * Set if supernode mode is enabled or not (by default this is disabled)
     *
     * @param enabled
     */
    public abstract void setSuperNodeEnabled(boolean enabled);

    /**
     *  Set if normal client mode is enabled (enabled by default on platforms that support it)
     *
     *  @param enabled
     */
    public abstract void setClientEnabled(boolean enabled);

    /**
     * check if there is super node around
     * */
    public abstract boolean isSuperNodeAvailable();

    /**
     * check if the file is available locally
     * */
    public abstract boolean isFileAvailable(String fileId);

    /**
     * request to download a file from super node
     *
     */
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
     * request to status of the peer to peer environment
     * */
    public abstract int getStatus();


}
