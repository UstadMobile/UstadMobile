package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SelectProducerView;
import com.ustadmobile.core.view.SelectSaleProductView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import com.ustadmobile.core.db.dao.PersonDao;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;

/**
 * Presenter for SelectProducer view
 **/
public class SelectProducerPresenter extends UstadBaseController<SelectProducerView> {

    private UmProvider<Person> umProvider;
    UmAppDatabase repository;
    private PersonDao providerDao;
    private long saleItemUid = 0;


    public SelectProducerPresenter(Object context, Hashtable arguments, SelectProducerView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getPersonDao();

        if(getArguments().containsKey(ARG_SALE_ITEM_UID)){
            saleItemUid = Long.valueOf((String) getArguments().get(ARG_SALE_ITEM_UID));
        }else{
            //Create a new SaleItem? - shouldn't happen.
            //throw exception.
        }


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllPeopleProvider();
        view.setListProvider(umProvider);

    }

    public void handleClickProducer(long personUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String,String> args = new Hashtable<>();
        args.put(ARG_PRODUCER_UID, String.valueOf(personUid));
        args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        impl.go(SelectSaleProductView.VIEW_NAME, args, context);
    }

}
