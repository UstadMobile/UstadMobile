package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.PersonEditPresenterField;

import java.util.List;

@UmDao
public abstract class PersonEditPresenterFieldDao implements BaseDao<PersonEditPresenterField>{

    @Override
    @UmInsert
    public abstract long insert(PersonEditPresenterField entity);

    @Override
    @UmInsert
    public abstract void insertAsync(PersonEditPresenterField entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * FROM PersonEditPresenterField WHERE personEditPresenterFieldUid = :uid")
    public abstract PersonEditPresenterField findByUid(long uid);

    @UmQuery("SELECT * FROM PersonEditPresenterField ORDER BY personEditPresenterFieldIndex")
    public abstract void findAllPersonEditPresenterFields(
            UmCallback<List<PersonEditPresenterField>> callback);

}
