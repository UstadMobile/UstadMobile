package com.ustadmobile.port.sharedse.p2p;

/**
 * Created by kileha3 on 05/03/2017.
 */
public interface P2PNodeListener {

    /**
     * A p2p node has been discovered and is available
     *
     * @param node
     */
    void nodeDiscovered(P2PNode node);

    /**
     * A p2p node that was discovered is now gone
     * @param node
     */
    void nodeGone(P2PNode node);

}
