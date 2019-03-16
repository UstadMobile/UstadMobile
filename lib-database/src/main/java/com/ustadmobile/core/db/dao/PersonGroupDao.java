package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.GroupWithMemberCount;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class PersonGroupDao implements SyncableDao<PersonGroup, PersonGroupDao> {

    @UmQuery("SELECT *, 0 AS memberCount FROM PersonGroup")
    public abstract UmProvider<GroupWithMemberCount> findAllGroups();

    @UmQuery("SELECT *, 0 AS memberCount FROM PersonGroup WHERE groupActive = 1")
    public abstract UmProvider<GroupWithMemberCount> findAllActiveGroups();

    @UmQuery("SELECT *, 0 AS memberCount FROM PersonGroup WHERE groupActive = 1")
    public abstract UmLiveData<List<GroupWithMemberCount>> findAllActiveGroupsLive();

    @UmQuery("SELECT * FROM PersonGroup WHERE groupActive = 1")
    public abstract UmLiveData<List<PersonGroup>> findAllActivePersonGroupsLive();

    @UmQuery("UPDATE PersonGroup SET groupActive = 0 WHERE groupUid = :uid")
    public abstract void inactivateGroupAsync(long uid, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    public abstract PersonGroup findByUid(long uid);

    @UmQuery("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<PersonGroup> resultObject);

    @UmQuery("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    public abstract UmLiveData<PersonGroup> findByUidLive(long uid);

    @UmUpdate
    public abstract void updateAsync(PersonGroup entity, UmCallback<Integer> resultObject);

}
