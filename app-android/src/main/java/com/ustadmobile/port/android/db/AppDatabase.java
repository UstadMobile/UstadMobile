package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.android.db.dao.OpdsEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryParentToChildJoinDaoAndriod;
import com.ustadmobile.port.android.db.dao.OpdsEntryWithRelationsDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsLinkDaoAndroid;


/**
 * Created by mike on 1/14/18.
 */
@Database(version = 1, entities =  {
        OpdsEntry.class, OpdsLink.class, OpdsEntryParentToChildJoin.class
})
public abstract class AppDatabase extends RoomDatabase {

    public abstract OpdsEntryDaoAndroid getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDaoAndroid getOpdsEntryWithRelationsDao();

    public abstract OpdsLinkDaoAndroid getOpdsLinkDao();

    public abstract OpdsEntryParentToChildJoinDaoAndriod getOpdsEntryParentToChildJoinDao();

}
