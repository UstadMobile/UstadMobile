package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class FeedEntryDao implements SyncableDao<FeedEntry, FeedEntryDao> {

    public static int generateFeedEntryHash(long personUid, long clazzLogUid, int alertType) {
        int hash = Long.valueOf(clazzLogUid).hashCode();
        hash = (31 * hash) + Long.valueOf(personUid).hashCode();
        hash = (31 * hash) + alertType;
        return hash;
    }

    @UmInsert
    public abstract long insert(FeedEntry entity);

    @UmInsert
    public abstract void insertAsync(FeedEntry entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM FeedEntry WHERE feedEntryUid = :uid")
    public abstract FeedEntry findByUid(long uid) ;

    @UmQuery("SELECT * FROM FeedEntry WHERE feedEntryPersonUid = :personUid AND feedEntryDone = 0")
    public abstract UmProvider<FeedEntry> findByPersonUid(long personUid);

    @UmQuery("SELECT * FROM FeedEntry WHERE feedEntryPersonUid = :personUid AND feedEntryDone = 0")
    public abstract List<FeedEntry> findByPersonUidList(long personUid);

    @UmQuery("SELECT * FROM FeedEntry")
    public abstract List<FeedEntry> findAll();

    @UmQuery("SELECT * FROM FeedEntry WHERE link = :link AND feedEntryDone = 0 " +
            "AND feedEntryPersonUid = :personUid")
    public abstract FeedEntry findByLink(long personUid, String link);

    @UmQuery("UPDATE FeedEntry SET feedEntryDone = 1 WHERE feedEntryUid = :feedEntryUid")
    public abstract void updateDoneTrue(long feedEntryUid);

    @UmQuery("UPDATE FeedEntry SET feedEntryDone = 0 WHERE feedEntryUid = :feedEntryUid")
    public abstract void updateDoneFalse(long feedEntryUid);
}
