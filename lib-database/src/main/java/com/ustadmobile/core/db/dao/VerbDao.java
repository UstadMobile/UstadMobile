package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.VerbEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
@UmRepository
public abstract class VerbDao implements SyncableDao<VerbEntity, VerbDao> {


    @UmQuery("SELECT * FROM VerbEntity WHERE urlId = :urlId")
    public abstract VerbEntity findByUrl(String urlId);
}
