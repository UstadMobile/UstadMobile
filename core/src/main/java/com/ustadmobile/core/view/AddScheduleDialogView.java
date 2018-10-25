package com.ustadmobile.core.view;


/**
 * AddScheduleDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface AddScheduleDialogView extends UstadView, DismissableDialog {

    String VIEW_NAME = "AddScheduleDialog";

    /**
     * For Android: closes the activity.
     */
    void finish();

    void setScheduleDropdownPresets(String[] presets);

    void setDayDropdownPresets(String[] presets);

    void setError(String errorMessage);
}
