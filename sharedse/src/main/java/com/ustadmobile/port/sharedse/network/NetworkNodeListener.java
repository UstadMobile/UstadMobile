package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 05/03/2017.
 */
public interface NetworkNodeListener {

    /**
     * A node has been discovered and is available
     *
     * @param node
     */
    void nodeDiscovered(NetworkNode node);

    /**
     * A node that was discovered is now gone
     * @param node
     */
    void nodeGone(NetworkNode node);

}
