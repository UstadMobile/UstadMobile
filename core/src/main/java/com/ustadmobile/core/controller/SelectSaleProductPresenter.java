package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductGroupDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;
import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_CATEGORY;


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

    private long producerUid, saleItemUid;


    public SelectSaleProductPresenter(Object context, Hashtable arguments, SelectSaleProductView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        saleProductDao = repository.getSaleProductDao();
        saleProductGroupDao = repository.getSaleProductGroupDao();

        if(getArguments().containsKey(ARG_PRODUCER_UID)){
            producerUid = Long.parseLong((String)getArguments().get(ARG_PRODUCER_UID));
        }
        if(getArguments().containsKey(ARG_SALE_ITEM_UID)){
            saleItemUid = Long.parseLong((String) getArguments().get(ARG_SALE_ITEM_UID));
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


        updateRecentProvider();
        updateCategoryProvider();
        //updateCollectionProvider();

    }

    private void updateRecentProvider(){
        recentProvider = saleProductDao.findAllActiveSNWIProvider();
        view.setRecentProvider(recentProvider);

    }
    private void updateCategoryProvider(){
        categoryProvider =
                saleProductGroupDao.findAllTypedActiveSNWIProvider(PRODUCT_GROUP_TYPE_CATEGORY);
        view.setCategoryProvider(categoryProvider);

    }
    private void updateCollectionProvider(){

    }


    public void handleClickProduct(long productUid) {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));
        args.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
        args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));

        impl.go(SaleItemDetailView.VIEW_NAME, args, context);
    }


}
