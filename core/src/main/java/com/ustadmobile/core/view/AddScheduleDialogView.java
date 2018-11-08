package com.ustadmobile.core.view;


/**
 * AddScheduleDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface AddScheduleDialogView extends UstadView, DismissableDialog {

    //View name
    String VIEW_NAME = "AddScheduleDialog";

    int EVERY_DAY_SCHEDULE_POSITION = 0;

    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Sets all the presets of Schedule drop down / spinner.
     *
     * @param presets   a string array of the presets in order.
     */
    void setScheduleDropdownPresets(String[] presets);

    /**
     * Sets all the presets in the Day dropdown spinner
     *
     * @param presets   a string array of the presets in order.
     */
    void setDayDropdownPresets(String[] presets);

    /**
     * Sets an error on the dialog if the input wasn't valid.
     *
     * @param errorMessage  the error message you want to display in the dialog.
     */
    void setError(String errorMessage);

    /**
     * Hides the day spinner (picker). This is usually called when "Every day" schedule is selected.
     * @param hide  true if we want to hide it. false if we want to show it.
     */
    void hideDayPicker(boolean hide);
}
