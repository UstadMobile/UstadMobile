package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import static com.ustadmobile.core.db.dao.RoleDao.SELECT_ACCOUNT_IS_ADMIN;

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class RoleDao implements SyncableDao<Role, RoleDao> {

    public static final String SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)";

}
