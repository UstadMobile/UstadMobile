package com.ustadmobile.core.view

/**
 * ClassDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzDetailView : UstadView {

    /**
     * Sets the toolbar of the view.
     *
     * @param toolbarTitle The toolbar title
     */
    fun setToolbarTitle(toolbarTitle: String)

    fun setAttendanceVisibility(visible: Boolean)

    fun setActivityVisibility(visible: Boolean)

    fun setSELVisibility(visible: Boolean)

    fun setSettingsVisibility(visible: Boolean)

    fun setupViewPager()

    companion object {

        //The View name
        val VIEW_NAME = "ClassDetail"
    }

}
