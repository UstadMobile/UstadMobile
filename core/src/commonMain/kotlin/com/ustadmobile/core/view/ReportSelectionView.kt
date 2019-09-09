package com.ustadmobile.core.view


/**
 * ReportSelection Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ReportSelectionView : UstadView {


    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        val VIEW_NAME = "ReportSelection"
    }

}
