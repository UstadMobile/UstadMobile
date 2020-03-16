package com.ustadmobile.core.view

interface BulkUploadMasterView : UstadView {

    /**
     * Set time zone as list to the activity.
     *
     * @param timeZoneIds   List of time zone ids
     */
    fun setTimeZonesList(timeZoneIds: List<String>)

    /**
     * Starts the file picker view
     */
    fun chooseFileFromDevice()

    fun finish()

    /**
     * Parse file
     * @param filePath  Path of the file to parse
     */
    fun parseFile(filePath: String)

    /**
     * Show a message on the screen.
     * @param message   The message
     */
    fun showMessage(message: String)

    /**
     * Sets if the bulk upload is in progress or not.
     * @param inProgress    true if bulk upload is in progress
     */
    fun setInProgress(inProgress: Boolean)

    /**
     * Updates the progress of bulk upload on the view. A percentage will be calculated
     * @param line  The line number
     * @param nlines    The total number of lines
     */
    fun updateProgressValue(line: Int, nlines: Int)

    fun addError(message: String, error: Boolean)

    fun addError(message: String)

    fun setErrorHeading(messageId: Int)

    fun getAllErrors(): MutableList<String>?

    companion object {

        val VIEW_NAME = "BulkUploadMaster"
    }
}
