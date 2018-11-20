package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DiscussionPost;
import com.ustadmobile.lib.db.entities.DiscussionPostWithPoster;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class DiscussionPostDao implements BaseDao<DiscussionPost> {

    @Override
    @UmInsert
    public abstract long insert(DiscussionPost entity);

    @Override
    @UmInsert
    public abstract void insertAsync(DiscussionPost entity, UmCallback<Long> result);

    @UmQuery("UPDATE DiscussionPost SET hasAttachments = :hasAttachment WHERE discussionPostUid = :postUid")
    public abstract void updatePost(long postUid,boolean hasAttachment, UmCallback<Integer> callback);

    @Override
    @UmQuery("SELECT * From DiscussionPost WHERE discussionPostUid = :uid")
    public abstract DiscussionPost findByUid(long uid);

    @UmQuery("SELECT DiscussionPost.*,Person.*,WamdaPerson.* ,\n" +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeDiscussionUid = DiscussionPost.discussionPostUid" +
            " AND WamdaLike.wamdaLikePersonUid = :personUid) AS liked,\n" +
            "(SELECT COUNT(*) FROM WamdaFollower WHERE WamdaFollower.wamdaFollowerPersonUid = :personUid " +
            "AND WamdaFollower.wamdaFollowingPersonUid = DiscussionPost.posterPersonUid ) AS following\n" +
            "FROM DiscussionPost ,Person, WamdaPerson \n" +
            "WHERE Person.personUid = DiscussionPost.posterPersonUid \n" +
            "AND WamdaPerson.wamdaPersonPersonUid = Person.personUid \n" +
            "AND DiscussionPost.clazzClazzUid = :clazzUid ORDER BY DiscussionPost.timePosted DESC")
    public abstract UmProvider<DiscussionPostWithPoster> findByClazzUidAsProvider(long clazzUid, long personUid);

    @UmQuery("SELECT * FROM DiscussionPost " +
            " LEFT JOIN Person ON DiscussionPost.posterPersonUid = Person.personUid " +
            " WHERE clazzClazzUid = :clazzUid " +
            " ORDER BY timePosted DESC")
    public abstract List<DiscussionPost> findByClazzUidAsList(long clazzUid);

    @UmQuery("SELECT DiscussionPost.*,Person.*,WamdaPerson.*,\n" +
            "(SELECT COUNT(*) FROM WamdaLike " +
            "WHERE WamdaLike.wamdaLikeDiscussionUid = DiscussionPost.discussionPostUid " +
            "AND WamdaLike.wamdaLikePersonUid = :personUid) AS liked,\n" +
            "0 AS following\n" +
            "FROM DiscussionPost  ,Person, WamdaPerson\n" +
            "WHERE Person.personUid = DiscussionPost.posterPersonUid\n" +
            "AND WamdaPerson.wamdaPersonPersonUid = Person.personUid \n" +
            "AND Person.personUid= :personUid\n" +
            "ORDER BY DiscussionPost.timePosted DESC")
    public abstract UmProvider<DiscussionPostWithPoster> findByPersonUid(long personUid);
}
