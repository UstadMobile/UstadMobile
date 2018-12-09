package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.WamdaClazz;
import com.ustadmobile.lib.db.entities.WamdaClazzWithSocialInfo;
import com.ustadmobile.lib.db.entities.WamdaClazzWithSocialInfoClazzMember;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class WamdaClazzDao implements SyncableDao<WamdaClazz, WamdaClazzDao> {

    @UmQuery("SELECT Clazz.*,WamdaClazz.*, (SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid) AS numLikes,(\n" +
            "SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents,\n" +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid AND WamdaLike.wamdaLikePersonUid = :personUid) AS liked, \n" +
            "(SELECT  COUNT(*) FROM WamdaShare WHERE WamdaShare.wamdaShareClazzUid = Clazz.clazzUid) AS numShares \n" +
            "FROM Clazz \n" +
            "LEFT JOIN WamdaClazz ON WamdaClazz.wamdaClazzUid = Clazz.clazzUid ORDER BY Clazz.clazzUid")
    public abstract UmProvider<WamdaClazzWithSocialInfo> findAll(long personUid);

    @UmQuery("SELECT Clazz.* , WamdaClazz.*, Person.*, \n" +
            "(SELECT COUNT(*) FROM WamdaLike WHERE WamdaLike.wamdaLikeClazzUid = Clazz.clazzUid) AS numLikes,\n" +
            "(SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents,\n" +
            "(SELECT  COUNT(*) FROM WamdaShare WHERE WamdaShare.wamdaShareClazzUid = Clazz.clazzUid) AS numShares \n" +
            "FROM Clazz \n" +
            "LEFT JOIN WamdaClazz ON Clazz.clazzUid = WamdaClazz.wamdaClazzClazzUid \n" +
            "LEFT JOIN Person ON Person.personUid = WamdaClazz.wamdaClazzPersonUid\n" +
            "WHERE Clazz.clazzUid = :wamdaClazzUid ORDER BY Clazz.clazzUid")
    public abstract UmLiveData<WamdaClazzWithSocialInfoClazzMember> findByClazzUid(long wamdaClazzUid);

    @UmInsert
    public abstract void insertSync(WamdaClazz wamdaClazz, UmCallback<Long> callback);
}
