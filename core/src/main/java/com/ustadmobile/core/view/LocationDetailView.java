package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.Location;

import java.util.List;

/**
 * Core View. Screen is for LocationDetail's View
 */
public interface LocationDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "LocationDetail";

    //Any argument keys:
    String LOCATION_UID = "LocationUid";
    String LOCATIONS_SET="LocationDetailLocationSet";

    void populateTopLocation(List<Location> locations);

    void updateLocationOnView(Location location);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

