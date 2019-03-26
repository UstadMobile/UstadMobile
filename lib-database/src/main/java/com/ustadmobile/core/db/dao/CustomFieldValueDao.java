package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.CustomFieldValue;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class CustomFieldValueDao
        implements SyncableDao<CustomFieldValue, CustomFieldValueDao> {

}
