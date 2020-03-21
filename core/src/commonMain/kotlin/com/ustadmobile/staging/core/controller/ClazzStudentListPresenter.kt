package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON_TYPE
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_ATTENDANCE_ASC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_ATTENDANCE_DESC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.core.view.ClazzStudentListView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The Presenter/Controller for ClazzStudentListFragment. This is responsible for the logic of
 * populating the Student list for the Class Selected and other actions associated with it like
 * navigating to student detail view and adding/enrolling a new student.
 *
 */
class ClazzStudentListPresenter(context: Any,
                                arguments: Map<String, String>?,
                                view: ClazzStudentListView,
                                val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        CommonHandlerPresenter<ClazzStudentListView>(context, arguments!!, view) {

    private var currentClazzId = -1L
    private var clazzPersonListProvider: DataSource.Factory<Int, PersonWithEnrollment>? = null

    private var idToOrderInteger: HashMap<Long, Int>? = null

    var isTeachersEditable = false
        private set

    var isCanAddTeachers = false
        private set
    var isCanAddStudents = false
        private set

    private val loggedInPerson: Long

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    val clazzDao = repository.clazzDao

    init {

        //Get Clazz Uid from argument and set it here to the Presenter
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzId = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        loggedInPerson = UmAccountManager.getActiveAccount(context)!!.personUid
    }


    /**
     * The Presenter here's onCreate. In Order:
     * 1. This populates the provider and sets it to the View.
     *
     * This will be called when the implementation View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState    The saved state
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        this.isTeachersEditable = true

        //Initialise sort spinner data:
        idToOrderInteger = HashMap<Long, Int>()
        updateSortSpinnerPreset()
    }

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

        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }


    /**
     * Method logic for what happens when you click Add Student
     *
     */
    private fun goToAddStudentFragment() {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzId.toString())
        args.put(ARG_NEW_PERSON_TYPE, ClazzMember.ROLE_STUDENT.toString())
        impl.go(ClazzDetailEnrollStudentView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * Method logic for what happens when you click Add Teacher
     *
     */
    private fun handleAddTeacher() {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzId.toString())
        args.put(ARG_NEW_PERSON_TYPE, ClazzMember.ROLE_TEACHER.toString())
        impl.go(ClazzDetailEnrollStudentView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * This method handles what happens when a student is clicked - It goes to the PersonDetailView
     * where it shows all information about the student/person.
     *
     * @param personUid Ths student Person UID.
     */
    private fun handleClickStudent(personUid: Long) {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzId.toString())
        args.put(ARG_PERSON_UID, personUid.toString())
        impl.go(PersonDetailView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * Method logic for what happens when we change the order of the student list.
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

    /**
     * This method updates the Class List provider based on the sort order flag selected.
     * Every order has a corresponding order by change in the database query where this method
     * reloads the class list provider.
     *
     * @param order The order selected.
     */
    private fun getAndSetProvider(order: Int) {
        when (order) {
            SORT_ORDER_NAME_ASC -> clazzPersonListProvider = repository.clazzMemberDao
                    .findAllPersonWithEnrollmentInClazzByClazzUidSortByNameAsc(currentClazzId)
            SORT_ORDER_NAME_DESC -> clazzPersonListProvider = repository.clazzMemberDao
                    .findAllPersonWithEnrollmentInClazzByClazzUidSortByNameDesc(currentClazzId)
            SORT_ORDER_ATTENDANCE_ASC -> clazzPersonListProvider = repository.clazzMemberDao
                    .findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceAsc(currentClazzId)
            SORT_ORDER_ATTENDANCE_DESC -> clazzPersonListProvider = repository.clazzMemberDao
                    .findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceDesc(currentClazzId)
            else -> clazzPersonListProvider = repository.clazzMemberDao
                    .findAllPersonWithEnrollmentInClazzByClazzUidSortByNameAsc(currentClazzId)
        }

        updateProviderToView()

    }

    private fun updateProviderToView() {
        GlobalScope.launch {
            val result = clazzDao.personHasPermission(loggedInPerson, Role
                    .PERMISSION_PERSON_INSERT)
            isCanAddStudents = result!!
            isCanAddTeachers = result

            view.runOnUiThread(Runnable {
                view.setPersonWithEnrollmentProvider(clazzPersonListProvider!!)
            })
        }

    }

    /**
     * This is the the primary action button for the list of students in this screen. It calls
     * handleClickStudent which in turn goes to PersonDetailView.
     *
     * @param arg The argument passed to the primary handler which is the Person's Uid.
     */
    override fun handleCommonPressed(arg: Any) {

        if ((arg as Long) == 0L) {
            goToAddStudentFragment()
        } else {
            handleClickStudent(arg)
        }
    }

    /**
     * The secondary action handler for the student list recycler adapter. There is no secondary
     * action on the list of students yet. For now, this does nothing.
     * @param arg   The argument passed to the secondary action handler of every item in the
     * recycler view.
     */
    override fun handleSecondaryPressed(arg: Any) {
        //No secondary action here.
        if ((arg as Long) < 0) {
            handleAddTeacher()
        }
    }

}
