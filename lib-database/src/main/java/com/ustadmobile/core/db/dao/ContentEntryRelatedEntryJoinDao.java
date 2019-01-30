package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

import static com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryRelatedEntryJoinDao
        implements SyncableDao<ContentEntryRelatedEntryJoin, ContentEntryRelatedEntryJoinDao> {

    @UmQuery("SELECT * from ContentEntryRelatedEntryJoin WHERE " +
            "cerejRelatedEntryUid = :contentEntryUid")
    public abstract ContentEntryRelatedEntryJoin findPrimaryByTranslation(long contentEntryUid);


    @UmQuery("SELECT ContentEntryRelatedEntryJoin.cerejContentEntryUid, ContentEntryRelatedEntryJoin.cerejRelatedEntryUid," +
            " CASE ContentEntryRelatedEntryJoin.cerejRelatedEntryUid" +
            " WHEN :contentEntryUid THEN (SELECT name FROM Language WHERE langUid = (SELECT primaryLanguageUid FROM ContentEntry WHERE contentEntryUid = ContentEntryRelatedEntryJoin.cerejContentEntryUid))" +
            " ELSE Language.name" +
            " END languageName" +
            " FROM ContentEntryRelatedEntryJoin" +
            " LEFT JOIN Language ON ContentEntryRelatedEntryJoin.cerejRelLanguageUid = Language.langUid" +
            " WHERE" +
            " (ContentEntryRelatedEntryJoin.cerejContentEntryUid = :contentEntryUid" +
            " OR ContentEntryRelatedEntryJoin.cerejContentEntryUid IN" +
            " (SELECT cerejContentEntryUid FROM ContentEntryRelatedEntryJoin WHERE cerejRelatedEntryUid = :contentEntryUid))" +
            " AND ContentEntryRelatedEntryJoin.relType = " + REL_TYPE_TRANSLATED_VERSION)
    public abstract void findAllTranslationsForContentEntry(long contentEntryUid,
                                                            UmCallback<List<ContentEntryRelatedEntryJoinWithLanguage>> callback);

    @UmUpdate
    public abstract void update(ContentEntryRelatedEntryJoin entity);

}
