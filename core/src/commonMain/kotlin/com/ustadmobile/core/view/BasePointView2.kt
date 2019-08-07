package com.ustadmobile.core.view

/**
 * Core view for main application base point - show bottom navigation and its items
 */
interface BasePointView2 : UstadView {

    /**
     * Show a dialog used to share the application itself offline
     */
    fun showShareAppDialog()

    fun dismissShareAppDialog()

    fun shareAppSetupFile(filePath: String)

    /**
     * Shows the bulk upload button if logged-in person is admin or not depending on the parameter.
     * @param show  boolean argument. if true, the bulk master button will show up. if false it will
     * hide it.
     */
    fun showBulkUploadForAdmin(show: Boolean)

    fun showSettings(show: Boolean)

    fun updatePermissionCheck()

    fun forceSync()

    fun showMessage(message: String)

    companion object {

        val VIEW_NAME = "PeopleHome"
    }
}
