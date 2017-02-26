package com.ustadmobile.port.sharedse.p2p;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import java.util.HashMap;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class P2PManagerSharedSE implements P2PManager {


    /**
     * Map of available supernodes mapped as node to index file
     */
    private HashMap<P2PNode, UstadJSOPDSFeed> availableIndexes;



    /**
    These methods are really a bit too system specific and therefor will not be part of any public API
     They are just here temporarily as reminders
    protected abstract void init(P2PActionListener listener);

    protected abstract void addLocalService(P2PActionListener listener);

    protected abstract void removeLocalService(P2PActionListener listener);

    protected abstract void prepareServiceDiscovery(P2PActionListener listener);

    protected abstract void startServiceDiscovery(P2PActionListener listener);

    protected abstract void stopServiceDiscovery(P2PActionListener listener);

    protected abstract void discoverPeers();
     */

    /**
     * Set if supernode mode is enabled or not (by default this is disabled)
     *
     * @param enabled
     */
    public abstract void setSuperNodeEnabled(Object context, boolean enabled);

    public abstract P2PNode[] getSuperNodes(Object context);

    /**
     *  Set if normal client mode is enabled (enabled by default on platforms that support it)
     *
     *  @param enabled
     */
    public abstract void setClientEnabled(Object context, boolean enabled);

    /**
     * check if there is super node around
     * */
    public abstract boolean isSuperNodeAvailable(Object context);

    /**
     * check if the file is available locally
     * */
    public abstract boolean isFileAvailable(Object context, String fileId);

    /**
     * request to download a file from super node
     *
     */
    public abstract int requestDownload(Object context, DownloadRequest request);

    /**
     * stop the download once has been started
     * */
    public abstract void stopDownload(Object context, int requestId, boolean delete);

    /**
     * request for the download status
     * */
    public abstract int[] getRequestStatus(Object context, int requestId);

    /**
     * request to status of the peer to peer environment
     * */
    public abstract int getStatus(Object context);





}
