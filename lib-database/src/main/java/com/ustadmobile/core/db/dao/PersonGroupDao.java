package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.GroupWithMemberCount;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class PersonGroupDao implements SyncableDao<PersonGroup, PersonGroupDao> {

    @UmQuery("SELECT *, 0 AS memberCount FROM PersonGroup")
    public abstract UmProvider<GroupWithMemberCount> findAllGroups();

}
