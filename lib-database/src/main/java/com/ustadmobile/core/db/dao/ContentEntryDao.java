package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

import javax.swing.text.AbstractDocument;

@UmDao
public abstract class ContentEntryDao implements BaseDao<ContentEntry> {

    @UmQuery("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl")
    public abstract ContentEntry findBySourceUrl(String sourceUrl);

    @UmUpdate
    public abstract int updateContentEntry(ContentEntry entry);

    @UmQuery("Select ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract UmProvider<ContentEntry> getChildrenByParentUid(long parentUid);

    @UmQuery("Select COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract void getCountNumberOfChildrenByParentUUid(long parentUid, UmCallback<Integer> callback);


    @UmQuery("Select * FROM ContentEntry where contentEntryUid = :parentUid")
    public abstract void getContentByUuid(long parentUid, UmCallback<ContentEntry> callback);


    @UmQuery("Select ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
            "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid")
    public abstract void findAllLanguageRelatedEntries(long entryUuid, UmCallback<List<ContentEntry>> umCallback);

}
