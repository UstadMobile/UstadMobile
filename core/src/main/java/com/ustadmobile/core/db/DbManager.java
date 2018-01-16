package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsFeedWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.db.impl.DbManagerFactory;
import com.ustadmobile.core.db.dao.OpdsFeedDao;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

/**
 * Created by mike on 1/13/18.
 */

public abstract class DbManager {

    private static DbManager instance;


    public DbManager() {
    }

    public static DbManager getInstance(Object context){
        if(instance == null)
            instance = DbManagerFactory.makeDbManager(context);

        return instance;
    }


    public abstract OpdsFeedDao getOpdsFeedDao();

    public abstract OpdsFeedWithRelationsDao getOpdsFeedRepository();

    public abstract OpdsFeedWithRelationsDao getOpdsFeedWithRelationsDao();

    public abstract OpdsFeedWithRelationsDao getOpdsFeedWithRelationsRepository();

    public abstract OpdsEntryDao getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDao getOpdsEntryWithRelationsDao();

    public abstract OpdsLinkDao getOpdsLinkDao();


    public abstract Object getContext();



}
