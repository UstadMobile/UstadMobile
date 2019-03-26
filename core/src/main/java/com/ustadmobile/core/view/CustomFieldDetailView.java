package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.CustomField;

/**
 * Core View. Screen is for CustomDetailDetail's View
 */
public interface CustomFieldDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "CustomDetailDetail";

    //Any argument keys:
    String ARG_CUSTOM_FIELD_UID = "CustomFieldUid";

    void setDropdownPresetsOnView(String[] dropdownPresets);

    void setCustomFieldOnView(CustomField customField);

    void showOptions(boolean show);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

