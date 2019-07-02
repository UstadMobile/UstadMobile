package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.lib.db.entities.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.SelectMultipleTreeDialogView.ARG_LOCATIONS_SET;


/**
 * The SelectMultipleTreeDialog Presenter.
 */
public class SelectMultipleTreeDialogPresenter
        extends CommonEntityHandlerPresenter<SelectMultipleTreeDialogView> {

    HashMap<String, Long> selectedOptions;

    private List<Long> selectedLocationsList;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultipleTreeDialogPresenter(Object context, Hashtable arguments,
                                             SelectMultipleTreeDialogView view) {
        super(context, arguments, view);

        if (arguments.containsKey(ARG_LOCATIONS_SET)) {
            long[] locationsArray = (long[]) arguments.get(ARG_LOCATIONS_SET);
            selectedLocationsList =
                    convertLongArray(locationsArray);
        }

        selectedOptions = new HashMap<>();
        getTopLocations();

    }

    public static ArrayList<Long> convertLongArray(long[] array) {
        ArrayList<Long> result = new ArrayList<Long>(array.length);
        for (long item : array)
            result.add(item);
        return result;
    }

    public HashMap<String, Long> getSelectedOptions() {
        return selectedOptions;
    }

    public void getTopLocations() {
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


    public void handleClickPrimaryActionButton() {
        view.finish();
    }


    public List<Long> getSelectedLocationsList() {
        if (selectedLocationsList == null) {
            return new ArrayList<>();
        }
        return selectedLocationsList;
    }

    public void setSelectedLocationsList(List<Long> selectedLocationsList) {
        this.selectedLocationsList = selectedLocationsList;
    }

    @Override
    public void entityChecked(String entityName, Long entityUid, boolean checked) {
        if (checked) {
            selectedOptions.put(entityName, entityUid);
        } else {
            if (selectedOptions.containsKey(entityName)) {
                selectedOptions.remove(entityUid);
            }
        }
    }

}
