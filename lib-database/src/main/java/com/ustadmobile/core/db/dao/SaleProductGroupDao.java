package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SaleProductGroup;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN )
@UmRepository
public abstract class SaleProductGroupDao implements SyncableDao<SaleProductGroup, SaleProductGroupDao> {

    //INSERT

    @UmInsert
    public abstract void insertAsync(SaleProductGroup entity, UmCallback<Long> insertCallback);


    //FIND ALL ACTIVE

    public static final String ALL_ACTIVE_QUERY =
            "SELECT * FROM SaleProductGroup WHERE saleProductGroupActive = 1";

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmLiveData<List<SaleProductGroup>> findAllActiveLive();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract List<SaleProductGroup> findAllActiveList();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract void findAllActiveAsync(UmCallback<List<SaleProductGroup>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmProvider<SaleProductGroup> findAllActiveProvider();

    public static final String ALL_ACTIVE_TYPED_QUERY =
            "SELECT * FROM SaleProductGroup WHERE saleProductGroupActive = 1 AND " +
                    " saleProductGroupType = :type";

    @UmQuery(ALL_ACTIVE_TYPED_QUERY)
    public abstract UmLiveData<List<SaleProductGroup>> findAllTypedActiveLive(int type);

    @UmQuery(ALL_ACTIVE_TYPED_QUERY)
    public abstract List<SaleProductGroup> findAllTypedActiveList(int type);

    @UmQuery(ALL_ACTIVE_TYPED_QUERY)
    public abstract void findAllTypedActiveAsync(int type, UmCallback<List<SaleProductGroup>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_TYPED_QUERY)
    public abstract UmProvider<SaleProductGroup> findAllTypedActiveProvider(int type);

    //LOOK UP

    public static final String FIND_BY_UID_QUERY =
            "SELECT * FROM SaleProductGroup WHERE saleProductGroupUid = :uid";

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract SaleProductGroup findByUid(long uid);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract void findByUidAsync(long uid, UmCallback<SaleProductGroup> findByUidCallback);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract UmLiveData<SaleProductGroup> findByUidLive(long uid);

    //INACTIVATE:

    public static final String INACTIVATE_QUERY =
            "UPDATE SaleProductGroup SET saleProductGroupActive = 0 WHERE saleProductGroupUid = :uid";
    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntity(long uid);

    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntityAsync(long uid, UmCallback<Integer> inactivateCallback);


    //UPDATE:

    @UmUpdate
    public abstract void updateAsync(SaleProductGroup entity, UmCallback<Integer> updateCallback);
}
