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

    /*
    String saleTitle;
    String locationName;
    long saleDueDate;
    float saleAmount;
    String saleCurrency;
    long saleCreationDate;
    int saleItemCount;
     */

    @UmQuery("SELECT Sale.saleTitle AS saleTitle, Location.title AS locationName, " +
            " Sale.saleDueDate AS saleDueDate, 0 AS saleAmount, '' AS saleCurrency," +
            " Sale.saleCreationDate AS saleCreationDate, 0 AS saleItemCount," +
            " Sale.salePreOrder AS preOrder, Sale.salePreOrder AS paymentDue " +
            "FROM Sale " +
            " LEFT JOIN Location ON Location.locationUid = Sale.saleLocationUid " +
            "WHERE saleActive = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetail();

    @UmQuery("SELECT Sale.saleTitle AS saleTitle, Location.title AS locationName, " +
            " Sale.saleDueDate AS saleDueDate, 0 AS saleAmount, '' AS saleCurrency," +
            " Sale.saleCreationDate AS saleCreationDate, 0 AS saleItemCount," +
            " Sale.salePreOrder AS preOrder, Sale.salePreOrder AS paymentDue " +
            "FROM Sale " +
            "LEFT JOIN Location ON Location.locationUid = Sale.saleLocationUid " +
            "WHERE saleActive = 1 AND preOrder = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetailPreOrders();

    @UmQuery("SELECT Sale.saleTitle AS saleTitle, Location.title AS locationName, " +
            " Sale.saleDueDate AS saleDueDate, 0 AS saleAmount, '' AS saleCurrency," +
            " Sale.saleCreationDate AS saleCreationDate, 0 AS saleItemCount," +
            " Sale.salePreOrder AS preOrder, Sale.salePreOrder AS paymentDue " +
            "FROM Sale " +
            " LEFT JOIN Location ON Location.locationUid = Sale.saleLocationUid " +
            " WHERE saleActive = 1 AND paymentDue = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetailPaymentDue();

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



}
