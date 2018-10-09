package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DiscussionPost;
import com.ustadmobile.lib.db.entities.DiscussionPostWithPoster;

import java.util.List;

@UmDao
public abstract class DiscussionPostDao implements BaseDao<DiscussionPost> {

    @Override
    @UmInsert
    public abstract long insert(DiscussionPost entity);

    @Override
    @UmInsert
    public abstract void insertAsync(DiscussionPost entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * From DiscussionPost WHERE discussionPostUid = :uid")
    public abstract DiscussionPost findByUid(long uid);

    @UmQuery("SELECT DiscussionPost.*,Person.*,\n" +
            " (SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeDiscussionUid = DiscussionPost.discussionPostUid " +
            "AND WamdaLike.wamdaLikePersonUid = :personUid) AS liked\n" +
            " FROM DiscussionPost \n" +
            " LEFT JOIN Person ON Person.personUid = DiscussionPost.posterPersonUid\n" +
            " AND DiscussionPost.clazzClazzUid = :clazzUid\n" +
            " ORDER BY DiscussionPost.timePosted DESC")
    public abstract UmProvider<DiscussionPostWithPoster> findByClazzUidAsProvider(long clazzUid, long personUid);

    @UmQuery("SELECT * FROM DiscussionPost " +
            " LEFT JOIN Person ON DiscussionPost.posterPersonUid = Person.personUid " +
            " WHERE clazzClazzUid = :clazzUid " +
            " ORDER BY timePosted DESC")
    public abstract List<DiscussionPost> findByClazzUidAsList(long clazzUid);

    @UmQuery("SELECT DiscussionPost.*,Person.*,\n" +
            " (SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeDiscussionUid = DiscussionPost.discussionPostUid " +
            "AND WamdaLike.wamdaLikePersonUid = :personUid) AS liked\n" +
            " FROM DiscussionPost \n" +
            " LEFT JOIN Person ON Person.personUid = DiscussionPost.posterPersonUid\n" +
            " AND DiscussionPost.posterPersonUid= :personUid\n" +
            " ORDER BY DiscussionPost.timePosted DESC")
    public abstract UmProvider<DiscussionPostWithPoster> findByPersonUid(long personUid);
}
