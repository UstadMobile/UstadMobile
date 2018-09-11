package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;

import java.util.List;

@UmDao
public abstract class PersonDetailPresenterFieldDao implements BaseDao<PersonDetailPresenterField>{

    @Override
    @UmInsert
    public abstract long insert(PersonDetailPresenterField entity);

    @Override
    @UmInsert
    public abstract void insertAsync(PersonDetailPresenterField entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * FROM PersonDetailPresenterField WHERE personDetailPresenterFieldUid = :uid")
    public abstract PersonDetailPresenterField findByUid(long uid);

    @UmQuery("SELECT * FROM PersonDetailPresenterField ORDER BY fieldIndex")
    public abstract void findAllPersonDetailPresenterFields(UmCallback<List<PersonDetailPresenterField>> callback);

}
