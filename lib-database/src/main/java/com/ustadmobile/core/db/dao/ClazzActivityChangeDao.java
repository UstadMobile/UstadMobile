package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;

import java.util.List;

@UmDao
public abstract  class ClazzActivityChangeDao implements BaseDao<ClazzActivityChange> {

    @UmInsert
    public abstract long insert(ClazzActivityChange entity);

    @UmUpdate
    public abstract void update(ClazzActivityChange entity);

    @UmInsert
    public abstract void insertAsync(ClazzActivityChange entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzActivityChange")
    public abstract UmProvider<ClazzActivityChange> findAllClazzActivityChanges();

    @UmQuery("SELECT * FROM ClazzActivityChange")
    public abstract void findAllClazzActivityChangesAsync(UmCallback<List<ClazzActivityChange>> result);

    @UmUpdate
    public abstract void updateAsync(ClazzActivityChange entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    public abstract ClazzActivityChange findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    public abstract  void findByUidAsync(long uid, UmCallback<ClazzActivityChange> result);

    @UmQuery("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeTitle = :title")
    public abstract ClazzActivityChange findByTitle(String title);
}
