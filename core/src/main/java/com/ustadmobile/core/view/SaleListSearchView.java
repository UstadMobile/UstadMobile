package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleListDetail;


/**
 * Core View. Screen is for SaleListSearch's View
 */
public interface SaleListSearchView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleListSearch";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();

    int SORT_MOST_RECENT = 1;
    int SORT_LOWEST_PRICE = 2;
    int SORT_HIGHEST_PRICE = 3;


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setListProvider(UmProvider<SaleListDetail> listProvider);

    void updateLocationSpinner(String[] locations);

    void updateDateRangeText(String dateRangeText);

    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    void updateSortSpinner(String[] presets);

}

