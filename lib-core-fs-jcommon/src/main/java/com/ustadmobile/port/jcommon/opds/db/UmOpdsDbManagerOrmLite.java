package com.ustadmobile.port.jcommon.opds.db;

import com.ustadmobile.core.opds.db.UmOpdsDbManager;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.port.jcommon.opds.db.entities.UmOpdsLinkEntity;

/**
 * Created by mike on 1/3/18.
 */

public class UmOpdsDbManagerOrmLite extends UmOpdsDbManager {

    @Override
    public Object makeNew(Class interfaceClass) {
        if(interfaceClass.equals(UmOpdsLink.class)) {
            return new UmOpdsLinkEntity();
        }

        return null;
    }
}
