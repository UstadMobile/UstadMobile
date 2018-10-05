package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaFollowerWithPerson;

@UmDao
public abstract class WamdaFollowerDao {


    /**
     * Find everyone following a particular user
     * @param personUid
     * @return
     */
    @UmQuery("SELECT * FROM WamdaFollower" +
            " LEFT JOIN Person ON WamdaFollower.wamdaFollowerFollowingPersonUid = Person.personUid " +
            "WHERE WamdaFollower.wamdaFollowerFollowerPersonUid = :personUid")
    public abstract UmProvider<WamdaFollowerWithPerson> findFollowersByPersonUid(long personUid);


    @UmQuery("SELECT count(*) FROM WamdaFollower,Person WHERE " +
            "WamdaFollower.wamdaFollowerFollowingPersonUid = Person.personUid " +
            "AND WamdaFollower.wamdaFollowerFollowerPersonUid = :personUid")
    public abstract void findTotalFollowersByPersonUid(long personUid, UmCallback<Integer> callback);


    /**
     * Find everyone followed by a particular person
     * @param personUid
     * @return
     */
    @UmQuery("SELECT * FROM WamdaFollower" +
            " LEFT JOIN Person ON WamdaFollower.wamdaFollowerFollowerPersonUid = Person.personUid " +
            "WHERE WamdaFollower.wamdaFollowerFollowingPersonUid = :personUid")
    public abstract UmProvider<WamdaFollowerWithPerson> findFollowingByPersonUid(long personUid);


    @UmQuery("SELECT count(*) FROM WamdaFollower,Person WHERE " +
            "WamdaFollower.wamdaFollowerFollowerPersonUid = Person.personUid " +
            "AND WamdaFollower.wamdaFollowerFollowerPersonUid = :personUid")
    public abstract void findTotalFollowingByPersonUid(long personUid, UmCallback<Integer> callback);


    //update query
    String sql = "SELECT WamdaUpdate.* FROM WamdaUpdate WHERE wamdaUpdatePersonUid IN " +
            " (SELECT wamdaFollowerFollowerPersonUid FROM WamdaFollower WHERE wamdaFollowerFollowerPersonUid = :userUid )";


}
