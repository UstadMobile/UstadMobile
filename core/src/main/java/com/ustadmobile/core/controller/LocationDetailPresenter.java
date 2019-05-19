package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.LocationDetailView;
import com.ustadmobile.lib.db.entities.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.LocationDetailView.LOCATIONS_SET;
import static com.ustadmobile.core.view.LocationDetailView.LOCATION_UID;


/**
 * Presenter for LocationDetail view
 **/
public class LocationDetailPresenter extends CommonLocationHandlerPresenter<LocationDetailView> {

    Location currentLocation;
    Location updatedLocation;
    private long currentLocationUid = 0;

    UmAppDatabase repository;
    LocationDao locationDao;

    HashMap<String, Long> selectedOptions;

    private List<Long> selectedLocationsList;

    public LocationDetailPresenter(Object context, Hashtable arguments, LocationDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        locationDao = repository.getLocationDao();

        if(arguments.containsKey(LOCATIONS_SET)){
            long[] locationsArray = (long[]) arguments.get(LOCATIONS_SET);
            selectedLocationsList =
                    ReportOverallAttendancePresenter.convertLongArray(locationsArray);
        }

        if(arguments.containsKey(LOCATION_UID)){
            currentLocationUid = (long) arguments.get(LOCATION_UID);
        }

        selectedOptions = new HashMap<>();
        getTopLocations();

    }

    private void getTopLocations(){
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

        if(currentLocationUid == 0){
            currentLocation = new Location();
            currentLocation.setTitle("");
            currentLocation.setLocationActive(false);

            locationDao.insertAsync(currentLocation, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromLocation(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else{
            initFromLocation(currentLocationUid);
        }
    }

    private void initFromLocation(long locationUid){
        this.currentLocationUid = locationUid;

        UmLiveData<Location> locationUmLiveData =
                locationDao.findByUidLive(currentLocationUid);
        locationUmLiveData.observe(LocationDetailPresenter.this,
                LocationDetailPresenter.this::handleLocationChanged);

        locationDao.findByUidAsync(locationUid, new UmCallback<Location>() {
            @Override
            public void onSuccess(Location result) {
                updatedLocation = result;
                view.updateLocationOnView(updatedLocation);
            }

            @Override
            public void onFailure(Throwable exception) { exception.printStackTrace();}
        });

    }

    private void handleLocationChanged(Location changedLocation){
        if(currentLocation == null){
            currentLocation = changedLocation;
        }

        if(updatedLocation == null || !updatedLocation.equals(changedLocation)){
            updatedLocation = changedLocation;
            selectedLocationsList = new ArrayList<>();
            long parentLocationUid = updatedLocation.getParentLocationUid();
            selectedLocationsList.add(parentLocationUid);

            getTopLocations();

            view.updateLocationOnView(updatedLocation);

        }
    }

    public void handleClickDone() {
        selectedLocationsList = new ArrayList<>(selectedOptions.values());

        Long firstLocation = 0L;
        if(!selectedLocationsList.isEmpty()){
            firstLocation = selectedLocationsList.get(0);
        }
        updatedLocation.setParentLocationUid(firstLocation);
        updatedLocation.setLocationActive(true);

        locationDao.updateAsync(updatedLocation, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });
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

    @Override
    public void locationChecked(String locationName, Long locationUid, boolean checked) {
        if (checked) {
            selectedOptions.put(locationName, locationUid);
        } else {
            if (selectedOptions.containsKey(locationName)) {
                selectedOptions.remove(locationName);
            }
        }
    }

    public void updateLocationTitle(String toString) {
        updatedLocation.setTitle(toString);
    }
}

