package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class PersonDetailPresenterFieldDao implements
        SyncableDao<PersonDetailPresenterField, PersonDetailPresenterFieldDao> {

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
    public abstract void findAllPersonDetailPresenterFields(
            UmCallback<List<PersonDetailPresenterField>> callback);

    @UmQuery("SELECT * FROM PersonDetailPresenterField WHERE viewModeVisible = 1 ORDER BY fieldIndex")
    public abstract void findAllPersonDetailPresenterFieldsViewMode(
            UmCallback<List<PersonDetailPresenterField>> callback);

    @UmQuery("SELECT * FROM PersonDetailPresenterField WHERE editModeVisible = 1 ORDER BY fieldIndex")
    public abstract void findAllPersonDetailPresenterFieldsEditMode(
            UmCallback<List<PersonDetailPresenterField>> callback);

    @UmQuery("SELECT * FROM PersonDetailPresenterField WHERE fieldUid = :uid")
    public abstract void findAllByFieldUid(long uid, UmCallback<List<PersonDetailPresenterField>> resultList);

    @UmQuery("SELECT * FROM PersonDetailPresenterField WHERE labelMessageId = :id")
    public abstract void findAllByLabelMessageId(int id, UmCallback<List<PersonDetailPresenterField>> resultList);

    @UmQuery("SELECT * FROM PersonDetailPresenterField WHERE fieldIndex = :id")
    public abstract void findAllByFieldIndex(int id, UmCallback<List<PersonDetailPresenterField>> resultList);
}
