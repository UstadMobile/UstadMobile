package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.CallPersonRelatedDialogView
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CallPersonRelatedDialogPresenter(context: Any, arguments: Map<String, String>?,
                                       view: CallPersonRelatedDialogView,
                                       val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<CallPersonRelatedDialogView>(context, arguments!!, view) {


    private var currentPerson: Person? = null
    internal var personUid: Long = 0
    private var clazzUid: Long = 0
    internal var repository: UmAppDatabase

    /**
     * Simple POJO representing a string and value.
     */
    inner class NameWithNumber internal constructor(var name: String, var number: String)

    init {

        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            personUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        } else {
            personUid = 0
        }
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            clazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        } else {
            clazzUid = 0
        }
        repository = UmAccountManager.getRepositoryForActiveAccount(this.context)
    }

    private fun generateCallingMap(personToCall: Person, teacherToCall: ClazzMemberWithPerson,
                                   mainOfficer: Person?) {

        val putThisMap = LinkedHashMap<Int, NameWithNumber>()
        putThisMap[NUMBER_FATHER] = NameWithNumber(impl.getString(MessageID.father, context) +
                "(" + personToCall.fatherName + ")", personToCall.fatherNumber.toString())
        putThisMap[NUMBER_MOTHER] = NameWithNumber(impl.getString(MessageID.mother, context) +
                "(" + personToCall.motherName + ")", personToCall.motherNum.toString())
        putThisMap[NUMBER_TEACHER] = NameWithNumber(impl.getString(MessageID.teacher, context) +
                "(" + teacherToCall.person!!.firstNames + " " +
                teacherToCall.person!!.lastName + ")",
                teacherToCall.person!!.phoneNum!!)


        if (mainOfficer != null) {
            putThisMap[NUMBER_RETENTION_OFFICER] = NameWithNumber(impl.getString(MessageID.retention_officer, context) +
                    "(" + mainOfficer.firstNames + " " + mainOfficer.lastName + ")",
                    personToCall.phoneNum.toString())
            view.showRetention(true)
        } else {
            view.showRetention(false)
        }

        view.setOnDisplay(putThisMap)
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val personDao = repository.personDao

        GlobalScope.launch {
            val result = personDao.findByUidAsync(personUid)
            if (result != null) {
                currentPerson = result
                getRelatedPeople()
            }
        }
    }

    private fun getRelatedPeople() {
        val clazzMemberDao = repository.clazzMemberDao
        val entityRoleDao = repository.entityRoleDao
        val roleDao = repository.roleDao
        val groupMemberDao = repository.personGroupMemberDao
        GlobalScope.launch {
            val result = clazzMemberDao.findClazzMemberWithPersonByRoleForClazzUid(clazzUid,
                    ClazzMember.ROLE_TEACHER)
            val mainTeacher: ClazzMemberWithPerson
            var mainOfficer: Person? = null
            if (!result!!.isEmpty()) {
                mainTeacher = result[0]

                val officerRole = roleDao.findByNameSync(Role.ROLE_NAME_OFFICER)
                val officerEntityRoles = entityRoleDao.findGroupByRoleAndEntityTypeAndUidSync(Clazz.TABLE_ID,
                        clazzUid, officerRole!!.roleUid)
                if (officerEntityRoles.size > 0) {
                    val mainOfficerGroupUid = officerEntityRoles[0].erGroupUid
                    val officers = groupMemberDao.findPersonByGroupUid(mainOfficerGroupUid)
                    mainOfficer = officers[0]
                }

                generateCallingMap(currentPerson!!, mainTeacher, mainOfficer)
            }
        }
    }

    companion object {
        val NUMBER_FATHER = 1
        val NUMBER_MOTHER = 2
        val NUMBER_TEACHER = 3
        val NUMBER_RETENTION_OFFICER = 4
    }
}
