package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
@UmRepository
public abstract class ContainerDao implements SyncableDao<Container, ContainerDao> {

    //public abstract List<Container> getContainersForContentEntry(long contentEntryUid);

    @UmQuery("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    public abstract void getMostRecentContainerForContentEntry(long contentEntry, UmCallback<Container> callback);

    @UmQuery("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntryUid " +
            "ORDER BY Container.lastModified DESC")
    public abstract void findFilesByContentEntryUid(long contentEntryUid, UmCallback<List<Container>> callback);


    @UmQuery("SELECT Container.* FROM Container " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    public abstract List<Container> findAllPublikContainers();

    @UmQuery("SELECT * From Container WHERE Container.containerUid = :containerUid")
    public abstract void findByUid(long containerUid, UmCallback<Container> containerUmCallback);




}
