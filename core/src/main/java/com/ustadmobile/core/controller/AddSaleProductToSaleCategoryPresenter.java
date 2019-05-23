package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import java.util.Hashtable;

import static com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.ARG_ADD_TO_CATEGORY_TYPE_CATEGORY;
import static com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.ARG_ADD_TO_CATEGORY_TYPE_ITEM;
import static com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_ASSIGN_TO_CATEGORY_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_CATEGORY;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_TITLE;

/**
 * Presenter for AddSaleProductToSaleCategory view
 **/
public class AddSaleProductToSaleCategoryPresenter extends UstadBaseController<AddSaleProductToSaleCategoryView> {

    private UmProvider<SaleNameWithImage> umProvider;
    UmAppDatabase repository;
    private SaleProductDao providerDao;
    private SaleProductParentJoinDao productParentJoinDao;
    private long assignToThisSaleProductCategoryUid;
    private UstadMobileSystemImpl impl;
    private boolean isCategory = false;


    public AddSaleProductToSaleCategoryPresenter(Object context, Hashtable arguments, AddSaleProductToSaleCategoryView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getSaleProductDao();
        productParentJoinDao = repository.getSaleProductParentJoinDao();

        impl = UstadMobileSystemImpl.getInstance();

        if(getArguments().containsKey(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID)){
            assignToThisSaleProductCategoryUid = Long.parseLong(
                    getArguments().get(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID).toString());
        }


    }

    public void handleAddNewItem(){
        Hashtable<String, String> args = new Hashtable<>();
        if(isCategory){
            args.put(ARG_NEW_CATEGORY, "true");
        }else {
            args.put(ARG_NEW_TITLE, "true");
        }
        args.put(ARG_ASSIGN_TO_CATEGORY_UID,
                String.valueOf(assignToThisSaleProductCategoryUid));
        impl.go(SaleProductDetailView.VIEW_NAME, args, context);
    }

    public void handleClickProduct(long productUid){
        productParentJoinDao.createJoin(productUid, assignToThisSaleProductCategoryUid, true);
        view.finish();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        String addTitle = "";
        String toolbarTitle= "";
        if(getArguments().containsKey(ARG_ADD_TO_CATEGORY_TYPE_ITEM)){
            addTitle = impl.getString(MessageID.create_new_item, context);
            toolbarTitle = impl.getString(MessageID.add_item, context);
            //Get provider
            umProvider = providerDao.findAllActiveProductsNotInCategorySNWIProvider(
                    assignToThisSaleProductCategoryUid);
            view.setListProvider(umProvider);
            isCategory = false;
        }else if(getArguments().containsKey(ARG_ADD_TO_CATEGORY_TYPE_CATEGORY)){
            addTitle = impl.getString(MessageID.create_new_subcategory, context);
            toolbarTitle = impl.getString(MessageID.add_subcategory, context);
            isCategory = true;
            //Get provider
            umProvider = providerDao.findAllActiveCategoriesNotInCategorySNWIProvider(
                    assignToThisSaleProductCategoryUid);
            view.setListProvider(umProvider);
        }
        view.setAddtitle(addTitle);
        view.setToolbarTitle(toolbarTitle);

    }


}
