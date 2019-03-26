package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.CustomField;

/**
 * Core View. Screen is for CustomDetailList's View
 */
public interface CustomFieldListView extends UstadView {

    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "CustomFieldList";

    //Any argument keys:

    void setListProvider(UmProvider<CustomField> provider);

    void setEntityTypePresets(String[] entityTypePresets);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

