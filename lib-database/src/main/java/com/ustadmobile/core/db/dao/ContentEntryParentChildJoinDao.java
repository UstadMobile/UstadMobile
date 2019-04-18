package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryParentChildJoinDao
        implements SyncableDao<ContentEntryParentChildJoin, ContentEntryParentChildJoinDao> {

    public static class ContentEntryParentChildJoinSummary {
        private boolean leaf;

        private long childContentEntryUid;

        public boolean isLeaf() {
            return leaf;
        }

        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }

        public long getChildContentEntryUid() {
            return childContentEntryUid;
        }

        public void setChildContentEntryUid(long childContentEntryUid) {
            this.childContentEntryUid = childContentEntryUid;
        }
    }

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin WHERE " +
           "cepcjChildContentEntryUid = :childEntryContentUid LIMIT 1")
    public abstract ContentEntryParentChildJoin findParentByChildUuids(long childEntryContentUid);

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin WHERE " +
            "cepcjChildContentEntryUid = :childEntryContentUid")
    public abstract List<ContentEntryParentChildJoin> findListOfParentsByChildUuid(long childEntryContentUid);

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin WHERE " +
            "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid LIMIT 1")
    public abstract ContentEntryParentChildJoin findJoinByParentChildUuids(long parentUid, long childUid);


    @UmQuery("SELECT cepcjChildContentEntryUid AS childContentEntryUid," +
            "ContentEntry.leaf AS leaf " +
            "FROM ContentEntryParentChildJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE cepcjParentContentEntryUid IN (:parentUids) ")
    public abstract List<ContentEntryParentChildJoinSummary> findChildEntriesByParents(List<Long> parentUids);


    @UmUpdate
    public abstract void update(ContentEntryParentChildJoin entity);

    @UmQuery("SELECT ContentEntryParentChildJoin.* FROM " +
            "ContentEntryParentChildJoin " +
            "LEFT JOIN ContentEntry parentEntry ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = parentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntry childEntry ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid " +
            "WHERE parentEntry.publik AND childEntry.publik")
    public abstract List<ContentEntryParentChildJoin> getPublicContentEntryParentChildJoins();

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin")
    public abstract List<ContentEntryParentChildJoin> getAll();
}
