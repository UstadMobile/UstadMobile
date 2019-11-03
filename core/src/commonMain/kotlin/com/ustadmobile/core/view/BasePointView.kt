package com.ustadmobile.core.view

import com.ustadmobile.core.model.NavigationItem

/**
 * Core view for main application base point - show bottom navigation and its items
 */
interface BasePointView : UstadView {

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

    fun setupNavigation(items: List<NavigationItem>)

    fun showDownloadAllButton(show:Boolean)

    fun loadProfileIcon(profileUrl: String);

    fun loadProfileImage(imagePath: String)

    companion object {

        val VIEW_NAME = "BasePoint2"
    }
}
