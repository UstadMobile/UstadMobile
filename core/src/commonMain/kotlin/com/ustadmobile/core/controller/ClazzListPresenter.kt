package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClassLogDetailView
import com.ustadmobile.core.view.ClazzEditView
import com.ustadmobile.core.view.ClazzListView
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_ATTENDANCE_ASC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_ATTENDANCE_DESC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_TEACHER_ASC
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The ClazzList's Presenter - responsible for the logic of listing the relevant classes on the
 * Class list screen.
 */
class ClazzListPresenter(context: Any, arguments: Map<String, String>?, view: ClazzListView,
                         val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClazzListView>(context, arguments!!, view) {

    private var clazzListProvider: DataSource.Factory<Int, ClazzWithNumStudents>? = null

    private var idToOrderInteger: HashMap<Long, Int>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    internal var clazzDao = repository.clazzDao

    internal var loggedInPersonUid: Long? = 0L

    private var searchQuery = "%"

    var recordAttendanceVisibility: Boolean? = false
        private set

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
        val objectArr = presetAL.toTypedArray()
        val strArr = arrayOfNulls<String>(objectArr.size)
        for (j in objectArr.indices) {
            strArr[j] = objectArr[j]
        }
        return strArr
    }

    /**
     * The ClazzListPresenter does the following in order:
     * 1. Populates the clazzListProvider and sets it to the view.
     * 2. Updates the Sort drop down options.
     *
     * @param savedState The state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val activeAccount = UmAccountManager.getActiveAccount(context)

        if (activeAccount != null) {

            loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
            idToOrderInteger = HashMap<Long, Int>()

            //Update Sorting options drop down options. This will also trigger the default
            // sort hence attaching the provider to the view.
            updateSortSpinnerPreset()

            //Check permissions
            checkPermissions()
        }
    }

    /**
     * Updates the search provider with the search value given to it.
     * @param searchValue   The search value
     */
    fun updateProviderWithSearch(searchValue: String) {
        searchQuery = "%$searchValue%"
        getAndSetProvider(0)
    }

    /**
     * Checks permission and updates the view accordingly
     */
    fun checkPermissions() {
        GlobalScope.launch {
            val result = clazzDao.personHasPermission(loggedInPersonUid!!, Role
                    .PERMISSION_CLAZZ_INSERT)

            view.showAddClassButton(result)
            view.showAllClazzSettingsButton(result)
        }

        GlobalScope.launch {
            val result = clazzDao.personHasPermission(loggedInPersonUid!!, Role
                    .PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT)
            recordAttendanceVisibility = result
        }

    }

    /**
     * Updates the currently set Provider to the view
     */
    private fun updateProviderToView() {
        view.setClazzListProvider(clazzListProvider!!)
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()

        idToOrderInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.namesb, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.attendance_high_to_low, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_ATTENDANCE_DESC)
        presetAL.add(impl.getString(MessageID.attendance_low_to_high, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_ATTENDANCE_ASC)
        presetAL.add(impl.getString(MessageID.teacher, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_TEACHER_ASC)
        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }

    /**
     * This method updates the Class List provider based on the sort order flag selected.
     * Every order has a corresponding order by change in the database query where this method
     * reloads the class list provider.
     *
     * @param order The order selected.
     */
    private fun getAndSetProvider(order: Int) {
        when (order) {

            SORT_ORDER_NAME_DESC -> clazzListProvider = repository.clazzDao
                    .findAllActiveClazzesSortByNameDesc(searchQuery, loggedInPersonUid!!)
            SORT_ORDER_ATTENDANCE_ASC -> clazzListProvider = repository.clazzDao
                    .findAllActiveClazzesSortByAttendanceAsc(searchQuery, loggedInPersonUid!!)
            SORT_ORDER_ATTENDANCE_DESC -> clazzListProvider = repository.clazzDao
                    .findAllActiveClazzesSortByAttendanceDesc(searchQuery, loggedInPersonUid!!)
            SORT_ORDER_TEACHER_ASC -> clazzListProvider = repository.clazzDao
                    .findAllActiveClazzesSortByTeacherAsc(searchQuery, loggedInPersonUid!!)
            else ->
                //SORT_ORDER_NAME_ASC
                clazzListProvider = repository.clazzDao
                        .findAllActiveClazzesSortByNameAsc(searchQuery, loggedInPersonUid!!)
        }

        updateProviderToView()

    }

    /**
     * Click class card handler. This should go within a class - opening the Class Detail View.
     *
     * @param clazz The class the user clicked on.
     */
    fun handleClickClazz(clazz: Clazz) {
        val args = HashMap<String, String>()
        val clazzUid = clazz.clazzUid
        args.put(ClazzListView.ARG_CLAZZ_UID, clazzUid.toString())

        impl.go(ClazzDetailView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * Click attendance button in Class card handler (as part of every Class item in the Class List
     * recycler view.
     *
     * @param clazz The class the user wants to record attendance for.
     */
    fun handleClickClazzRecordAttendance(clazz: Clazz) {

        GlobalScope.launch {
            val result = repository.clazzLogDao.findMostRecentByClazzUid(clazz.clazzUid)
            if (result == null) {
                view.showMessage(MessageID.no_schedule_message)
            } else {
                val args = HashMap<String, String>()
                val clazzUid = clazz.clazzUid
                args.put(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID, clazzUid.toString())
                impl.go(ClassLogDetailView.VIEW_NAME, args, view.viewContext)
            }
        }

    }

    /**
     * The primary action button for the Class List - here it is to add a new Class . (On Android it
     * is the Floating Action Button).
     * This method will create a new class, get its new ID and open up Class Edit View with the new
     * ID to edit it.
     *
     */
    fun handleClickPrimaryActionButton() {
        val args = HashMap<String, String>()
        args.put(ClazzEditView.ARG_NEW, true.toString())
        impl.go(ClazzEditView.VIEW_NAME, args, context)
    }

    /**
     * Method logic for what happens when we change the order of the clazz list.
     *
     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date
     */
    fun handleChangeSortOrder(order: Long) {
        var order = order
        order = order + 1
        if (idToOrderInteger!!.containsKey(order)) {
            val sortCode = idToOrderInteger!!.get(order)!!
            getAndSetProvider(sortCode)
        }
    }

}
