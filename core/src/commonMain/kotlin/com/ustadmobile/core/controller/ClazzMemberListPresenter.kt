package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.approvePendingClazzMember
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzMemberListPresenter(context: Any, arguments: Map<String, String>, view: ClazzMemberListView,
                          di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzMemberListView, ClazzMember>(context, arguments, view, di, lifecycleOwner) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    var filterByClazzUid: Long = -1

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ClazzMemberListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        filterByClazzUid = arguments[ARG_FILTER_BY_CLAZZUID]?.toLong() ?: -1
        super.onCreate(savedState)

        view.sortOptions = SortOrder.values().toList().map { ClazzMemberListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    override suspend fun onLoadFromDb() {
        super.onLoadFromDb()

        view.list = repo.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzMember.ROLE_TEACHER)
        view.studentList = repo.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzMember.ROLE_STUDENT)
        val activePersonUid = accountManager.activeAccount.personUid

        view.addStudentVisible = db.clazzDao.personHasPermissionWithClazz(activePersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_STUDENT)

        if(view.addStudentVisible) {
            view.pendingStudentList = db.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                    ClazzMember.ROLE_STUDENT_PENDING)
        }

        view.addTeacherVisible = db.clazzDao.personHasPermissionWithClazz(activePersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_TEACHER)
    }


    override fun handleClickEntry(entry: ClazzMember) {
        //Just go to PersonDetail - this view is not used as a picker
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.clazzMemberPersonUid.toString()), context)
    }

    fun handleClickPendingRequest(member: ClazzMember, approved: Boolean) {
        GlobalScope.launch {
            if(approved) {
                repo.approvePendingClazzMember(member)
            }else {
                repo.clazzMemberDao.updateAsync(member.also {
                    it.clazzMemberActive = false
                })
            }
        }

    }

    override fun handleClickCreateNewFab() {
        //there really isn't a fab here. There are buttons for add teacher and add student in the list itself
    }

    fun handleEnrolMember(person: Person, role: Int) {
        GlobalScope.launch {
            repo.enrolPersonIntoClazzAtLocalTimezone(person, filterByClazzUid, role)
        }
    }


    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzMemberListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder

        }
    }
}