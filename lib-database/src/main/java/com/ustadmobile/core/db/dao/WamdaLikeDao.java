package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaLike;
import com.ustadmobile.lib.db.entities.WamdaLikeWithDiscussionPersonClazz;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
public abstract class WamdaLikeDao implements SyncableDao<WamdaLike, WamdaLikeDao> {

    @UmInsert
    public abstract void insertAsync(WamdaLike like, UmCallback<Long> callback);

    @UmQuery("DELETE FROM WamdaLike WHERE wamdaLikePersonUid = :personUid AND wamdaLikeClazzUid = :clazzUid")
    public abstract void unlikeClazz(long personUid, long clazzUid, UmCallback<Integer> callback);


    @UmQuery("DELETE FROM WamdaLike WHERE wamdaLikePersonUid = :personUid AND wamdaLikeDiscussionUid = :discussionUid")
    public abstract void unlikeDiscussion(long personUid, long discussionUid, UmCallback<Integer> callback);


    @UmQuery("SELECT WamdaLike.*, Person.*, Clazz.clazzName, Clazz.clazzUid, DiscussionPost.* ,\n" +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid) AS numLikes,\n" +
            "(SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents,\n" +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid AND WamdaLike.wamdaLikePersonUid = :personUid) AS clazzLiked,\n" +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeDiscussionUid = DiscussionPost.clazzClazzUid AND WamdaLike.wamdaLikePersonUid = :personUid) AS discussionLiked,\n" +
            "(SELECT  COUNT(*) FROM WamdaShare WHERE WamdaShare.wamdaShareClazzUid = Clazz.clazzUid) AS numShares\n" +
            "FROM WamdaLike \n" +
            "LEFT JOIN Person ON WamdaLike.wamdaLikePersonUid = Person.personUid \n" +
            "LEFT JOIN Clazz ON WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid \n" +
            "LEFT JOIN DiscussionPost ON WamdaLike.wamdaLikeDiscussionUid = DiscussionPost.discussionPostUid \n" +
            "WHERE WamdaLike.wamdaLikePersonUid = :personUid ORDER BY WamdaLike.timeStamp DESC")
    public abstract UmProvider<WamdaLikeWithDiscussionPersonClazz> findLikesByPerson(long personUid);


}
