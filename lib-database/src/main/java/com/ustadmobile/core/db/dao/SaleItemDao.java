package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class SaleItemDao implements SyncableDao<SaleItem, SaleItemDao> {

    //INSERT

    @UmInsert
    public abstract void insertAsync(SaleItem entity, UmCallback<Long> insertCallback);


    //FIND ALL ACTIVE

    public static final String ALL_ACTIVE_QUERY = "SELECT * FROM SaleItem WHERE saleItemActive = 1";

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmLiveData<List<SaleItem>> findAllActiveLive();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract List<SaleItem> findAllActiveList();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract void findAllActiveAsync(UmCallback<List<SaleItem>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmProvider<SaleItem> findAllActiveProvider();


    /**
     *     long saleItemPictureUid;
     *     String saleItemProductName;
     *     int saleItemQuantityCount;
     *     float saleItemPrice;
     *     float saleItemDiscountPerItem;
     *     boolean saleItemDelivered;
     */
    public static final String ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_QUERY =
            "SELECT SaleItem.*, SaleProductPicture.saleProductPictureUid AS saleItemPictureUid, " +
            " SaleProduct.saleProductName AS saleItemProductName " +
            "FROM SaleItem " +
            " LEFT JOIN SaleProduct ON SaleItem.saleItemProductUid = SaleProduct.saleProductUid " +
            " LEFT JOIN SaleProductPicture ON SaleProductPicture.saleProductPictureSaleProductUid = " +
            "   SaleProduct.saleProductUid " +
            "WHERE saleItemActive = 1";
    @UmQuery(ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_QUERY)
    public abstract UmLiveData<List<SaleItemListDetail>> findAllSaleItemListDetailActiveLive();

    @UmQuery(ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_QUERY)
    public abstract UmProvider<SaleItemListDetail> findAllSaleItemListDetailActiveProvider();


    //Total amount of every sale per sale uid

    public static final String TOTAL_PAID_BY_SALE_UID =
            "SELECT SUM(saleItemPricePerPiece * saleItemQuantity) FROM SaleItem " +
                    "WHERE saleItemSaleUid = :saleUid AND saleItemActive = 1 " +
                    "AND saleItemSold = 1";
    public static final String TOTAL_DISCOUNT_BY_SALE_UID =
            "SELECT SUM(saleItemDiscount * saleItemQuantity) FROM SaleItem " +
                    "WHERE saleItemSaleUid = :saleUid AND saleItemActive = 1 " +
                    "AND saleItemSold = 1";

    @UmQuery(TOTAL_PAID_BY_SALE_UID)
    public abstract long findTotalPaidInASale(long saleUid);

    @UmQuery(TOTAL_PAID_BY_SALE_UID)
    public abstract void findTotalPaidBySaleAsync(long saleUid, UmCallback<Long> resultCallback);

    @UmQuery(TOTAL_DISCOUNT_BY_SALE_UID)
    public abstract long findTotalDiscountInASale(long saleUid);

    @UmQuery(TOTAL_DISCOUNT_BY_SALE_UID)
    public abstract void findTotalDiscountBySaleAsync(long saleUid, UmCallback<Long> resultCallback);



    //LOOK UP

    public static final String FIND_BY_UID_QUERY = "SELECT * FROM SaleItem WHERE saleItemUid = :uid";

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract SaleItem findByUid(long uid);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract void findByUidAsync(long uid, UmCallback<SaleItem> findByUidCallback);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract UmLiveData<SaleItem> findByUidLive(long uid);

    //INACTIVATE:

    public static final String INACTIVATE_QUERY =
            "UPDATE SaleItem SET saleItemActive = 0 WHERE saleItemUid = :uid";
    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntity(long uid);

    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntityAsync(long uid, UmCallback<Integer> inactivateCallback);


    //UPDATE:

    @UmUpdate
    public abstract void updateAsync(SaleItem entity, UmCallback<Integer> updateCallback);

}
