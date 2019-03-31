package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.StatementEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
@UmRepository
public abstract class StatementDao implements SyncableDao<StatementEntity, StatementDao> {

    @UmQuery("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    public abstract StatementEntity findByStatementId(String id);

    @UmQuery("SELECT * From StatementEntity")
    public abstract List<StatementEntity> getAll();
}
