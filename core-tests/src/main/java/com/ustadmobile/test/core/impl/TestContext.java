package com.ustadmobile.test.core.impl;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.io.File;
import java.util.Properties;

/**
 * Created by mike on 5/13/17.
 */

public class TestContext {

    private Properties appProps;

    private Properties userPrefs;

    private String contextName;

    public TestContext(String contextName) {
        appProps = new Properties();
        userPrefs = new Properties();
        this.contextName = contextName;
    }


    public Properties getAppProps() {
        return appProps;
    }

    public Properties getUserPrefs() {
        return userPrefs;
    }

    /**
     * The context name: used in directory paths to separate out different contexts
     *
     * @return Context name as above
     */
    public String getContextName() {
        return contextName;
    }


}
