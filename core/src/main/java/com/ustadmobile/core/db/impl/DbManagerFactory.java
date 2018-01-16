package com.ustadmobile.core.db.impl;

import com.ustadmobile.core.db.DbManager;

/**
 * Created by mike on 1/13/18.
 */

public class DbManagerFactory {

    public static DbManager makeDbManager(Object context) {
        throw new RuntimeException ("DbManagerFactory core: must use implementation");
    }

}
