package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaClazzWithSocialInfo;

@UmDao
public abstract class WamdaClazzDao {

    @UmQuery("SELECT Clazz.* , " +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid) AS numLikes," +
            "(SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents," +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid" +
            " AND WamdaLike.wamdaLikePersonUid =:personUid) AS liked, " +
            "(SELECT  COUNT(*) FROM WamdaShare WHERE WamdaShare.wamdaShareClazzUid = Clazz.clazzUid) AS numShares " +
            "FROM Clazz")
    public abstract UmProvider<WamdaClazzWithSocialInfo> findAll(long personUid);
}
