package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.DashboardTag;


/**
 * Core View. Screen is for DashboardEntryList's View
 */
public interface DashboardEntryListView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "DashboardEntryList";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setDashboardEntryProvider(UmProvider<DashboardEntry> listProvider);

    void setDashboardTagProvider(UmProvider<DashboardTag> listProvider);

    void loadChips(String[] tags);

    void showSetTitle(String existingTitle, long entryUid);

}

