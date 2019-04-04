package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

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


    public SaleListPresenter(Object context, Hashtable arguments, SaleListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getSaleDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllActiveAsSaleListDetailProvider();
        view.setListProvider(umProvider);

    }

    public void filterAll(){
        umProvider = providerDao.findAllActiveAsSaleListDetailProvider();
        view.setListProvider(umProvider);

    }

    public void filterPreOrder(){
        umProvider = providerDao.findAllActiveSaleListDetailPreOrdersProvider();
        view.setListProvider(umProvider);

    }
    public void filterPaymentDue(){

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
