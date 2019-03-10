package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
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
    @UmQuery("Select Person.* from PersonGroupMember " +
            " LEFT JOIN Person on PersonGroupMember.groupMemberPersonUid = Person.personUid " +
            " WHERE PersonGroupMember.groupMemberGroupUid = :groupUid")
    public abstract List<Person> findPersonByGroupUid(long groupUid);

}
