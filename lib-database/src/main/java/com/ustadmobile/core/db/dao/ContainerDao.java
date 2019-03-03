package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
@UmRepository
public abstract class ContainerDao implements SyncableDao<Container, ContainerDao> {

   // public abstract List<Container> getContainersForContentEntry(long contentEntryUid);

   // public abstract Container getMostRecentContainerForContentEntry(long contentEntry);
}
