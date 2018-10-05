package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.WamdaPerson;
import com.ustadmobile.lib.db.entities.WamdaPersonWithTotalFollowers;

@UmDao
public abstract class WamdaPersonDao {

    @UmInsert
    public abstract void insertAsync(WamdaPerson wamdaPerson,UmCallback<Long> callback);

    @UmQuery("SELECT * FROM WamdaPerson WHERE wamdaPersonUid = :personUid")
    public abstract void findByUidAsync(long personUid,UmCallback<WamdaPerson> callback);

    @UmUpdate
    public abstract void updateAsync(WamdaPerson wamdaPerson,UmCallback<Integer> callback);

    @UmQuery("SELECT Person.*, WamdaPerson.*, " +
            "(SELECT COUNT(*) FROM WamdaFollower WHERE wamdaFollowerFollowingPersonUid = :personUid) AS totalNumFollowers, " +
            "(SELECT COUNT(*) FROM WamdaFollower WHERE wamdaFollowerFollowerPersonUid = :personUid) AS totalNumFollowing " +
            " FROM Person " +
            " LEFT JOIN WamdaPerson ON WamdaPerson.wamdaPersonPersonUid = Person.personUid")
    public abstract UmLiveData<WamdaPersonWithTotalFollowers> findWithTotalNumFollowers(long personUid);
}
