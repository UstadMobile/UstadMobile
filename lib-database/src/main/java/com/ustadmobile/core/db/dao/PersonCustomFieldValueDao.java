package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class PersonCustomFieldValueDao implements
        SyncableDao<PersonCustomFieldValue, PersonCustomFieldValueDao> {

    @Override
    @UmInsert
    public abstract long insert(PersonCustomFieldValue entity);

    @Override
    @UmInsert
    public abstract void insertAsync(PersonCustomFieldValue entity, UmCallback<Long> resultObject);

    @Override
    @UmQuery("SELECT * FROM PersonCustomFieldValue WHERE personCustomFieldValueUid = :uid")
    public abstract PersonCustomFieldValue findByUid(long uid);

    @UmQuery("SELECT * FROM PersonCustomFieldValue WHERE " +
            "personCustomFieldValuePersonUid = :personUid AND " +
            "personCustomFieldValuePersonCustomFieldUid = :fieldUid")
    public abstract void findCustomFieldByFieldAndPersonAsync(long fieldUid, long personUid,
                                                      UmCallback<PersonCustomFieldValue> resultObject);

    @UmUpdate
    public abstract void updateAsync(PersonCustomFieldValue entity, UmCallback<Integer> resultObject);

    @UmUpdate
    public abstract void updateListAsync(List<PersonCustomFieldValue> entities,
                                         UmCallback<Integer> callback);


    @UmQuery("SELECT * FROM PersonField " +
            "LEFT JOIN PersonCustomFieldValue ON " +
            "PersonCustomFieldValue.personCustomFieldValuePersonCustomFieldUid = " +
            " PersonField.personCustomFieldUid " +
            "WHERE personCustomFieldValuePersonUid = :personUid")
    public abstract void findByPersonUidAsync2(long personUid,
                          UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>> callback);



}
