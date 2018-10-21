package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
public abstract class ExampleSyncableDao implements SyncableDao<ExampleSyncableEntity, ExampleSyncableDao>  {

}
