package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Created by mike on 2/2/18.
 */

public class EntryStatusResponseWithNode extends EntryStatusResponse{

    @UmEmbedded
    private NetworkNode networkNode;

    public EntryStatusResponseWithNode() {

    }

    public EntryStatusResponseWithNode(NetworkNode networkNode) {
        this.networkNode = networkNode;
    }


    public NetworkNode getNetworkNode() {
        return networkNode;
    }

    public void setNetworkNode(NetworkNode networkNode) {
        this.networkNode = networkNode;
    }
}
