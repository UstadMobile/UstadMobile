package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductGroupDao;
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SaleProductCategoryListView;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_MORE_CATEGORY;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_MORE_RECENT;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_PASS_PRODUCER_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_PASS_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_SALEPRODUCT_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_SELECT_PRODUCT;
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
    SaleProductParentJoinDao productParentJoinDao;
    UstadMobileSystemImpl impl;
    private boolean catalogMode;

    private long producerUid, saleItemUid;


    public SelectSaleProductPresenter(Object context, Hashtable arguments, SelectSaleProductView view, boolean isCatalog) {
        super(context, arguments, view);

        impl = UstadMobileSystemImpl.getInstance();

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        saleProductDao = repository.getSaleProductDao();
        saleProductGroupDao = repository.getSaleProductGroupDao();
        productParentJoinDao = repository.getSaleProductParentJoinDao();

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
        collectionProvider = productParentJoinDao.findAllCategoriesInCollection();
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
            //Need to select the product.
            if(isCategory) {
                args.put(ARG_SALEPRODUCT_UID, String.valueOf(productUid));
                args.put(ARG_PASS_PRODUCER_UID, String.valueOf(producerUid));
                args.put(ARG_PASS_SALE_ITEM_UID, String.valueOf(saleItemUid));
                args.put(ARG_SELECT_PRODUCT, "true");
                impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);
            }else {
                args.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));
                args.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
                args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
                impl.go(SaleItemDetailView.VIEW_NAME, args, context);
            }
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

    public void handleDelteSaleProduct(long productUid, boolean isCategory) {
        saleProductDao.inactivateEntityAsync(productUid, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                //Send message to view
                if(isCategory){
                    view.runOnUiThread(() -> view.showMessage(MessageID.category_deleted));

                }else{
                    view.runOnUiThread(() -> view.showMessage(MessageID.item_deleted));
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleClickRecentMore() {
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_MORE_RECENT, "true");
        if(catalogMode){
                impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);

        }else {
            //Need to select the product.

            //Pass it on
            args.put(ARG_PASS_PRODUCER_UID, String.valueOf(producerUid));
            args.put(ARG_PASS_SALE_ITEM_UID, String.valueOf(saleItemUid));
            args.put(ARG_SELECT_PRODUCT, "true");

            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);
        }
    }

    public void handleClickCategoryMore() {
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_MORE_CATEGORY, "true");
        if(catalogMode){
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);

        }else {
            //Need to select the product.

            //Pass it on
            args.put(ARG_PASS_PRODUCER_UID, String.valueOf(producerUid));
            args.put(ARG_PASS_SALE_ITEM_UID, String.valueOf(saleItemUid));
            args.put(ARG_SELECT_PRODUCT, "true");

            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);
        }
    }

    public void handleClickCollectionMore() {
        saleProductDao.findByNameAsync("Collection", new UmCallback<SaleProduct>() {
            @Override
            public void onSuccess(SaleProduct result) {
                handleClickProduct(result.getSaleProductUid(), true);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }
}
