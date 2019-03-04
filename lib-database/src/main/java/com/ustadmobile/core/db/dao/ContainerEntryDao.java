package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class ContainerEntryDao implements BaseDao<ContainerEntry> {

    @UmQuery("SELECT ContainerEntry.*, ContainerEntryFile.* " +
            "FROM ContainerEntry " +
            "LEFT JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    public abstract List<ContainerEntryWithContainerEntryFile> findByContainer(long containerUid);

    @UmQuery("SELECT ContainerEntry.*, ContainerEntryFile.* " +
            "FROM ContainerEntry " +
            "LEFT JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    public abstract void findByContainer(long containerUid, UmCallback<List<ContainerEntryWithContainerEntryFile>> umCallback);


    @UmQuery("SELECT ContainerEntry.* FROM ContainerEntry " +
            "LEFT JOIN Container ON Container.containerUid = ContainerEntry.ceContainerUid " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = Container.containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    public abstract List<ContainerEntry> findAllPublikContainerEntries();


}
