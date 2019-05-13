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

    //@UmQuery("")
    //public abstract void findAllSaleProductsWithNoParentAsync(UmCallback<List<SaleProduct>>
    // resultListCallback);

    /**
     * Find all SaleProduct a given SaleProduct is a child of.
     * @param childSaleProductUid   The child sale product uid
     * @param resultListCallback    Return callback of list of SaleProduct
     */
    @UmQuery("SELECT SaleProduct.* FROM SaleProductParentJoin LEFT JOIN SaleProduct ON " +
            " SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
            " WHERE SaleProductParentJoin.saleProductParentJoinChildUid = :childSaleProductUid")
    public abstract void findAllJoinByChildSaleProductAsync(long childSaleProductUid,
                                                UmCallback<List<SaleProduct>> resultListCallback);

}
