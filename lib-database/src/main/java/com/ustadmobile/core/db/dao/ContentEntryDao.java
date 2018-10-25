package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
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

    @UmQuery("Select ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract List<ContentEntry> getChildrenByParentUidTest(long parentUid);


}
