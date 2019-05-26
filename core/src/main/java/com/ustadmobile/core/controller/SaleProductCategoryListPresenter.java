package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SaleProductCategoryListView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;

import com.ustadmobile.core.db.dao.SaleProductDao;

import static com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.ARG_ADD_TO_CATEGORY_TYPE_CATEGORY;
import static com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.ARG_ADD_TO_CATEGORY_TYPE_ITEM;
import static com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_MORE_CATEGORY;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_MORE_RECENT;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_PASS_PRODUCER_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_PASS_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_SALEPRODUCT_UID;
import static com.ustadmobile.core.view.SaleProductCategoryListView.ARG_SELECT_PRODUCT;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_ASSIGN_TO_CATEGORY_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_CATEGORY;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_TITLE;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_SALE_PRODUCT_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;

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

    private boolean selectProductMode = false;
    private String producerUid;
    private String saleItemUid;

    private boolean moreRecent;
    private boolean moreCategory;


    public SaleProductCategoryListPresenter(Object context, Hashtable arguments, SaleProductCategoryListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        productDao = repository.getSaleProductDao();
        productParentJoinDao = repository.getSaleProductParentJoinDao();

        impl = UstadMobileSystemImpl.getInstance();
        //Populate itemProvider and categoryProvider

        if(getArguments().containsKey(ARG_SELECT_PRODUCT)){
            if(getArguments().get(ARG_SELECT_PRODUCT).equals("true")){
                selectProductMode = true;
            }
        }
        if(getArguments().containsKey(ARG_PASS_PRODUCER_UID)){
            producerUid = getArguments().get(ARG_PASS_PRODUCER_UID).toString();
        }
        if(getArguments().containsKey(ARG_PASS_SALE_ITEM_UID)){
            saleItemUid = getArguments().get(ARG_PASS_SALE_ITEM_UID).toString();
        }
        if(getArguments().containsKey(ARG_MORE_RECENT)){
            moreRecent = true;
        }
        if(getArguments().containsKey(ARG_MORE_CATEGORY)){
            moreCategory = true;
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(getArguments().containsKey(ARG_SALEPRODUCT_UID)){
            productDao.findByUidAsync(Long.parseLong(getArguments()
                    .get(ARG_SALEPRODUCT_UID).toString()), new UmCallback<SaleProduct>() {
                @Override
                public void onSuccess(SaleProduct result) {
                    if(result != null){
                        currentSaleProductCategory = result;
                    }else{
                        currentSaleProductCategory = new SaleProduct("", "",
                                true, false);
                    }
                    setCategoryOnView(true, true);
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }else{
            currentSaleProductCategory = new SaleProduct("","",
                    true, false);

            setCategoryOnView(moreRecent, moreCategory);
        }

        if(selectProductMode){
            view.hideFAB(true);
        }
    }

    private void setCategoryOnView(boolean recent, boolean category){
        //Update on view
        view.initFromSaleCategory(currentSaleProductCategory);

        //Get category and item providers

        //Get SaleNameWithImage for all items
        if(currentSaleProductCategory.getSaleProductUid() != 0){
            view.hideEditMenu(false);
            itemProvider = productParentJoinDao.findAllItemsInACategory(
                    currentSaleProductCategory.getSaleProductUid());
            categoryProvider = productParentJoinDao.findAllCategoriesInACategory(
                    currentSaleProductCategory.getSaleProductUid());
        }else{
            view.hideEditMenu(true);
            itemProvider = productDao.findAllActiveSNWIProvider();
            categoryProvider = productDao.findAllActiveCategoriesSNWIProvider();
        }

        if(recent)
        view.runOnUiThread(() ->view.setListProvider(itemProvider));
        if(category)
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
            if(selectProductMode){
                //Go to the sale item
                args.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));
                args.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
                args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
                impl.go(SaleItemDetailView.VIEW_NAME, args, context);

            }else {
                //Go to product detail.
                args.put(ARG_SALE_PRODUCT_UID, String.valueOf(productUid));
                impl.go(SaleProductDetailView.VIEW_NAME, args, context);
            }

        }else{
            //Go to category detail
            if(selectProductMode) {
                //pass it on bro
                args.put(ARG_SELECT_PRODUCT, "true");
                args.put(ARG_PASS_PRODUCER_UID, producerUid);
                args.put(ARG_PASS_SALE_ITEM_UID, saleItemUid);
            }

            args.put(ARG_SALEPRODUCT_UID, String.valueOf(productUid));
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context);
        }
    }

    public void handleClickAddItem(){
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID,
                String.valueOf(currentSaleProductCategory.getSaleProductUid()));
        args.put(ARG_ADD_TO_CATEGORY_TYPE_ITEM, "true");
        impl.go(AddSaleProductToSaleCategoryView.VIEW_NAME, args, context);
    }

    public void handleClickAddSubCategory(){

        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID,
                String.valueOf(currentSaleProductCategory.getSaleProductUid()));
        args.put(ARG_ADD_TO_CATEGORY_TYPE_CATEGORY, "true");
        impl.go(AddSaleProductToSaleCategoryView.VIEW_NAME, args, context);
    }
}
