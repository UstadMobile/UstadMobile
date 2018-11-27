package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract  class ClazzActivityChangeDao implements SyncableDao<ClazzActivityChange, ClazzActivityChangeDao> {

    @UmInsert
    public abstract long insert(ClazzActivityChange entity);

    @UmUpdate
    public abstract void update(ClazzActivityChange entity);

    @UmInsert
    public abstract void insertAsync(ClazzActivityChange entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM ClazzActivityChange")
    public abstract UmProvider<ClazzActivityChange> findAllClazzActivityChanges();

    @UmQuery("SELECT * FROM ClazzActivityChange")
    public abstract void findAllClazzActivityChangesAsync(UmCallback<List<ClazzActivityChange>> resultList);

    @UmQuery("SELECT * FROM ClazzActivityChange")
    public abstract UmLiveData<List<ClazzActivityChange>> findAllClazzActivityChangesAsyncLive();

    @UmUpdate
    public abstract void updateAsync(ClazzActivityChange entity, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    public abstract ClazzActivityChange findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    public abstract  void findByUidAsync(long uid, UmCallback<ClazzActivityChange> resultObject);

    @UmQuery("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeTitle = :title")
    public abstract ClazzActivityChange findByTitle(String title);
}
