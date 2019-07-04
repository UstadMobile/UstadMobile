package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.Location;

import java.util.List;

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SelectMultipleLocationTreeDialogView extends UstadView {

    String VIEW_NAME = "SelectMultipleTreeDialog";

    //This argument is used to store the selection uids so that when this fragment is opened,
    // we can pre-tick the locations already selected.
    String ARG_LOCATIONS_SET = "LocationsSelected";

    /**
     * Populate the Top level location list to the view. Called when we start the tree fragment.
     * @param locations The list of top locations
     */
    void populateTopLocation(List<Location> locations);

    /**
     * Sets the title of the fragment
     * @param title
     */
    void setTitle(String title);

    /**
     * Sends back selection to parent activity and closes the fragment
     */
    void finish();

}
