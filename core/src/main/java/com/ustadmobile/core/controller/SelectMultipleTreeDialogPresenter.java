package com.ustadmobile.core.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.lib.db.entities.Location;

import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATIONS_SET;


/**
 * The SelectMultipleTreeDialog Presenter.
 */
public class SelectMultipleTreeDialogPresenter
        extends UstadBaseController<SelectMultipleTreeDialogView> {

    HashMap<String, Long> selectedOptions;

    private List<Long> selectedLocationsList;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultipleTreeDialogPresenter(Object context, Hashtable arguments,
                                             SelectMultipleTreeDialogView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_LOCATIONS_SET)){
            long[] locationsArray = (long[]) arguments.get(ARG_LOCATIONS_SET);
            selectedLocationsList =
                    ReportOverallAttendancePresenter.convertLongArray(locationsArray);
        }

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
        view.finish();
    }

    @Override
    public void setUIStrings() {

    }

    public List<Long> getSelectedLocationsList() {
        if(selectedLocationsList == null){
            return new ArrayList<>();
        }
        return selectedLocationsList;
    }

    public void setSelectedLocationsList(List<Long> selectedLocationsList) {
        this.selectedLocationsList = selectedLocationsList;
    }
}
