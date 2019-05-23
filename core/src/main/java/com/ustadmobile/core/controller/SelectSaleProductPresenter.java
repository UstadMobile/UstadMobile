package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductGroupDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SaleProductCategoryListView;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_SALEPRODUCT_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_CATEGORY;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_TITLE;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_SALE_PRODUCT_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;
import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_COLLECTION;


/**
 * Presenter for SelectSaleProduct view
 **/
public class SelectSaleProductPresenter extends UstadBaseController<SelectSaleProductView> {

    private UmProvider<SaleNameWithImage> recentProvider;
    private UmProvider<SaleNameWithImage> categoryProvider;
    private UmProvider<SaleNameWithImage> collectionProvider;

    UmAppDatabase repository;

    SaleProductDao saleProductDao;
    SaleProductGroupDao saleProductGroupDao;
    UstadMobileSystemImpl impl;
    private boolean catalogMode;

    private long producerUid, saleItemUid;


    public SelectSaleProductPresenter(Object context, Hashtable arguments, SelectSaleProductView view, boolean isCatalog) {
        super(context, arguments, view);

        impl = UstadMobileSystemImpl.getInstance();

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        saleProductDao = repository.getSaleProductDao();
        saleProductGroupDao = repository.getSaleProductGroupDao();

        if(getArguments().containsKey(ARG_PRODUCER_UID)){
            producerUid = Long.parseLong((String)getArguments().get(ARG_PRODUCER_UID));
        }
        if(getArguments().containsKey(ARG_SALE_ITEM_UID)){
            saleItemUid = Long.parseLong((String) getArguments().get(ARG_SALE_ITEM_UID));
        }

        catalogMode = isCatalog;

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


        updateRecentProvider();
        updateCategoryProvider();
        updateCollectionProvider();

    }

    private void updateRecentProvider(){

        recentProvider = saleProductDao.findAllActiveProductsSNWIProvider();
        view.setRecentProvider(recentProvider);

    }
    private void updateCategoryProvider(){

        categoryProvider = saleProductDao.findAllActiveCategoriesSNWIProvider();
        view.setCategoryProvider(categoryProvider);

    }
    private void updateCollectionProvider(){
        collectionProvider =
                saleProductGroupDao.findAllTypedActiveSNWIProvider(PRODUCT_GROUP_TYPE_COLLECTION);
        view.setCollectionProvider(collectionProvider);
    }


    public void handleClickProduct(long productUid, boolean isCategory) {

        Hashtable<String, String> args = new Hashtable<>();
        if(catalogMode){

            if(isCategory){
                args.put(ARG_SALEPRODUCT_UID, String.valueOf(productUid));
                impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);
            }else{
                args.put(ARG_SALE_PRODUCT_UID, String.valueOf(productUid));
                impl.go(SaleProductDetailView.VIEW_NAME, args, context);
            }
        }else {
            args.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));
            args.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
            args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));

            impl.go(SaleItemDetailView.VIEW_NAME, args, context);
            view.finish();
        }
    }

    public void handleClickAddItem(){
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_NEW_TITLE, "true");
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);

    }

    public void handleClickAddSubCategory(){
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_NEW_CATEGORY, "true");
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);
    }


    public void handleEditSaleProduct(long productUid) {
        //TODO
    }

    public void handleDelteSaleProduct(long productUid) {
        //TODO
    }
}
