package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsFeed;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.android.db.dao.OpdsEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryWithRelationsDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsFeedDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsFeedWithRelationsDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsLinkDaoAndroid;

/**
 * Created by mike on 1/14/18.
 */
@Database(version = 1, entities =  {
        OpdsEntry.class, OpdsFeed.class, OpdsLink.class
})
public abstract class AppDatabase extends RoomDatabase {



    public abstract OpdsFeedDaoAndroid getOpdsFeedDao();

    public abstract OpdsFeedWithRelationsDaoAndroid getOpdsFeedWithRelationsDao();

    public abstract OpdsEntryDaoAndroid getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDaoAndroid getOpdsEntryWithRelationsDao();

    public abstract OpdsLinkDaoAndroid getOpdsLinkDao();

}
