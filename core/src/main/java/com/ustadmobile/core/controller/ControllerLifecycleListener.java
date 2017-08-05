package com.ustadmobile.core.controller;

/**
 * Created by mike on 8/3/17.
 */

public interface ControllerLifecycleListener {

    void onStarted(UstadBaseController controller);

    void onStopped(UstadBaseController controller);

    void onDestroyed(UstadBaseController controller);

}
