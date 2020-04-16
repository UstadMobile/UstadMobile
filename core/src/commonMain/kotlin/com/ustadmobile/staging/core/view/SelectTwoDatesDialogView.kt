package com.ustadmobile.core.view


/**
 * SelectTwoDatesDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectTwoDatesDialogView : UstadView {


    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        val VIEW_NAME = "SelectTwoDatesDialog"
    }

}
