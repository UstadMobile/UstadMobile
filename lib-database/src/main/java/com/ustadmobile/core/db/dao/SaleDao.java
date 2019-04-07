package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleListDetail;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class SaleDao implements SyncableDao<Sale, SaleDao> {

    //INSERT

    @UmInsert
    public abstract void insertAsync(Sale entity, UmCallback<Long> insertCallback);


    //FIND ALL ACTIVE

    public static final String ALL_SALES_ACTIVE_QUERY = "SELECT * FROM Sale WHERE saleActive = 1";

    @UmQuery(ALL_SALES_ACTIVE_QUERY)
    public abstract UmLiveData<List<Sale>> findAllActiveLive();

    @UmQuery(ALL_SALES_ACTIVE_QUERY)
    public abstract List<Sale> findAllActiveList();

    @UmQuery(ALL_SALES_ACTIVE_QUERY)
    public abstract void findAllActiveAsync(UmCallback<List<Sale>> allActiveSalesCallback);

    @UmQuery(ALL_SALES_ACTIVE_QUERY)
    public abstract UmProvider<Sale> findAllActiveProvider();

    @UmQuery(ALL_SALES_ACTIVE_QUERY +
            " AND salePreOrder = 1 ")
    public abstract UmLiveData<List<Sale>> findAllActivePreorderSalesLive();

    @UmQuery(ALL_SALES_ACTIVE_QUERY +
            " AND salePaymentDone = 0")
    public abstract UmLiveData<List<Sale>> findAllActivePaymentDueSalesLive();

    @UmQuery("SELECT * FROM Sale WHERE saleTitle = :saleTitle AND saleActive = 1")
    public abstract void findAllSaleWithTitleAsync(String saleTitle, UmCallback<List<Sale>> resultCallback);


    /*
    String saleTitle;
    String locationName;
    long saleDueDate;
    float saleAmount;
    String saleCurrency;
    long saleCreationDate;
    int saleItemCount;
     */
    public static final String ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY =
    " SELECT s.*, Location.title AS locationName, " +
    " COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - " +
            "SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = " +
            "Sale.saleUid WHERE Sale.saleUid = s.saleUid) ,0) AS saleAmount, " +
    " 'Afs' AS saleCurrency,  " +
    " (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = s.saleUid) AS saleItemCount " +
    "FROM Sale s " +
    " LEFT JOIN Location ON Location.locationUid = s.saleLocationUid WHERE s.saleActive = 1";


    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY)
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveAsSaleListDetailLive();

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY)
    public abstract List<SaleListDetail> findAllActiveAsSaleListDetailList();

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY)
    public abstract void findAllActiveAsSaleListDetailAsync(UmCallback<List<SaleListDetail>> allActiveSalesCallback);

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY)
    public abstract UmProvider<SaleListDetail> findAllActiveAsSaleListDetailProvider();

    //Filter

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY + " AND salePreOrder = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetailPreOrdersLive();

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY + " AND salePreOrder = 1")
    public abstract UmProvider<SaleListDetail> findAllActiveSaleListDetailPreOrdersProvider();

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY + " AND salePaymentDone = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetailPaymentDueLive();

    @UmQuery(ALL_SALES_ACTIVE_AS_SALE_LIST_DETAIL_QUERY + " AND salePaymentDone = 0")
    public abstract UmProvider<SaleListDetail> findAllActiveSaleListDetailPaymentDueProvider();


    //LOOK UP

    public static final String FIND_BY_UID_QUERY = "SELECT * FROM Sale WHERE saleUid = :saleUid";

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract Sale findByUid(long saleUid);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract void findByUidAsync(long saleUid, UmCallback<Sale> findByUidCallback);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract UmLiveData<Sale> findByUidLive(long saleUid);

    //INACTIVATE:

    public static final String INACTIVATE_SALE_QUERY =
            "UPDATE Sale SET saleActive = 0 WHERE saleUid = :saleUid";
    @UmQuery(INACTIVATE_SALE_QUERY)
    public abstract void inactivateEntity(long saleUid);

    @UmQuery(INACTIVATE_SALE_QUERY)
    public abstract void inactivateEntityAsync(long saleUid, UmCallback<Integer> inactivateSaleCallback);


    //UPDATE:

    @UmUpdate
    public abstract void updateAsync(Sale entity, UmCallback<Integer> updateCallback);


    //Get overdue sale count
    @UmQuery("select count(*) from sale where Sale.saleDueDate < :today and Sale.saleDueDate > 0")
    public abstract void getOverDueSaleCountAsync(long today, UmCallback<Integer> resultCallback);

}
