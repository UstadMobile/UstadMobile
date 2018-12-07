package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.lib.db.entities.Location;



/**
 * The SelectMultipleTreeDialog Presenter.
 */
public class SelectMultipleTreeDialogPresenter
        extends UstadBaseController<SelectMultipleTreeDialogView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultipleTreeDialogPresenter(Object context, Hashtable arguments,
                                             SelectMultipleTreeDialogView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}

        getTopLocations();

    }

    public void getTopLocations(){
        LocationDao locationDao = repository.getLocationDao();
        locationDao.findTopLocationsAsync(new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> result) {
                view.populateTopLocation(result, null, null);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }



    public void getLocationForParentUid(long parentUid, Object treeNode, Object parentNode){
        LocationDao locationDao = repository.getLocationDao();
        locationDao.findAllChildLocationsForUidAsync(parentUid, new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> result) {

                view.populateTopLocation(result, treeNode, parentNode);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickPrimaryActionButton() {
        //TODO: Check if nothing else required. The finish() should call the onResult method in parent activity, etc. Make sure you send the list
        view.finish();
    }

    @Override
    public void setUIStrings() {

    }

}
