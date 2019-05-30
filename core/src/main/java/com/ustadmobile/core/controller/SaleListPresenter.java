package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.SalePaymentDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Hashtable;

import com.ustadmobile.core.view.SaleListSearchView;
import com.ustadmobile.core.view.SaleListView;
import com.ustadmobile.core.view.SaleDetailView;

import com.ustadmobile.core.db.UmProvider;

import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.lib.db.entities.SaleListDetail;

import static com.ustadmobile.core.db.dao.SaleDao.ALL_SELECTED;
import static com.ustadmobile.core.db.dao.SaleDao.PAYMENT_SELECTED;
import static com.ustadmobile.core.db.dao.SaleDao.PREORDER_SELECTED;
import static com.ustadmobile.core.db.dao.SaleDao.SORT_ORDER_AMOUNT_ASC;
import static com.ustadmobile.core.db.dao.SaleDao.SORT_ORDER_AMOUNT_DESC;
import static com.ustadmobile.core.db.dao.SaleDao.SORT_ORDER_DATE_CREATED_ASC;
import static com.ustadmobile.core.db.dao.SaleDao.SORT_ORDER_DATE_CREATED_DESC;
import static com.ustadmobile.core.db.dao.SaleDao.SORT_ORDER_NAME_ASC;
import static com.ustadmobile.core.db.dao.SaleDao.SORT_ORDER_NAME_DESC;
import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;

/**
 * Presenter for SaleList view
 **/
public class SaleListPresenter extends CommonHandlerPresenter<SaleListView> {

    private UmProvider<SaleListDetail> umProvider;
    UmAppDatabase repository;
    private SaleDao saleDao;
    private SalePaymentDao salePaymentDao;

    private Hashtable<Long, Integer> idToOrderInteger;

    private int filterSelected ;

    private int currentSortOrder = 0;

    public SaleListPresenter(Object context, Hashtable arguments, SaleListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleDao = repository.getSaleDao();
        salePaymentDao = repository.getSalePaymentDao();

    }

    public void handleChangeSortOrder(long order){
        order=order+1;

        if(idToOrderInteger.containsKey(order)){
            currentSortOrder= idToOrderInteger.get(order);
            getAndSetProvider(currentSortOrder);
        }
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private String[] arrayListToStringArray(ArrayList<String> presetAL){
        Object[] objectArr = presetAL.toArray();
        String[] strArr = new String[objectArr.length];
        for(int j = 0 ; j < objectArr.length ; j ++){
            strArr[j] = (String) objectArr[j];
        }
        return strArr;
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private void updateSortSpinnerPreset(){
        ArrayList<String> presetAL = new ArrayList<>();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        idToOrderInteger = new Hashtable<>();

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_ASC);
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_DESC);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_asc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_AMOUNT_ASC);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_desc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_AMOUNT_DESC);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_date_desc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_DATE_CREATED_DESC);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_date_asc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_DATE_CREATED_ASC);

        String[] sortPresets = arrayListToStringArray(presetAL);

        view.updateSortSpinner(sortPresets);
    }


    private void getAndSetProvider(int sortCode){

        umProvider = saleDao.filterAndSortSale(filterSelected, sortCode);
        view.setListProvider(umProvider, false, false);

    }

    public void filterAndSetProvider(String search){
        umProvider = saleDao.filterAndSortSale(filterSelected, search, currentSortOrder);
        view.setListProvider(umProvider, false, false);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider
        umProvider = saleDao.findAllActiveAsSaleListDetailProvider();
        view.setListProvider(umProvider, false, false);

        idToOrderInteger = new Hashtable<>();
        updateSortSpinnerPreset();

        observePreOrderAndPaymentCounters();

    }

    public void observePreOrderAndPaymentCounters(){
        UmLiveData<Integer> preOrderLiveData = saleDao.getPreOrderSaleCountLive();
        UmLiveData<Integer> paymentsDueLiveData = salePaymentDao.getPaymentsDueCountLive();

        preOrderLiveData.observe(SaleListPresenter.this,
                SaleListPresenter.this::handlePreOrderCountUpdate);

        paymentsDueLiveData.observe(SaleListPresenter.this,
                SaleListPresenter.this::handlePaymentDueCountUpdate);
    }

    public void handlePreOrderCountUpdate(int count){
        view.updatePreOrderCounter(count);
    }

    public void handlePaymentDueCountUpdate(int count){
        view.updatePaymentDueCounter(count);
    }

    public void filterAll(){
        filterSelected = ALL_SELECTED;
        umProvider = saleDao.findAllActiveAsSaleListDetailProvider();
        view.setListProvider(umProvider, false, false);

    }

    public void filterPreOrder(){
        filterSelected = PREORDER_SELECTED;
        umProvider = saleDao.findAllActiveSaleListDetailPreOrdersProvider();
        view.setListProvider(umProvider, false, true);

    }
    public void filterPaymentDue(){
        filterSelected = PAYMENT_SELECTED;
        umProvider = saleDao.findAllActiveSaleListDetailPaymentDueProvider();
        view.setListProvider(umProvider, true, false);
    }

    void handleClickSale(long saleUid){
        UstadMobileSystemImpl impl =UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_UID, String.valueOf(saleUid));
        impl.go(SaleDetailView.VIEW_NAME, args, context);

    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        impl.go(SaleDetailView.VIEW_NAME, args, context);
    }

    public void handleClickSearch(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        impl.go(SaleListSearchView.VIEW_NAME, args, context);
    }

    @Override
    public void handleCommonPressed(Object arg) {
        handleClickSale((Long) arg);
    }

    @Override
    public void handleSecondaryPressed(Object arg) {

    }
}
