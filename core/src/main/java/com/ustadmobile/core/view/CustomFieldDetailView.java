package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;

/**
 * Core View. Screen is for CustomDetailDetail's View
 */
public interface CustomFieldDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "CustomDetailDetail";

    //Any argument keys:
    String ARG_CUSTOM_FIELD_UID = "CustomFieldUid";

    void setDropdownPresetsOnView(String[] dropdownPresets);
    void setEntityTypePresetsOnView(String[] entityTypePresets);

    void setCustomFieldOnView(CustomField customField);

    void showOptions(boolean show);

    void setListProvider(UmProvider<CustomFieldValueOption> listProvider);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

