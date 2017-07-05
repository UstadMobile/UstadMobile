package com.ustadmobile.core.networkmanager;

/**
 * <h1>NetworkManagerTaskListener</h1>
 *
 * This is an interface which monitor and manages all Network tasks
 *
 * @author kileha3
 */

public interface NetworkManagerTaskListener {

    /**
     * Indicate that Network task execution is finished
     * @param networkTask Network task which was executed.
     */
    void networkTaskStatusChanged(NetworkTask networkTask);
}
