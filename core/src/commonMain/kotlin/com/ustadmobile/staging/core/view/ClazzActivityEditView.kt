package com.ustadmobile.core.view


/**
 * ClazzActivityEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzActivityEditView : UstadView {

    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Update the toolbar's title with the given text.
     *
     * @param title     The string title
     */
    fun updateToolbarTitle(title: String)

    /**
     * Set all activity change presets to the view to be selected.
     *
     * @param presets   The string array in order to be populated in the Activity change drop down /
     * spinner
     */
    fun setClazzActivityChangesDropdownPresets(presets: Array<String>)

    /**
     * Sets the view type for the unit of measurement chosen.
     *
     * @param uomType   The type as per ClazzActivityChange
     */
    fun setUnitOfMeasureType(uomType: Long)


    fun setActivityChangeOption(option: Long)

    /**
     * Sets which thumb is active based on the flag
     *
     * @param thumbs    The thumb flag
     */
    fun setThumbs(thumbs: Int)

    /**
     * Set notes to the view.
     *
     * @param notes The notes
     */
    fun setNotes(notes: String)

    /**
     * Sets the unit of measure string
     *
     * @param uomText   The unit of measurement value to be populated.
     */
    fun setUOMText(uomText: String)

    /**
     * Method to hide / show the measurement bit - useful when no Activity Change is selected.
     */
    fun setMeasureBitVisibility(visible: Boolean)

    /**
     * Sets the true/false visibility
     *
     * @param visible   true if visible, false if invisible
     */
    fun setTrueFalseVisibility(visible: Boolean)

    /**
     * Sets the dateString to the View
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    fun updateDateHeading(dateString: String)

    fun showFAB(show: Boolean)

    fun setEditable(editable: Boolean)

    companion object {

        //View name
        val VIEW_NAME = "ClazzActivityEdit"

        val THUMB_OFF = 0
        val THUMB_GOOD = 1
        val THUMB_BAD = 2

        //Arguments
        val ARG_CLAZZACTIVITY_UID = "clazzActivityUid"
        val ARG_CLAZZACTIVITY_LOGDATE = "ClazzActivityLogDate"
    }

}
