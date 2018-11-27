package com.ustadmobile.core.view;


/**
 * AddActivityChangeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface AddActivityChangeDialogView extends UstadView {

    String VIEW_NAME = "AddActivityChangeDialog";


    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Set measurement unit presets
     *
     * @param presets   The presets
     */
    void setMeasurementDropdownPresets(String[] presets);
}
