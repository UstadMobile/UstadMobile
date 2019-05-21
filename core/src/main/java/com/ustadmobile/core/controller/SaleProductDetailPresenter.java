package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SaleProductDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleProduct;

import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.lib.db.entities.SaleProductSelected;

import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_CATEGORY;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_TITLE;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_SALE_PRODUCT_UID;

/**
 * Presenter for SaleProductDetail view
 **/
public class SaleProductDetailPresenter extends UstadBaseController<SaleProductDetailView> {

    private UmProvider<SaleProduct> umProvider;
    UmAppDatabase repository;
    private SaleProductDao saleProductDao;
    private UstadMobileSystemImpl impl;
    private SaleProduct currentSaleProduct;
    UmProvider<SaleProductSelected> categoriesProvider;


    public SaleProductDetailPresenter(Object context, Hashtable arguments, SaleProductDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleProductDao = repository.getSaleProductDao();

        impl = UstadMobileSystemImpl.getInstance();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update toolbar title
        String toolbarTitle = "";
        if(getArguments().containsKey(ARG_NEW_TITLE)){
            toolbarTitle = impl.getString(MessageID.create_new_item, context);
        }else if(getArguments().containsKey(ARG_NEW_CATEGORY)){
            toolbarTitle = impl.getString(MessageID.create_new_subcategory, context);
        }
        view.updateToolbarTitle(toolbarTitle);

        //Get SaleProductSelected and update the view
        if(getArguments().containsKey(ARG_SALE_PRODUCT_UID)){
            saleProductDao.findByUidAsync((Long) getArguments().get(ARG_SALE_PRODUCT_UID),
                    new UmCallback<SaleProduct>() {
                @Override
                public void onSuccess(SaleProduct result) {
                    if(result == null) {
                        currentSaleProduct = result;

                        //Update provider: TODO
                        //categoriesProvider = saleProductDao.getCategoriesSelectedForSaleProduct(currentSaleProduct.getSaleProductUid());
                        //view.setListProvider(categoriesProvider);


                    }else{
                        currentSaleProduct = new SaleProduct();
                    }
                    view.initFromSaleProduct(currentSaleProduct);

                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }

    }

    public void handleClickSave() {

        //Do something.

        view.finish();
    }
}
