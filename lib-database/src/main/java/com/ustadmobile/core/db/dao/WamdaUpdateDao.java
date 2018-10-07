package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaUpdate;
import com.ustadmobile.lib.db.entities.WamdaUpdateWithPerson;

@UmDao
public abstract class WamdaUpdateDao {

    @UmInsert
    public abstract void insert(WamdaUpdate update, UmCallback<Long> callback);

    @UmQuery("SELECT WamdaUpdate.*, Person.* FROM WamdaUpdate" +
            " LEFT JOIN Person ON  WamdaUpdate.wamdaUpdatePersonUid = Person.personUid" +
            " AND Person.personUid IN " +
            " (SELECT wamdaFollowerPersonUid FROM WamdaFollower WHERE wamdaFollowerPersonUid = :personUid)")
    public abstract UmProvider<WamdaUpdateWithPerson> findByPersonUid(long personUid);
}
