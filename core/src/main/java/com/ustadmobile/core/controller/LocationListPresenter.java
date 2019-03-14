package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.LocationListView;
import com.ustadmobile.core.view.LocationDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Location;

import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount;

/**
 * Presenter for LocationList view
 **/
public class LocationListPresenter extends UstadBaseController<LocationListView> {

    private UmProvider<LocationWithSubLocationCount> umProvider;
    UmAppDatabase repository;
    private LocationDao providerDao;


    public LocationListPresenter(Object context, Hashtable arguments, LocationListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getLocationDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllTopLocationsWithCount();
        view.setListProvider(umProvider);

    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(LocationDetailView.VIEW_NAME, args, context);
    }


}
