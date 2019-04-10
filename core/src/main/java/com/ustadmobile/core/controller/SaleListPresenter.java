package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Hashtable;

import com.ustadmobile.core.view.SaleListView;
import com.ustadmobile.core.view.SaleDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Sale;

import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.lib.db.entities.SaleListDetail;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;

/**
 * Presenter for SaleList view
 **/
public class SaleListPresenter extends UstadBaseController<SaleListView> {

    private UmProvider<SaleListDetail> umProvider;
    UmAppDatabase repository;
    private SaleDao providerDao;

    public static final int SORT_ORDER_NAME=1;
    public static final int SORT_ORDER_AMOUNT=2;
    public static final int SORT_ORDER_DATE_CREATED_DESC=3;
    public static final int SORT_ORDER_DATE_CREATED_ASC=4;

    private Hashtable<Long, Integer> idToOrderInteger;

    public static final int ALL_SELECTED = 1;
    public static final int PREORDER_SELECTED = 2;
    public static final int PAYMENT_SELECTED = 3;

    private int filterSelected ;

    public SaleListPresenter(Object context, Hashtable arguments, SaleListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getSaleDao();


    }

    public void handleChangeSortOrder(long order){
        order=order+1;

        if(idToOrderInteger.containsKey(order)){
            int sortCode = idToOrderInteger.get(order);
            getAndSetProvider(sortCode);
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

        presetAL.add(impl.getString(MessageID.sale_list_sort_by_name, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_AMOUNT);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_date_desc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_DATE_CREATED_DESC);
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_date_asc, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_DATE_CREATED_ASC);

        String[] sortPresets = arrayListToStringArray(presetAL);

        view.updateSortSpinner(sortPresets);
    }


    public void getAndSetProvider(int sortCode){

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllActiveAsSaleListDetailProvider();
        view.setListProvider(umProvider);

        idToOrderInteger = new Hashtable<>();
        updateSortSpinnerPreset();

    }

    public void filterAll(){
        filterSelected = ALL_SELECTED;
        umProvider = providerDao.findAllActiveAsSaleListDetailProvider();
        view.setListProvider(umProvider);

    }

    public void filterPreOrder(){
        filterSelected = PREORDER_SELECTED;
        umProvider = providerDao.findAllActiveSaleListDetailPreOrdersProvider();
        view.setListProvider(umProvider);

    }
    public void filterPaymentDue(){
        filterSelected = PAYMENT_SELECTED;
        umProvider = providerDao.findAllActiveSaleListDetailPaymentDueProvider();
        view.setListProvider(umProvider);
    }

    public void handleClickSale(long saleUid){
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


}
