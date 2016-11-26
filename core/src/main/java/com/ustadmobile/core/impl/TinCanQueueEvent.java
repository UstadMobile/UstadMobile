package com.ustadmobile.core.impl;

/**
 * Created by mike on 11/11/16.
 */

public class TinCanQueueEvent {

    private int queueSize;

    public TinCanQueueEvent(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

}
