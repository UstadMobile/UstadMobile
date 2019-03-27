package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
@UmRepository
public abstract class ContextXObjectStatementJoinDao implements SyncableDao<ContextXObjectStatementJoin, ContextXObjectStatementJoinDao> {

    public static final int CONTEXT_FLAG_PARENT = 0;

    public static final int CONTEXT_FLAG_CATEGORY = 1;

    public static final int CONTEXT_FLAG_GROUPING = 2;

    public static final int CONTEXT_FLAG_OTHER = 3;

    @UmQuery("SELECT * FROM ContextXObjectStatementJoin where contextStatementUid = :statementUid and contextXObjectUid = :objectUid")
    public abstract ContextXObjectStatementJoin findByStatementAndObjectUid(long statementUid, long objectUid);

}
