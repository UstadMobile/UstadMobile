package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

/**
 * ClassLogDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClassLogDetailView : UstadView {

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set ot the Recycler View.
     *
     * @param clazzLogAttendanceRecordProvider The provider data
     */
    fun setClazzLogAttendanceRecordProvider(
            clazzLogAttendanceRecordProvider: DataSource.Factory<Int, ClazzLogAttendanceRecordWithPerson>)

    /**
     * Finish the view (close it)
     */
    fun finish()

    /**
     * Update the title bar of the Take Attendance ClazzLogDetail Activity.
     *
     * @param title The title of the ClazzLogDetailActivity string
     */
    fun updateToolbarTitle(title: String)

    /**
     * Updates the date heading in the activity
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    fun updateDateHeading(dateString: String)

    fun showMarkAllButtons(show: Boolean)

    companion object {

        //The View name
        val VIEW_NAME = "ClassLogDetail"

        /**
         * Used when starting to direct the view to show a particular clazz log
         */
        val ARG_CLAZZ_LOG_UID = "clazzLogUid"

        /**
         * Used when starting to direct the view to show the most recent clazz log (if any)
         */
        val ARG_MOST_RECENT_BY_CLAZZ_UID = "mostRecentByClazzUid"
    }

}
