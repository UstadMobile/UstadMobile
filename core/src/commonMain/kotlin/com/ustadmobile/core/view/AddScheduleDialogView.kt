package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.Schedule

/**
 * AddScheduleDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface AddScheduleDialogView : UstadView, DismissableDialog {

    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Sets all the presets of Schedule drop down / spinner.
     *
     * @param presets   a string array of the presets in order.
     */
    fun setScheduleDropdownPresets(presets: Array<String>)

    /**
     * Sets all the presets in the Day dropdown spinner
     *
     * @param presets   a string array of the presets in order.
     */
    fun setDayDropdownPresets(presets: Array<String>)

    /**
     * Sets an error on the dialog if the input wasn't valid.
     *
     * @param errorMessage  the error message you want to display in the dialog.
     */
    fun setError(errorMessage: String)

    /**
     * Hides the day spinner (picker). This is usually called when "Every day" schedule is selected.
     * @param hide  true if we want to hide it. false if we want to show it.
     */
    fun hideDayPicker(hide: Boolean)

    /**
     * Updates the schedule for editing existing schedule
     * @param schedule  The schedule object
     */
    fun updateFields(schedule: Schedule)

    companion object {

        //View name
        val VIEW_NAME = "AddScheduleDialog"

        val EVERY_DAY_SCHEDULE_POSITION = 0
    }
}
