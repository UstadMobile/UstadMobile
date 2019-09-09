package com.ustadmobile.core.view


/**
 * AddActivityChangeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface AddActivityChangeDialogView : UstadView {


    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Set measurement unit presets
     *
     * @param presets   The presets
     */
    fun setMeasurementDropdownPresets(presets: Array<String>)

    companion object {

        val VIEW_NAME = "AddActivityChangeDialog"
    }
}
