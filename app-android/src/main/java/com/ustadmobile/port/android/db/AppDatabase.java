package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.lib.db.entities.*;
import com.ustadmobile.port.android.db.dao.ContainerFileDaoAndroid;
import com.ustadmobile.port.android.db.dao.ContainerFileEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.EntryStatusResponseDaoAndroid;
import com.ustadmobile.port.android.db.dao.NetworkNodeDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryParentToChildJoinDaoAndriod;
import com.ustadmobile.port.android.db.dao.OpdsEntryWithRelationsDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsLinkDaoAndroid;


/**
 * Created by mike on 1/14/18.
 */
@Database(version = 1, entities =  {
        OpdsEntry.class, OpdsLink.class, OpdsEntryParentToChildJoin.class,
        ContainerFile.class, ContainerFileEntry.class, DownloadJob.class,
        DownloadJobItem.class, NetworkNode.class, EntryStatusResponse.class

})
public abstract class AppDatabase extends RoomDatabase {

    public abstract OpdsEntryDaoAndroid getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDaoAndroid getOpdsEntryWithRelationsDao();

    public abstract OpdsLinkDaoAndroid getOpdsLinkDao();

    public abstract OpdsEntryParentToChildJoinDaoAndriod getOpdsEntryParentToChildJoinDao();

    public abstract ContainerFileEntryDaoAndroid getContainerFileEntryDao();

    public abstract ContainerFileDaoAndroid getContainerFileDao();

    public abstract NetworkNodeDaoAndroid getNetworkNodeDao();

    public abstract EntryStatusResponseDaoAndroid getEntryStatusResponseDao();

}
