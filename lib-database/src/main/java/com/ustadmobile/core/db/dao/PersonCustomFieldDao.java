package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;


@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class PersonCustomFieldDao
        implements SyncableDao<PersonField, PersonCustomFieldValueDao> {

    @UmInsert
    public abstract long insert(PersonField entity);

    @UmInsert
    public abstract void insertAsync(PersonField entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * FROM PersonField WHERE personCustomFieldUid = :uid")
    public abstract PersonField findByUid(long uid);

    @UmQuery("SELECT * FROM PersonField WHERE personCustomFieldUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<PersonField> result);

    @UmQuery("SELECT MAX(personCustomFieldUid) FROM PersonField")
    public abstract int findLatestUid();

    @UmQuery("SELECT * FROM PersonField WHERE personCustomFieldUid > :minCustomFieldUid")
    public abstract void findAllCustomFields(int minCustomFieldUid,
                                             UmCallback<List<PersonField>> result);
}
