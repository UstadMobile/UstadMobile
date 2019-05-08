package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class CustomFieldDao implements SyncableDao<CustomField, CustomFieldDao> {

    @UmQuery("SELECT * FROM CustomField WHERE customFieldUid = :uid")
    public abstract UmLiveData<CustomField> findByUidLive(long uid);

    @UmQuery("SELECT * FROM CustomField WHERE customFieldUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<CustomField> resultObject);

    @UmUpdate
    public abstract void updateAsync(CustomField entity, UmCallback<Integer> resultObjcet);

    @UmQuery("SELECT * FROM CustomField WHERE customFieldEntityType = :tableId AND " +
            " customFieldActive = 1")
    public abstract UmProvider<CustomField> findAllCustomFieldsProviderForEntity(int tableId);

    @UmQuery("SELECT * FROM CustomField WHERE customFieldEntityType = :tableId AND " +
            " customFieldActive = 1")
    public abstract void findAllCustomFieldsProviderForEntityAsync(int tableId,
                                                  UmCallback<List<CustomField>> listResultCallback);

    @UmQuery("UPDATE CustomField SET customFieldActive = 0 WHERE customFieldUid = :customFieldUid")
    public abstract void deleteCustomField(long customFieldUid, UmCallback<Integer> resultCallback);

    @UmQuery("SELECT * FROM CustomField WHERE customFieldName = :fieldName COLLATE NOCASE AND " +
            "customFieldEntityType = :tableId AND customFieldActive = 1 ")
    public abstract void findByFieldNameAndEntityTypeAsync(String fieldName, int tableId,
                                               UmCallback<List<CustomField>> listResultCallback);

    //For debugging

    @UmQuery("SELECT * FROM CustomField")
    public abstract List<CustomField> findAll();
}
