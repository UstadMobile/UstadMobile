package com.ustadmobile.core.db.impl;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.port.android.db.DbManagerAndroid;

/**
 * Created by mike on 1/14/18.
 */

public class DbManagerFactory {

    public static DbManager makeDbManager(Object context) {
        return new DbManagerAndroid(context);
    }
}
