package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaLike;

@UmDao
public abstract class WamdaLikeDao {

    @UmInsert
    public abstract void insertAsync(WamdaLike like, UmCallback<Long> callback);

    @UmQuery("DELETE FROM WamdaLike WHERE wamdaLikePersonUid = :personUid AND wamdaLikeClazzUid = :clazzUid")
    public abstract void unlikeClazz(long personUid, long clazzUid, UmCallback<Integer> callback);


    @UmQuery("DELETE FROM WamdaLike WHERE wamdaLikePersonUid = :personUid AND wamdaLikeDiscussionUid = :discussionUid")
    public abstract void unlikeDiscussion(long personUid, long discussionUid, UmCallback<Integer> callback);



}
