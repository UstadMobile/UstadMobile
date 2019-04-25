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

    public static final int ALL_SELECTED = 1;
    public static final int PREORDER_SELECTED = 2;
    public static final int PAYMENT_SELECTED = 3;

    public static final int SORT_ORDER_NAME_ASC =1;
    public static final int SORT_ORDER_NAME_DESC =2;
    public static final int SORT_ORDER_AMOUNT_ASC=3;
    public static final int SORT_ORDER_AMOUNT_DESC=4;
    public static final int SORT_ORDER_DATE_CREATED_DESC=5;
    public static final int SORT_ORDER_DATE_CREATED_ASC=6;


    //INSERT

    @UmInsert
    public abstract void insertAsync(Sale entity, UmCallback<Long> insertCallback);


    //FIND ALL ACTIVE

    public static final String ALL_SALES_QUERY = "SELECT * FROM Sale";

    @UmQuery(ALL_SALES_QUERY)
    public abstract List<Sale> findAllList();

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

    @UmQuery("SELECT * FROM Sale WHERE saleTitle = :saleTitle AND saleActive = 1")
    public abstract List<Sale> findAllSaleWithTitle(String saleTitle);


    /*
    String saleTitle;
    String locationName;
    long saleDueDate;
    float saleAmount;
    String saleCurrency;
    long saleCreationDate;
    int saleItemCount;
     */
//    public static final String ALL_SALE_LIST =
//    " SELECT s.*, Location.title AS locationName, " +
//    " COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - " +
//            "SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = " +
//            "Sale.saleUid WHERE Sale.saleUid = s.saleUid) ,0) AS saleAmount, " +
//    " 'Afs' AS saleCurrency,  " +
//    " (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = s.saleUid) AS saleItemCount " +
//    "FROM Sale s " +
//    " LEFT JOIN Location ON Location.locationUid = s.saleLocationUid WHERE s.saleActive = 1";
//
    public static final String ALL_SALE_LIST =
            " SELECT sl.*, " +
            " (SELECT SaleItem.saleItemQuantity " +
            " FROM Sale stg " +
            " LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid " +
            " WHERE stg.saleUid = sl.saleUid " +
            " ORDER BY stg.saleCreationDate ASC LIMIT 1 " +
            " )  " +
            " || 'x ' || " +
            " (SELECT SaleProduct.saleProductName " +
            " FROM SaleItem sitg " +
            " LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = sitg.saleItemProductUid " +
            " WHERE sitg.saleItemSaleUid = sl.saleUid " +
            " ORDER BY sitg.saleItemCreationDate ASC LIMIT 1) " +
            " || " +
            " (select " +
            "  (case  " +
            "   when  " +
            "   (SELECT count(*) from SaleItem sid where sid.saleItemSaleUid = sl.saleUid) > 1 " +
            "   then '...'  " +
            "   else '' " +
            "  end) " +
            " from sale) " +
            " AS saleTitleGen, " +
            " Location.title AS locationName, " +
            " COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - " +
            "            SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = " +
            "            Sale.saleUid WHERE Sale.saleUid = sl.saleUid) ,0 " +
            " ) AS saleAmount, " +
            " (COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - " +
            "            SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = " +
            "            Sale.saleUid WHERE Sale.saleUid = sl.saleUid) ,0 " +
            " ) - COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment " +
            " WHERE SalePayment.salePaymentSaleUid = sl.saleUid " +
            " AND SalePayment.salePaymentDone = 1 AND SalePayment.salePaymentActive = 1) ,0)) AS saleAmountDue, " +
            " 'Afs' AS saleCurrency,  " +
            " coalesce(" +
            "    ( " +
            "    SELECT SaleItem.saleItemDueDate FROM SaleItem LEFT JOIN Sale on Sale.saleUid = " +
            "       SaleItem.saleItemSaleUid WHERE SaleItem.saleItemSaleUid = sl.saleUid  " +
            "      AND Sale.salePreOrder = 1 AND Sale.saleActive = 1 AND SaleItem.saleItemPreOrder = 1 " +
            "      AND SaleItem.saleItemSold = 0 ORDER BY SaleItem.saleItemDueDate ASC LIMIT 1 " +
            "    ) ,0) AS earliestDueDate, "  +
            " (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = sl.saleUid) AS saleItemCount," +
            " COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment  " +
            "  WHERE SalePayment.salePaymentSaleUid = sl.saleUid " +
            "  AND SalePayment.salePaymentDone = 1 AND SalePayment.salePaymentActive = 1) ,0) " +
            "  AS saleAmountPaid " +
            " FROM Sale sl " +
            " LEFT JOIN Location ON Location.locationUid = sl.saleLocationUid WHERE sl.saleActive = 1  ";



    @UmQuery(ALL_SALE_LIST)
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveAsSaleListDetailLive();

    @UmQuery(ALL_SALE_LIST)
    public abstract List<SaleListDetail> findAllActiveAsSaleListDetailList();

    @UmQuery(ALL_SALE_LIST)
    public abstract void findAllActiveAsSaleListDetailAsync(UmCallback<List<SaleListDetail>> allActiveSalesCallback);

    @UmQuery(ALL_SALE_LIST + " AND salePreOrder = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetailPreOrdersLive();

    @UmQuery(ALL_SALE_LIST + " AND salePaymentDone = 1")
    public abstract UmLiveData<List<SaleListDetail>> findAllActiveSaleListDetailPaymentDueLive();

    //filter and sort

    public static final String FILTER_PREORDER = " AND salePreOrder = 1";
    public static final String FILTER_PAYMENT_DUE = " AND saleAmountPaid < saleAmount ";

    @UmQuery(ALL_SALE_LIST)
    public abstract UmProvider<SaleListDetail> findAllActiveAsSaleListDetailProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER)
    public abstract UmProvider<SaleListDetail> findAllActiveSaleListDetailPreOrdersProvider();


    //Payments due shows the payment amount pending vs the total amount of the sale.
    @UmQuery(ALL_SALE_LIST + FILTER_PAYMENT_DUE)
    public abstract UmProvider<SaleListDetail> findAllActiveSaleListDetailPaymentDueProvider();




    public static final String SORT_NAME_ASC = " ORDER BY sl.saleTitle ASC ";
    public static final String SORT_NAME_DEC = " ORDER BY sl.saleTitle DESC ";
    public static final String SORT_TOTAL_AMOUNT_DESC = " ORDER BY saleAmount DESC ";
    public static final String SORT_TOTAL_AMOUNT_ASC = " ORDER BY saleAmount ASC ";
    public static final String SORT_ORDER_DATE_DESC = " ORDER BY sl.saleCreationDate DESC ";
    public static final String SORT_ORDER_DATE_ASC = " ORDER BY sl.saleCreationDate ASC ";

    @UmQuery(ALL_SALE_LIST +  SORT_NAME_ASC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterAllSortNameAscProvider();

    @UmQuery(ALL_SALE_LIST + SORT_NAME_DEC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterAllSortNameDescProvider();

    @UmQuery(ALL_SALE_LIST + SORT_TOTAL_AMOUNT_DESC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterAllSortTotalAscProvider();

    @UmQuery(ALL_SALE_LIST + SORT_TOTAL_AMOUNT_ASC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterAllSortTotalDescProvider();

    @UmQuery(ALL_SALE_LIST + SORT_ORDER_DATE_DESC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterAllSortDateAscProvider();

    @UmQuery(ALL_SALE_LIST + SORT_ORDER_DATE_ASC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterAllSortDateDescProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER + SORT_NAME_ASC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterPreOrderSortNameAscProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER + SORT_NAME_DEC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterPreOrderSortNameDescProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER + SORT_TOTAL_AMOUNT_DESC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterPreOrderSortTotalAscProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER + SORT_TOTAL_AMOUNT_ASC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterPreOrderSortTotalDescProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER + SORT_ORDER_DATE_DESC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterPreOrderSortDateAscProvider();

    @UmQuery(ALL_SALE_LIST + FILTER_PREORDER + SORT_ORDER_DATE_ASC)
    public abstract UmProvider<SaleListDetail> findAllSaleFilterPreOrderSortDateDescProvider();


    public UmProvider<SaleListDetail> filterAndSortSale(int filter, int sort){

        switch (filter){
            case ALL_SELECTED:
                switch (sort){
                    case SORT_ORDER_NAME_ASC:
                        return findAllSaleFilterAllSortNameAscProvider();
                    case SORT_ORDER_NAME_DESC:
                        return findAllSaleFilterAllSortNameDescProvider();
                    case SORT_ORDER_AMOUNT_ASC:
                        return findAllSaleFilterAllSortTotalAscProvider();
                    case SORT_ORDER_AMOUNT_DESC:
                        return findAllSaleFilterAllSortTotalDescProvider();
                    case SORT_ORDER_DATE_CREATED_DESC:
                        return findAllSaleFilterAllSortDateAscProvider();
                    case SORT_ORDER_DATE_CREATED_ASC:
                        return findAllSaleFilterAllSortDateDescProvider();
                }
                break;
            case PREORDER_SELECTED:
                switch (sort){
                    case SORT_ORDER_NAME_ASC:
                        return findAllSaleFilterPreOrderSortNameAscProvider();
                    case SORT_ORDER_NAME_DESC:
                        return findAllSaleFilterPreOrderSortNameDescProvider();
                    case SORT_ORDER_AMOUNT_ASC:
                        return findAllSaleFilterPreOrderSortTotalAscProvider();
                    case SORT_ORDER_AMOUNT_DESC:
                        return findAllSaleFilterPreOrderSortTotalDescProvider();
                    case SORT_ORDER_DATE_CREATED_DESC:
                        return findAllSaleFilterPreOrderSortDateAscProvider();
                    case SORT_ORDER_DATE_CREATED_ASC:
                        return findAllSaleFilterPreOrderSortDateDescProvider();
                }
                break;
            case PAYMENT_SELECTED:
                break;
        }
        return findAllActiveAsSaleListDetailProvider();
    }

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
    @UmQuery("select count(*) from sale where Sale.saleDueDate < :today and Sale.saleDueDate > 0 AND Sale.saleActive = 1")
    public abstract void getOverDueSaleCountAsync(long today, UmCallback<Integer> resultCallback);

    @UmQuery("select count(*) from sale where salePreOrder = 1 AND saleActive = 1")
    public abstract UmProvider<Integer> getPreOrderSaleCountProvider();

    @UmQuery("select count(*) from sale where salePreOrder = 1 AND saleActive = 1")
    public abstract UmLiveData<Integer> getPreOrderSaleCountLive();
}
