package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class PersonGroupMemberDao implements SyncableDao<PersonGroupMember,
        PersonGroupMemberDao> {

    @UmQuery("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid")
    public abstract void findAllGroupWherePersonIsIn(long personUid,
                                                 UmCallback<List<PersonGroupMember>> resultList);

    @UmQuery("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid")
    public abstract List<PersonGroupMember> findAllGroupWherePersonIsInSync(long personUid);

    @UmQuery("SELECT * FROM PersonGroupMember WHERE groupMemberGroupUid = :groupUid " +
            " AND groupMemberActive = 1")
    public abstract UmProvider<PersonGroupMember> finAllMembersWithGroupId(long groupUid);

    @UmQuery("SELECT Person.*, (0) AS clazzUid, " +
            "  (0) AS attendancePercentage, " +
            "  (0) AS clazzMemberRole,  " +
            "  (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "  PersonPicture.personPicturePersonUid = Person.personUid ORDER BY picTimestamp " +
            "  DESC LIMIT 1) AS personPictureUid, " +
            "  (0) AS enrolled from PersonGroupMember " +
            " LEFT JOIN Person ON PersonGroupMember.groupMemberPersonUid = Person.personUid " +
            " WHERE groupMemberGroupUid = :groupUid AND groupMemberActive = 1 ")
    public abstract UmProvider<PersonWithEnrollment> findAllPersonWithEnrollmentWithGroupUid(long groupUid);

    @UmQuery("Select Person.* from PersonGroupMember " +
            " LEFT JOIN Person on PersonGroupMember.groupMemberPersonUid = Person.personUid " +
            " WHERE PersonGroupMember.groupMemberGroupUid = :groupUid")
    public abstract List<Person> findPersonByGroupUid(long groupUid);

    @UmQuery("SELECT * FROM PersonGroupMember WHERE groupMemberGroupUid = :groupUid AND " +
            " groupMemberPersonUid = :personUid ")
    public abstract void findMemberByGroupAndPersonAsync(long groupUid, long personUid,
                                                    UmCallback<PersonGroupMember> resultObject);

    @UmQuery("UPDATE PersonGroupMember SET groupMemberActive = 0 " +
            " WHERE groupMemberPersonUid = :personUid AND groupMemberGroupUid = :groupUid")
    public abstract void inactivateMemberFromGroupAsync(long personUid, long groupUid,
                                                   UmCallback<Integer> resultObject);

}
