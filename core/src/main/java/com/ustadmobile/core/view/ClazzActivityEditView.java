package com.ustadmobile.core.view;


/**
 * ClazzActivityEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzActivityEditView extends UstadView {

    //View name
    String VIEW_NAME = "ClazzActivityEdit";

    //Arguments
    String ARG_CLAZZACTIVITY_UID = "clazzActivityUid";

    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Update the toolbar's title with the given text.
     *
     * @param title     The string title
     */
    void updateToolbarTitle(String title);

    /**
     * Set all activity change presets to the view to be selected.
     *
     * @param presets   The string array in order to be populated in the Activity change drop down /
     *                  spinner
     */
    void setClazzActivityChangesDropdownPresets(String[] presets);

    /**
     * Sets the view type for the unit of measurement chosen.
     *
     * @param uomType   The type as per ClazzActivityChange
     */
    void setUnitOfMeasureType(long uomType);
}
