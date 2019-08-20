package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON_TYPE
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.CUSTOM_FIELD_MIN_UID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The ClazzDetailEnrollStudent's presenter - responsible for the logic of Enrolling a student
 * Enrollment detail screen shows all students that are enrolled as well as students not enrolled
 * along with an enrollment tick mark.
 *
 * Gets called when Add Student is pressed when within a Clazz.
 *
 */
class ClazzDetailEnrollStudentPresenter(context: Any, arguments: Map<String, String>?,
                                        view: ClazzDetailEnrollStudentView,
                                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : CommonHandlerPresenter<ClazzDetailEnrollStudentView>(context, arguments!!, view) {

    private var currentClazzUid: Long = 0
    private var currentRole = 0
    private var personWithEnrollmentUmProvider: DataSource.Factory<Int, PersonWithEnrollment>? = null

    //PersonGroup enrollment
    private var groupUid: Long = 0

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzMemberDao = repository.clazzMemberDao

    init {

        //Set current Clazz being enrolled.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        if (arguments!!.containsKey(ARG_NEW_PERSON_TYPE)) {
            currentRole = arguments!!.get(ARG_NEW_PERSON_TYPE)!!.toInt()
        }

        if (arguments!!.containsKey(GROUP_UID)) {
            groupUid = arguments!!.get(GROUP_UID)!!.toLong()
        }
    }

    /**
     * Order:
     * 1. Gets all students with enrollment information from the database.
     * 2. Sets the provider to the view.
     *
     * @param savedState The savedState
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentRole == ClazzMember.ROLE_TEACHER) {

            personWithEnrollmentUmProvider = repository.clazzMemberDao
                    .findAllEligibleTeachersWithEnrollmentForClassUid(currentClazzUid)
        } else if (currentRole == ClazzMember.ROLE_STUDENT) {
            personWithEnrollmentUmProvider = repository.clazzMemberDao
                    .findAllStudentsWithEnrollmentForClassUid(currentClazzUid)
        } else if (groupUid != 0L) {
            //PersonGroup enrollmnet
            personWithEnrollmentUmProvider = repository.personDao.findAllPeopleWithEnrollmentInGroup(groupUid)
        }

        setProviderToView()

    }

    /**
     * Sets the provider attached to this Presenter to the View.
     */
    private fun setProviderToView() {
        view.setStudentsProvider(personWithEnrollmentUmProvider!!)
    }

    /**
     * Handles what happens when new Student button clicked. This will:
     * 1. Create a new student.
     * 2. Create null custom fields for the student.
     * 3. Go to PersonEdit for that person.
     *
     */
    fun handleClickEnrollNewPerson() {
        //Goes to PersonEditActivity with currentClazzUid passed as argument
        val newPerson = Person()
        newPerson.active = false
        val personDao = repository.personDao
        val personFieldDao = repository.personCustomFieldDao
        val customFieldValueDao = repository.personCustomFieldValueDao
        GlobalScope.launch {
            val result = personDao.createPersonAsync(newPerson)
            //Also create null Custom Field values so it shows up in the Edit screen.

            val allCustomFields = personFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID)

            for (everyCustomField in allCustomFields) {
                val cfv = PersonCustomFieldValue()
                cfv.personCustomFieldValuePersonCustomFieldUid = everyCustomField.personCustomFieldUid
                cfv.personCustomFieldValuePersonUid = result!!
                cfv.personCustomFieldValueUid = customFieldValueDao.insert(cfv)
            }

            val args = HashMap<String, String>()
            args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
            args.put(ARG_PERSON_UID, result.toString())
            args.put(ARG_NEW_PERSON_TYPE, currentRole.toString())
            args.put(ARG_NEW_PERSON, "true")
            impl.go(PersonEditView.VIEW_NAME, args, view.viewContext)

        }


    }

    /**
     * Does nothing. Any common handler goes here. Here we are doing nothing. We don't want to see Student Details
     * when we are in the enrollment screen.
     *
     * @param arg   Any argument to the handler.
     */
    override fun handleCommonPressed(arg: Any) {}

    /**
     * The secondary handler for the Enrollment screen on the main recycler view - is to toggle
     * enrollment for that student pressed.
     *
     * @param arg The argument here - is a Map of the student id and the enrollment status
     */
    override fun handleSecondaryPressed(arg: Any) {

        //The Unchecked cast warning is expected. We are making a personal assumption from the View.
        val argument = arg as Map.Entry<PersonWithEnrollment, Boolean>

        handleEnrollChanged(argument.key, argument.value)
    }

    /**
     * Handles role changed for every person. This method will update the clazzMember for the
     * person whose role changed. If the student person does not have a Clazz Member entry, it will
     * create one and persist the database with the new value.
     *
     * @param person The person with Enrollment object whose to be enrolled or not.
     * @param enrolled  The enrolled status. True for enrolled, False for un-enrolled.
     */
    private fun handleEnrollChanged(person: PersonWithEnrollment, enrolled: Boolean) {

        if (groupUid != 0L) {
            //PersonGroup enrollment.
            val groupMemberDao = repository.personGroupMemberDao

            GlobalScope.launch {
                val existingGroupMember = groupMemberDao.findMemberByGroupAndPersonAsync(groupUid,
                        person.personUid)


                if (enrolled) {
                    if (existingGroupMember == null) {
                        //Create the PersonGroupMember
                        val newGroupMember = PersonGroupMember()
                        newGroupMember.groupMemberGroupUid = groupUid
                        newGroupMember.groupMemberPersonUid = person.personUid
                        newGroupMember.groupMemberActive = (true)
                        val result = groupMemberDao.insertAsync(newGroupMember)
                        newGroupMember.groupMemberUid = result

                    } else {
                        if (!existingGroupMember.groupMemberActive) {
                            existingGroupMember.groupMemberActive = (true)
                            groupMemberDao.update(existingGroupMember)
                        }
                        //else let it be
                    }

                } else {
                    //if already enrolled, disable ClazzMember.
                    if (existingGroupMember != null) {
                        existingGroupMember.groupMemberActive = (false)
                        groupMemberDao.update(existingGroupMember)
                    }
                }
            }

            return
        }

        GlobalScope.launch {
            val existingClazzMember = clazzMemberDao.findByPersonUidAndClazzUidAsync(person.personUid,
                    currentClazzUid)

            if (enrolled) {
                if (existingClazzMember == null) {
                    //Create the ClazzMember
                    val newClazzMember = ClazzMember()
                    newClazzMember.clazzMemberClazzUid = currentClazzUid
                    if (currentRole == ClazzMember.ROLE_TEACHER) {
                        newClazzMember.clazzMemberRole = (ClazzMember.ROLE_TEACHER)
                    } else {
                        newClazzMember.clazzMemberRole = (ClazzMember.ROLE_STUDENT)
                    }
                    newClazzMember.clazzMemberPersonUid = person.personUid
                    newClazzMember.clazzMemberDateJoined = UMCalendarUtil.getDateInMilliPlusDays(0)
                    newClazzMember.clazzMemberActive = true
                    val result = clazzMemberDao.insertAsync(newClazzMember)
                    newClazzMember.clazzMemberUid = result

                } else {
                    if (!existingClazzMember.clazzMemberActive) {
                        existingClazzMember.clazzMemberActive = true
                        clazzMemberDao.update(existingClazzMember)
                    }
                    //else let it be
                }

            } else {
                //if already enrolled, disable ClazzMember.
                if (existingClazzMember != null) {
                    existingClazzMember.clazzMemberActive = false
                    clazzMemberDao.update(existingClazzMember)
                }
            }
        }
    }

    /**
     * Handles what happens when Done clicked on the list- No need to do anything. Recycler View
     * already made those changes. Just exit the activity (finish).
     */
    fun handleClickDone() {
        view.finish()
    }

}
