package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;

@UmDatabase
public abstract class UmAppDatabase {

    private static UmAppDatabase instance;

    public static UmAppDatabase getInstance(Object context) {
        if(instance == null){
            instance = com.ustadmobile.core.db.UmAppDatabase_Factory.makeUmAppDatabase(context);
        }

        return instance;
    }

    public abstract OpdsEntryDao getOpdsEntryDao();

}
