package com.ustadmobile.core.view;


/**
 * ClazzActivityEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzActivityEditView extends UstadView {

    //View name
    String VIEW_NAME = "ClazzActivityEdit";

    int THUMB_OFF = 0;
    int THUMB_GOOD = 1;
    int THUMB_BAD = 2;

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


    void setActivityChangeOption(long option);

    /**
     * Sets which thumb is active based on the flag
     *
     * @param thumbs    The thumb flag
     */
    void setThumbs(int thumbs);

    /**
     * Set notes to the view.
     *
     * @param notes The notes
     */
    void setNotes(String notes);

    /**
     * Sets the unit of measure string
     *
     * @param uomText   The unit of measurement value to be populated.
     */
    void setUOMText(String uomText);

    /**
     * Method to hide / show the measurement bit - useful when no Activity Change is selected.
     */
    void setMeasureBitVisibility(boolean visible);

    /**
     * Sets the true/false visibility
     *
     * @param visible   true if visible, false if invisible
     */
    void setTrueFalseVisibility(boolean visible);

    /**
     * Sets the dateString to the View
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    void updateDateHeading(String dateString);

}
