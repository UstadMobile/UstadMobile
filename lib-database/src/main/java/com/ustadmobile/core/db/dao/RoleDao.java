package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class RoleDao implements SyncableDao<Role, RoleDao> {

}
