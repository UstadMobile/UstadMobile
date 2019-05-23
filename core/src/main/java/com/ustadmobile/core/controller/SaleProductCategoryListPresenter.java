package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView;
import com.ustadmobile.core.view.SaleProductCategoryListView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;

import com.ustadmobile.core.db.dao.SaleProductDao;

import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_SALEPRODUCT_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_ASSIGN_TO_CATEGORY_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_CATEGORY;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_TITLE;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_SALE_PRODUCT_UID;

/**
 * Presenter for SaleProductCategoryList view
 **/
public class SaleProductCategoryListPresenter extends UstadBaseController<SaleProductCategoryListView> {

    private UmProvider<SaleNameWithImage> itemProvider;
    private UmProvider<SaleNameWithImage> categoryProvider;
    UmAppDatabase repository;
    private SaleProductDao productDao;
    private SaleProduct currentSaleProductCategory;
    private SaleProductParentJoinDao productParentJoinDao;
    private UstadMobileSystemImpl impl;


    public SaleProductCategoryListPresenter(Object context, Hashtable arguments, SaleProductCategoryListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        productDao = repository.getSaleProductDao();
        productParentJoinDao = repository.getSaleProductParentJoinDao();

        impl = UstadMobileSystemImpl.getInstance();
        //Populate itemProvider and categoryProvider

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(getArguments().containsKey(ARG_SALEPRODUCT_UID)){
            productDao.findByUidAsync(Long.parseLong(getArguments().get(ARG_SALEPRODUCT_UID).toString()), new UmCallback<SaleProduct>() {
                @Override
                public void onSuccess(SaleProduct result) {
                    if(result != null){
                        currentSaleProductCategory = result;
                    }else{
                        currentSaleProductCategory = new SaleProduct("", "", true, false);
                    }
                    setCategoryOnView();
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }else{
            currentSaleProductCategory = new SaleProduct("","",true, false);
            setCategoryOnView();
        }

    }

    private void setCategoryOnView(){
        //Update on view
        view.initFromSaleCategory(currentSaleProductCategory);

        //Get category and item providers

        //Get SaleNameWithImage for all items
        itemProvider = productParentJoinDao.findAllItemsInACategory(currentSaleProductCategory.getSaleProductUid());
        view.runOnUiThread(() ->view.setListProvider(itemProvider));

        //Get SaleNameWithImage for all categories
        categoryProvider = productParentJoinDao.findAllCategoriesInACategory(currentSaleProductCategory.getSaleProductUid());
        view.runOnUiThread(() -> view.setCategoriesListProvider(categoryProvider));

    }

    //If you want to edit the category itself.
    public void handleClickEditThisCategory(){
        long categoryUid = currentSaleProductCategory.getSaleProductUid();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_PRODUCT_UID, String.valueOf(categoryUid));
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);
    }

    public void handleClickEditCategory(long saleProductUid){
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_PRODUCT_UID, String.valueOf(saleProductUid));
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);
    }

    public void handleDeleteCategory(long saleProductUid){
        //TODO: this
    }

    /**
     * Go to item/category detail page.
     * @param productUid
     */
    public void handleClickProduct(long productUid, boolean isCategory) {
        Hashtable<String, String> args = new Hashtable<>();
        if(!isCategory){
            //Go to product detail.
            args.put(ARG_SALE_PRODUCT_UID, String.valueOf(productUid));
            impl.go(SaleProductDetailView.VIEW_NAME, args, context);
        }else{
            //Go to category detail
            args.put(ARG_SALEPRODUCT_UID, String.valueOf(productUid));
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);
        }
    }

    public void handleClickAddItem(){
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_NEW_TITLE, "true");
        args.put(ARG_ASSIGN_TO_CATEGORY_UID,
                String.valueOf(currentSaleProductCategory.getSaleProductUid()));
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);

        impl.go(AddSaleProductToSaleCategoryView.VIEW_NAME, args, context);
    }

    public void handleClickAddSubCategory(){
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_NEW_CATEGORY, "true");
        args.put(ARG_ASSIGN_TO_CATEGORY_UID,
                String.valueOf(currentSaleProductCategory.getSaleProductUid()));
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);
    }
}
