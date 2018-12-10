package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.WamdaPerson;
import com.ustadmobile.lib.db.entities.WamdaPersonWithTotalFollowers;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class WamdaPersonDao implements SyncableDao<WamdaPerson, WamdaPersonDao> {

    @UmInsert
    public abstract void insertAsync(WamdaPerson wamdaPerson,UmCallback<Long> callback);

    @UmQuery("SELECT * FROM WamdaPerson WHERE wamdaPersonUid = :personUid")
    public abstract void findByUidAsync(long personUid,UmCallback<WamdaPerson> callback);

    @UmQuery("UPDATE WamdaPerson SET pointScore = pointScore + :score WHERE wamdaPersonUid = :personUid")
    public abstract void updateScore(long personUid, int score);

    @UmUpdate
    public abstract void updateAsync(WamdaPerson wamdaPerson,UmCallback<Integer> callback);

    @UmQuery("SELECT Person.*, WamdaPerson.*, " +
            "(SELECT COUNT(*) FROM WamdaFollower WHERE wamdaFollowerPersonUid = :personUid) AS totalNumFollowing, " +
            "(SELECT COUNT(*) FROM WamdaFollower WHERE wamdaFollowingPersonUid = :personUid) AS  totalNumFollowers" +
            " FROM Person " +
            " LEFT JOIN WamdaPerson ON WamdaPerson.wamdaPersonPersonUid = Person.personUid" +
            " WHERE Person.personUid = :personUid ")
    public abstract UmLiveData<WamdaPersonWithTotalFollowers> findWithTotalNumFollowers(long personUid);

    public static WamdaPerson makeWamdaPersonForNewUser(long newPersonUid, String profileStatus,
                                                 Object context) {
        WamdaPerson wamdaPerson = new WamdaPerson();
        wamdaPerson.setWamdaPersonPersonUid(newPersonUid);
        wamdaPerson.setProfileStatus(profileStatus);
        return wamdaPerson;
    }

}
