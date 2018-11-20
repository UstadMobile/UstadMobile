package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.PersonCustomField;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

@UmDao
public abstract class PersonCustomFieldDao implements BaseDao<PersonCustomField> {

    @UmInsert
    public abstract long insert(PersonCustomField entity);

    @UmInsert
    public abstract void insertAsync(PersonCustomField entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * FROM PersonCustomField WHERE personCustomFieldUid = :uid")
    public abstract PersonCustomField findByUid(long uid);
}
