package com.ustadmobile.staging.core.view

import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz

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
    fun setClazz(clazz: Clazz)

    fun setupTabs(tabs: List<String>)

    fun setSettingsVisibility(visible: Boolean)

    companion object {

        //The View name
        val VIEW_NAME = "ClassDetail"
    }

}
