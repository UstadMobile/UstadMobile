package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SaleProductCategoryListView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;

import com.ustadmobile.core.db.dao.SaleProductDao;

/**
 * Presenter for SaleProductCategoryList view
 **/
public class SaleProductCategoryListPresenter extends UstadBaseController<SaleProductCategoryListView> {

    private UmProvider<SaleNameWithImage> itemProvider;
    private UmProvider<SaleNameWithImage> categoryProvider;
    UmAppDatabase repository;
    private SaleProductDao providerDao;


    public SaleProductCategoryListPresenter(Object context, Hashtable arguments, SaleProductCategoryListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getSaleProductDao();

        //Populate itemProvider and categoryProvider

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get SaleNameWithImage for all items
        //setListProvider

        //Get SaleNameWithImage for all categories
        //setCategoriesListProvider

    }


    public void handleClickProduct(long productUid) {
    }
}
