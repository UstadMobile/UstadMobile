package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.PersonLocationJoin;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(inheritPermissionFrom = PersonDao.class,
inheritPermissionForeignKey = "personLocationPersonUid",
inheritPermissionJoinedPrimaryKey = "personUid")
@UmRepository
public abstract class PersonLocationJoinDao implements SyncableDao<PersonLocationJoin, PersonLocationJoinDao> {


}
