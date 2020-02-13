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
    private val personGroupDao = repository.personGroupDao
    private val entityRoleDao = repository.entityRoleDao
    private val roleDao = repository.roleDao

    private var teacherRole: Role? = null

    init {
        //Set current Clazz being enrolled.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments[ARG_CLAZZ_UID]!!.toLong()
        }

        if (arguments.containsKey(ARG_NEW_PERSON_TYPE)) {
            currentRole = arguments[ARG_NEW_PERSON_TYPE]!!.toInt()
        }

        //Group uid usually used and given in editing Groups from GroupDetail screen.
        if (arguments.containsKey(GROUP_UID)) {
            groupUid = arguments[GROUP_UID]!!.toLong()
        }
    }

    /**
     * Order:
     * 1. Gets all students with enrollment information from the database.
     * 2. Sets the provider to the view.
     *
     * @param savedState The savedState
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentRole == ClazzMember.ROLE_TEACHER) {

            personWithEnrollmentUmProvider = repository.clazzMemberDao
                    .findAllEligibleTeachersWithEnrollmentForClassUid(currentClazzUid)
        } else if (currentRole == ClazzMember.ROLE_STUDENT) {
            personWithEnrollmentUmProvider = repository.clazzMemberDao
                    .findAllStudentsWithEnrollmentForClassUid(currentClazzUid)
        } else if (groupUid != 0L) {
            //PersonGroup enrollment - usually called from GroupDetail screen.
            personWithEnrollmentUmProvider = repository.personDao.findAllPeopleWithEnrollmentInGroup(groupUid)
        }

        GlobalScope.launch {
            teacherRole = roleDao.findByName(Role.ROLE_NAME_TEACHER)
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

        GlobalScope.launch {
            var loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
            if(loggedInPersonUid != null){
                loggedInPersonUid = 0
            }
            val newPersonUid = personDao.createPersonAsync(newPerson, loggedInPersonUid)

            val pwe = PersonWithEnrollment()
            pwe.personUid = newPerson.personUid

            handleEnrollChanged(pwe, true)

            val args = HashMap<String, String>()
            args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
            args.put(ARG_PERSON_UID, newPersonUid.toString())
            args.put(ARG_NEW_PERSON_TYPE, currentRole.toString())
            args.put(ARG_NEW_PERSON, "true")
            view.finish()
            impl.go(PersonEditView.VIEW_NAME, args, view.viewContext)
        }
    }

    /**
     * Does nothing. Any common handler goes here. Here we are doing nothing.
     * We don't want to see Student Details when we are in the enrollment screen.
     *
     * @param arg   Any argument to the handler.
     */
    override fun handleCommonPressed(arg: Any, arg2:Any) {}

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

        if (groupUid != 0L) {   //Usually called from GroupDetail screen.
            //PersonGroup enrollment.
            val groupMemberDao = repository.personGroupMemberDao

            GlobalScope.launch {

                //Find existing group member in the group
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
            //Find existing clazz Member (if it exists)
            val existingClazzMember =
                    clazzMemberDao.findByPersonUidAndClazzUidAsync(person.personUid,
                            currentClazzUid)

            //will be true if current Role is teacher, else false.
            var isTeacher: Boolean = currentRole == ClazzMember.ROLE_TEACHER

            //Get person's individual person group
            // Assumption: Person will always have its own group. We can create it if it
            // doesn't exist
            var personGroup = personGroupDao.findPersonIndividualGroup(person.personUid)
            if(personGroup == null){
                println(">>PERSON's GROUP NOT CREATED> ARE YOU SURE ITS NOT CREATED?>")
                personGroup = PersonGroup()
                personGroup.groupName = person.fullName(impl.getLocale(context)) + "'s Individual group"
                personGroup.groupActive = true
                personGroup.groupPersonUid = person.personUid
                personGroup.groupUid = personGroupDao.insert(personGroup)
            }

            //Find existing Role Assignment
            val personClazzTeacherAssignments: List<EntityRole>
            var personClazzTeacherAssignment: EntityRole? = null
            if(teacherRole != null) {
                personClazzTeacherAssignments =
                        entityRoleDao.findByEntitiyAndPersonGroupAndRole(Clazz.TABLE_ID,
                            currentClazzUid, personGroup.groupUid, teacherRole!!.roleUid)
                if(!personClazzTeacherAssignments.isEmpty()){
                    personClazzTeacherAssignment = personClazzTeacherAssignments.get(0)
                }else{
                    //Create new assignment
                    if(isTeacher) {
                        personClazzTeacherAssignment = EntityRole(Clazz.TABLE_ID, currentClazzUid,
                                personGroup.groupUid, teacherRole!!.roleUid)
                        personClazzTeacherAssignment.erActive = false
                        entityRoleDao.insert(personClazzTeacherAssignment)
                    }
                }
            }


            if (enrolled) {
                if (existingClazzMember == null) {
                    //Create the ClazzMember
                    val newClazzMember = ClazzMember()
                    newClazzMember.clazzMemberClazzUid = currentClazzUid
                    if (isTeacher) {
                        newClazzMember.clazzMemberRole = (ClazzMember.ROLE_TEACHER)
                    } else {
                        newClazzMember.clazzMemberRole = (ClazzMember.ROLE_STUDENT)
                    }
                    newClazzMember.clazzMemberPersonUid = person.personUid
                    newClazzMember.clazzMemberDateJoined =
                            UMCalendarUtil.getDateInMilliPlusDays(0)
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

                //TODOne: Also assign Teacher/Student role to the person in its person group scoped
                // to this class.

                if (isTeacher && personClazzTeacherAssignment != null) {
                    personClazzTeacherAssignment.erActive = true
                    entityRoleDao.update(personClazzTeacherAssignment)
                }

            } else {
                //if already enrolled, disable ClazzMember.
                if (existingClazzMember != null) {
                    existingClazzMember.clazzMemberActive = false
                    clazzMemberDao.update(existingClazzMember)
                }

                //TODOne: If role assignment exists then deactivate it.
                if (isTeacher && personClazzTeacherAssignment != null) {
                    personClazzTeacherAssignment.erActive = false
                    entityRoleDao.update(personClazzTeacherAssignment)
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
