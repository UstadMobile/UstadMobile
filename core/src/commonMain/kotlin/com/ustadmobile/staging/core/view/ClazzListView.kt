package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

/**
 * ClassList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzListView : UstadView {

    /**
     * Sets the class list as provider to the view.
     * @param clazzListProvider The UMProvider provider of ClazzWithNumStudents Type.
     */
    fun setClazzListProvider(clazzListProvider: DataSource.Factory<Int, ClazzWithNumStudents>)

    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    fun updateSortSpinner(presets: Array<String?>)


    fun showAddClassButton(show: Boolean)


    fun showAllClazzSettingsButton(show: Boolean)


    fun forceCheckPermissions()

    /**
     * Shows a message to view. Can be as simple as toast on Android
     * @param messageID   The message ID we want to show.
     */
    fun showMessage(messageID: Int)

    companion object {

        val VIEW_NAME = "ClassList"

        val ARG_CLAZZLOG_UID = "clazzloguid"

        val ARG_CLAZZ_MOST_RECENT = "clazzMostRecent"

        @Deprecated("")
        val ARG_LOGDATE = "logdate"

        @Deprecated("Use UstadView arg")
        val ARG_CLAZZ_UID = "ClazzUid"

        val SORT_ORDER_NAME_ASC = 1
        val SORT_ORDER_NAME_DESC = 2
        val SORT_ORDER_ATTENDANCE_ASC = 3
        val SORT_ORDER_ATTENDANCE_DESC = 4
        val SORT_ORDER_TEACHER_ASC = 5
    }

}
