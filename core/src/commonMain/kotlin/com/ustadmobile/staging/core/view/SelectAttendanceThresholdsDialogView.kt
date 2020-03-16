package com.ustadmobile.core.view


/**
 * SelectAttendanceThresholdsDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectAttendanceThresholdsDialogView : UstadView {


    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        val VIEW_NAME = "SelectAttendanceThresholdsDialog"
    }

}
