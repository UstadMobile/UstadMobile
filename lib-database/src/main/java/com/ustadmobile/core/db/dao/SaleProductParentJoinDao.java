package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductParentJoin;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class SaleProductParentJoinDao implements BaseDao<SaleProductParentJoin> {

//    /**
//     * Basically find all SaleProduct with no
//     */
//    @UmQuery("")
//    public abstract void findAllSaleProductsWithNoParentAsync(UmCallback<List<SaleProduct>> resultListCallback);
//
//    @UmQuery("")
//    public abstract void findAllJoinByChildSaleProductAsync(long childSaleProductUid, UmCallback<List<SaleProduct>> resultListCallback);
}
