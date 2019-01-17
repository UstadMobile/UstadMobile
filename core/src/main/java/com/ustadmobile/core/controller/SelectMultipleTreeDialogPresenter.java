package com.ustadmobile.core.controller;

import java.util.HashMap;
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

    HashMap<String, Long> selectedOptions;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultipleTreeDialogPresenter(Object context, Hashtable arguments,
                                             SelectMultipleTreeDialogView view) {
        super(context, arguments, view);

        selectedOptions = new HashMap<>();
        getTopLocations();

    }

    public HashMap<String, Long> getSelectedOptions() {
        return selectedOptions;
    }

    public void getTopLocations(){
        LocationDao locationDao = repository.getLocationDao();
        locationDao.findTopLocationsAsync(new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> result) {
                view.populateTopLocation(result);
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


    public void locationChecked(String locationName, Long locationUid, boolean checked){
        if(checked){
            selectedOptions.put(locationName, locationUid);
        }else{
            if(selectedOptions.containsKey(locationName)){
                selectedOptions.remove(locationName);
            }
        }

    }


    public void handleClickPrimaryActionButton() {
        //TODO: Check if nothing else required. The finish() should call the onResult method in parent activity, etc. Make sure you send the list
        view.finish();
    }

    @Override
    public void setUIStrings() {

    }

}
