package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.db.dao.PersonCustomFieldDao
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonGroupMemberDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonField
import com.ustadmobile.lib.db.entities.PersonGroupMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON_TYPE
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.CUSTOM_FIELD_MIN_UID

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
    private var personWithEnrollmentUmProvider: UmProvider<PersonWithEnrollment>? = null

    //PersonGroup enrollment
    private var groupUid: Long = 0

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzMemberDao = repository.clazzMemberDao

    init {

        //Set current Clazz being enrolled.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)
        }

        if (arguments!!.containsKey(ARG_NEW_PERSON_TYPE)) {
            currentRole = arguments!!.get(ARG_NEW_PERSON_TYPE)
        }

        if (arguments!!.containsKey(GROUP_UID)) {
            groupUid = arguments!!.get(GROUP_UID)
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

        personDao.createPersonAsync(newPerson, object : UmCallback<Long> {

            override fun onSuccess(result: Long?) {

                //Also create null Custom Field values so it shows up in the Edit screen.
                personFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID,
                        object : UmCallback<List<PersonField>> {
                            override fun onSuccess(allCustomFields: List<PersonField>?) {

                                for (everyCustomField in allCustomFields!!) {
                                    val cfv = PersonCustomFieldValue()
                                    cfv.setPersonCustomFieldValuePersonCustomFieldUid(
                                            everyCustomField.personCustomFieldUid)
                                    cfv.setPersonCustomFieldValuePersonUid(result)
                                    cfv.personCustomFieldValueUid = customFieldValueDao.insert(cfv)
                                }

                                val args = HashMap<String, String>()
                                args.put(ARG_CLAZZ_UID, currentClazzUid)
                                args.put(ARG_PERSON_UID, result)
                                args.put(ARG_NEW_PERSON_TYPE, currentRole)
                                args.put(ARG_NEW_PERSON, "true")
                                impl.go(PersonEditView.VIEW_NAME, args, view.getContext())
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        })
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })


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
        val argument = arg as Entry<PersonWithEnrollment, Boolean>

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

            groupMemberDao.findMemberByGroupAndPersonAsync(groupUid, person.personUid,
                    object : UmCallback<PersonGroupMember> {
                        override fun onSuccess(existingGroupMember: PersonGroupMember?) {


                            if (enrolled) {
                                if (existingGroupMember == null) {
                                    //Create the PersonGroupMember
                                    val newGroupMember = PersonGroupMember()
                                    newGroupMember.groupMemberGroupUid = groupUid
                                    newGroupMember.groupMemberPersonUid = person.personUid
                                    newGroupMember.setGroupMemberActive(true)
                                    groupMemberDao.insertAsync(newGroupMember, object : UmCallback<Long> {
                                        override fun onSuccess(result: Long?) {
                                            newGroupMember.groupMemberUid = result
                                        }

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })

                                } else {
                                    if (!existingGroupMember.isGroupMemberActive()) {
                                        existingGroupMember.setGroupMemberActive(true)
                                        groupMemberDao.update(existingGroupMember)
                                    }
                                    //else let it be
                                }

                            } else {
                                //if already enrolled, disable ClazzMember.
                                if (existingGroupMember != null) {
                                    existingGroupMember.setGroupMemberActive(false)
                                    groupMemberDao.update(existingGroupMember)
                                }
                            }
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })

            return
        }

        clazzMemberDao.findByPersonUidAndClazzUidAsync(person.personUid, currentClazzUid,
                object : UmCallback<ClazzMember> {

                    override fun onSuccess(existingClazzMember: ClazzMember?) {

                        if (enrolled) {
                            if (existingClazzMember == null) {
                                //Create the ClazzMember
                                val newClazzMember = ClazzMember()
                                newClazzMember.clazzMemberClazzUid = currentClazzUid
                                if (currentRole == ClazzMember.ROLE_TEACHER) {
                                    newClazzMember.setRole(ClazzMember.ROLE_TEACHER)
                                } else {
                                    newClazzMember.setRole(ClazzMember.ROLE_STUDENT)
                                }
                                newClazzMember.clazzMemberPersonUid = person.personUid
                                newClazzMember.setDateJoined(System.currentTimeMillis())
                                newClazzMember.clazzMemberActive = true
                                clazzMemberDao.insertAsync(newClazzMember, object : UmCallback<Long> {
                                    override fun onSuccess(result: Long?) {
                                        newClazzMember.clazzMemberUid = result
                                    }

                                    override fun onFailure(exception: Throwable?) {
                                        print(exception!!.message)
                                    }
                                })
                            } else {
                                if (!existingClazzMember.isClazzMemberActive()) {
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

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }

    /**
     * Handles what happens when Done clicked on the list- No need to do anything. Recycler View
     * already made those changes. Just exit the activity (finish).
     */
    fun handleClickDone() {
        view.finish()
    }

}
