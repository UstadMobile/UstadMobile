package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN )
@UmRepository
public abstract class SaleProductDao implements SyncableDao<SaleProduct, SaleProductDao> {

    //INSERT

    @UmInsert
    public abstract void insertAsync(SaleProduct entity, UmCallback<Long> insertCallback);


    //FIND ALL ACTIVE

    public static final String ALL_ACTIVE_QUERY =
            "SELECT * FROM SaleProduct WHERE saleProductActive = 1";

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmLiveData<List<SaleProduct>> findAllActiveLive();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract List<SaleProduct> findAllActiveList();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract void findAllActiveAsync(UmCallback<List<SaleProduct>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmProvider<SaleProduct> findAllActiveProvider();

    //LOOK UP

    public static final String FIND_BY_UID_QUERY =
            "SELECT * FROM SaleProduct WHERE saleProductUid = :uid";

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract SaleProduct findByUid(long uid);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract void findByUidAsync(long uid, UmCallback<SaleProduct> findByUidCallback);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract UmLiveData<SaleProduct> findByUidLive(long uid);

    public static final String FIND_BY_NAME_QUERY =
        "SELECT * FROM SaleProduct WHERE saleProductName = :name AND saleProductActive = 1";

    @UmQuery(FIND_BY_NAME_QUERY)
    public abstract SaleProduct findByName(String name);

    @UmQuery(FIND_BY_NAME_QUERY)
    public abstract void findByNameAsync(String name, UmCallback<SaleProduct> findByUidCallback);

    @UmQuery(FIND_BY_NAME_QUERY)
    public abstract UmLiveData<SaleProduct> findByNameLive(String name);

    //INACTIVATE:

    public static final String INACTIVATE_QUERY =
            "UPDATE SaleProduct SET saleProductActive = 0 WHERE saleProductUid = :uid";
    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntity(long uid);

    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntityAsync(long uid, UmCallback<Integer> inactivateCallback);


    //UPDATE:

    @UmUpdate
    public abstract void updateAsync(SaleProduct entity, UmCallback<Integer> updateCallback);
}
