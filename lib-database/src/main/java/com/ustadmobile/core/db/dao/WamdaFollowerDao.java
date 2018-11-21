package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaFollower;
import com.ustadmobile.lib.db.entities.WamdaFollowerWithPerson;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
public abstract class WamdaFollowerDao implements SyncableDao<WamdaFollower, WamdaFollowerDao> {


    @UmInsert
    public abstract void insertAsync(WamdaFollower wamdaFollower,UmCallback<Long> callback);

    /**
     * Find everyone following a particular user
     * @param personUid
     * @return
     */
    @UmQuery("SELECT WamdaFollower.*, Person.*, WamdaPerson.*,  \n" +
            "\t(SELECT COUNT(*) FROM WamdaFollower SubFollower  \n" +
            "\tWHERE  SubFollower.wamdaFollowerPersonUid = :currentUserUid  \n" +
            "\tAND  \n" +
            "\tSubFollower.wamdaFollowingPersonUid = WamdaFollower.wamdaFollowerPersonUid)  \n" +
            "\tAS following  \n" +
            "FROM WamdaFollower \n" +
            "LEFT JOIN Person ON WamdaFollower.wamdaFollowerPersonUid = Person.personUid \n" +
            "LEFT JOIN WamdaPerson ON WamdaPerson.wamdaPersonPersonUid = Person.personUid \n" +
            "WHERE WamdaFollower.wamdaFollowingPersonUid = :personUid ORDER BY WamdaFollower.timeStamp DESC")
    public abstract UmProvider<WamdaFollowerWithPerson> findFollowersByPersonUid(long personUid, long currentUserUid);


    /**
     * Find everyone followed by a particular person
     * @param personUid
     * @return
     */
    @UmQuery("SELECT WamdaFollower.*, Person.*, " +
            "(SELECT COUNT(*) FROM WamdaFollower SubFollower " +
            " WHERE SubFollower.wamdaFollowerPersonUid = :currentUserUid " +
            " AND" +
            " SubFollower.wamdaFollowingPersonUid = WamdaFollower.wamdaFollowingPersonUid) " +
            " AS following" +
            " FROM WamdaFollower" +
            " LEFT JOIN Person ON WamdaFollower.wamdaFollowingPersonUid = Person.personUid" +
            " WHERE WamdaFollower.wamdaFollowerPersonUid = :personUid ORDER BY WamdaFollower.timeStamp DESC")
    public abstract UmProvider<WamdaFollowerWithPerson> findFollowingByPersonUid(long personUid, long currentUserUid);


    @UmQuery("DELETE FROM WamdaFollower WHERE wamdaFollowerPersonUid = :followerPersonUid " +
            "AND wamdaFollowingPersonUid = :followingPersonUid")
    public abstract void unfollowPerson(long followerPersonUid, long followingPersonUid, UmCallback<Integer> callback);


}
