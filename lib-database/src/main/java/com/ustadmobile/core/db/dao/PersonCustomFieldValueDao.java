package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue;

import java.util.List;

@UmDao
public abstract class PersonCustomFieldValueDao implements BaseDao<PersonCustomFieldValue> {

    @Override
    @UmInsert
    public abstract long insert(PersonCustomFieldValue entity);

    @Override
    @UmInsert
    public abstract void insertAsync(PersonCustomFieldValue entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * FROM PersonCustomFieldValue WHERE personCustomFieldValueUid = :uid")
    public abstract PersonCustomFieldValue findByUid(long uid);


    @UmQuery("SELECT * FROM PersonField " +
            "LEFT JOIN PersonCustomFieldValue ON " +
            "PersonCustomFieldValue.personCustomFieldValuePersonCustomFieldUid = PersonField.personCustomFieldUid " +
            "WHERE personCustomFieldValuePersonUid = :personUid")
    public abstract void findByPersonUidAsync2(long personUid,
                                              UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>> callback);



}
