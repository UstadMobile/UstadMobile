package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
public abstract class EntityRoleDao implements SyncableDao<EntityRole, EntityRoleDao> {

    public abstract boolean userHasTableLevelPermission(long accountPersonUid, int tableId, long permission,
                                              UmCallback<Boolean> callback);

}
