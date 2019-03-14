package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.LocationDetailView;


/**
 * Presenter for LocationDetail view
 **/
public class LocationDetailPresenter extends UstadBaseController<LocationDetailView> {

    UmAppDatabase repository;


    public LocationDetailPresenter(Object context, Hashtable arguments, LocationDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }


    public void handleClickDone() {

        view.finish();
    }
}
