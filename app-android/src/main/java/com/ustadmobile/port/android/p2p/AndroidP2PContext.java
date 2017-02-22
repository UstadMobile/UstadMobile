package com.ustadmobile.port.android.p2p;


import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Interface implemented by UstadBaseActivity and any service that for any reason needs to obtain
 * a reference to the p2p service
 */

public interface AndroidP2PContext {

    WifiDirectHandler getP2PService();

}
