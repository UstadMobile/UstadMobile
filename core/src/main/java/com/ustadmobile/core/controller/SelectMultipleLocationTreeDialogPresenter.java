package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView;
import com.ustadmobile.lib.db.entities.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.ARG_LOCATIONS_SET;


/**
 * The SelectMultipleTreeDialog Presenter.
 */
public class SelectMultipleLocationTreeDialogPresenter
        extends CommonEntityHandlerPresenter<SelectMultipleLocationTreeDialogView> {

    private HashMap<String, Long> selectedOptions;

    private List<Long> selectedLocationsList;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultipleLocationTreeDialogPresenter(Object context, Hashtable arguments,
                                                     SelectMultipleLocationTreeDialogView view) {
        super(context, arguments, view);

        if (arguments.containsKey(ARG_LOCATIONS_SET)) {
            long[] locationsArray = (long[]) arguments.get(ARG_LOCATIONS_SET);
            selectedLocationsList = convertLongArrayToList(locationsArray);
        }
        selectedOptions = new HashMap<>();

        //Get top locations - and populate the view with it.
        getTopLocations();

    }

    /**
     * Getter for selected Locations
     * @return  selected options (locations) as a HashMap<Location name, Location Uid>
     */
    public HashMap<String, Long> getSelectedOptions() {
        return selectedOptions;
    }


    /**
     * Util method to convert an array of long to a List of Long
     * @param array long array
     * @return  List of Long
     */
    private static ArrayList<Long> convertLongArrayToList(long[] array) {
        ArrayList<Long> result = new ArrayList<Long>(array.length);
        for (long item : array)
            result.add(item);
        return result;
    }


    /**
     * Gets top locations and load initial data to the recycler view
     */
    private void getTopLocations() {
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

    public List<Long> getSelectedLocationsList() {
        if (selectedLocationsList == null) {
            return new ArrayList<>();
        }
        return selectedLocationsList;
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
