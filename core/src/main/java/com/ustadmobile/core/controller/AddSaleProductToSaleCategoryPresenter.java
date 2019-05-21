package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleProduct;

import com.ustadmobile.core.db.dao.SaleProductDao;

/**
 * Presenter for AddSaleProductToSaleCategory view
 **/
public class AddSaleProductToSaleCategoryPresenter extends UstadBaseController<AddSaleProductToSaleCategoryView> {

    private UmProvider<SaleProduct> umProvider;
    UmAppDatabase repository;
    private SaleProductDao providerDao;


    public AddSaleProductToSaleCategoryPresenter(Object context, Hashtable arguments, AddSaleProductToSaleCategoryView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getSaleProductDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider

    }


}
