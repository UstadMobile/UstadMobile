package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.CustomFieldValue;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class CustomFieldValueDao
        implements SyncableDao<CustomFieldValue, CustomFieldValueDao> {

    //TODO: Wrong , Check it:
    @UmQuery("SELECT * FROM CustomFieldValue " +
            " LEFT JOIN CustomField ON CustomField.customFieldUid = CustomFieldValue.customFieldValueFieldUid " +
            " WHERE customFieldValueEntityUid = :uid AND" +
            " CustomField.customFieldEntityType = :type LIMIT 1")
    public abstract void findByEntityTypeAndUid(int type, long uid, UmCallback<CustomFieldValue> resultCallback);


    @UmQuery("SELECT * FROM CustomFieldValue WHERE customFieldValueFieldUid = :fieldUid AND " +
            " customFieldValueEntityUid = :entityUid ")
    public abstract void findValueByCustomFieldUidAndEntityUid(long fieldUid, long entityUid, UmCallback<CustomFieldValue> resultCallback);

    @UmQuery("SELECT * FROM CustomFieldValue WHERE customFieldValueFieldUid = :fieldUid AND " +
            " customFieldValueEntityUid = :entityUid ")
    public abstract CustomFieldValue findValueByCustomFieldUidAndEntityUidSync(long fieldUid, long entityUid);


}
