package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaUpdate;
import com.ustadmobile.lib.db.entities.WamdaUpdateWithPerson;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
public abstract class WamdaUpdateDao implements SyncableDao<WamdaUpdate, WamdaUpdateDao> {

    @UmInsert
    public abstract void insert(WamdaUpdate update, UmCallback<Long> callback);

    @UmQuery("SELECT WamdaUpdate.*, Person.* \n" +
            " FROM WamdaUpdate \n" +
            " LEFT JOIN Person on WamdaUpdate.wamdaUpdatePersonUid = Person.personUid\n" +
            " WHERE WamdaUpdate.wamdaUpdatePersonUid IN (SELECT WamdaFollower.wamdaFollowingPersonUid" +
            " FROM WamdaFollower WHERE wamdaFollowerPersonUid = :personUid)")
    public abstract UmProvider<WamdaUpdateWithPerson> findByPersonUid(long personUid);
}
