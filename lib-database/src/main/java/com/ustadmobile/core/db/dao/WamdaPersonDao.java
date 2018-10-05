package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaFollowerWithPerson;

import java.util.List;

@UmDao
public abstract class WamdaFollowerDao {


    /**
     * Find everyone following a particular user
     * @param followingUid
     * @return
     */
    @UmQuery("SELECT WamdaFollower.* FROM WamdaFollower" +
            " LEFT JOIN Person ON WamdaFollower.wamdaFollowerFollowingUid = Person.personUid " +
            "WHERE WamdaFollower.wamdaFollowerFollowerPersonUid = :followingUid")
    public abstract List<WamdaFollowerWithPerson> findByFollowingPersonUid(long followingUid);


    String sql = "SELECT WamdaUpdate.* FROM WamdaUpdate WHERE wamdaUpdatePersonUid IN " +
            " (SELECT wamdaFollowerFollowingUid FROM WamdaFollower WHERE wamdaFollowerFollowerPersonUid = :userUid )";


}
