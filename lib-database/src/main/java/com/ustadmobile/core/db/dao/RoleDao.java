package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import static com.ustadmobile.core.db.dao.RoleDao.SELECT_ACCOUNT_IS_ADMIN;

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class RoleDao implements SyncableDao<Role, RoleDao> {

    public static final String SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)";

    @UmQuery("SELECT * FROM Role WHERE roleName=:roleName")
    public abstract void findByName(String roleName, UmCallback<Role> callback);

    @UmQuery("SELECT * FROM Role WHERE roleName = :roleName")
    public abstract Role findByNameSync(String roleName);

    @UmQuery("SELECT * FROM Role WHERE roleActive = 1")
    public abstract UmProvider<Role> findAllActiveRoles();

    @UmQuery("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    public abstract void inactiveRole(long uid);

    @UmQuery("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    public abstract void inactiveRoleAsync(long uid, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM Role WHERE roleUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Role> resultObject);

    @UmQuery("SELECT * FROM Role WHERE roleUid = :uid")
    public abstract UmLiveData<Role> findByUidLive(long uid);

    @UmUpdate
    public abstract void updateAsync(Role entitiy, UmCallback<Integer> resultObject);
}
