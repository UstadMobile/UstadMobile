package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsFeed;
import com.ustadmobile.port.android.db.dao.OpdsEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryWithRelationsDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsFeedDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsFeedWithRelationsDaoAndroid;

/**
 * Created by mike on 1/14/18.
 */
@Database(version = 1, entities =  {
        OpdsEntry.class, OpdsFeed.class
})
public abstract class AppDatabase extends RoomDatabase {



    public abstract OpdsFeedDaoAndroid getOpdsFeedDao();

    public abstract OpdsFeedWithRelationsDaoAndroid getOpdsFeedWithRelationsDao();

    public abstract OpdsEntryDaoAndroid getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDaoAndroid getOpdsEntryWithRelationsDao();

}
