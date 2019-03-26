package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class CustomFieldValueOptionDao
    implements SyncableDao<CustomFieldValueOption, CustomFieldValueOptionDao> {

    @UmQuery("SELECT * FROM CustomFieldValueOption " +
            " WHERE customFieldValueOptionFieldUid = :customFieldUid " +
            " AND customFieldValueOptionActive = 1")
    public abstract UmProvider<CustomFieldValueOption> findAllOptionsForField(long customFieldUid);

    @UmQuery("UPDATE CustomFieldValueOption SET customFieldValueOptionActive = 0 WHERE" +
            " customFieldValueOptionUid = :uid")
    public abstract void deleteOption(long uid, UmCallback<Integer> resultObjecT);
}
