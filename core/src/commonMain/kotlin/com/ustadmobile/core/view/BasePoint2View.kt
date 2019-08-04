package com.ustadmobile.core.view


/**
 * Core View. Screen is for BasePoint2View's View
 */
interface BasePoint2View : UstadView {

    //Any argument keys:


    fun showCatalog(show: Boolean)
    fun showInventory(show: Boolean)
    fun showSales(show: Boolean)
    fun showCourses(show: Boolean)
    fun shareAppSetupFile(filePath: String)
    fun forceSync()
    fun sendToast(messageId: Int)
    fun checkPermissions()

    fun showSettings(show: Boolean)

    /**
     * Show a dialog used to share the application itself offline
     */
    fun showShareAppDialog()

    /**
     * dismiss the share app dialog
     */
    fun dismissShareAppDialog()

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateNotificationForSales(number: Int)

    fun updateImageOnView(imagePath: String)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "BasePoint2View"
    }

}

