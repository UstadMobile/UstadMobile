package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.enrollPersonToSchool
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class SchoolMemberListPresenter(context: Any, arguments: Map<String, String>, view: SchoolMemberListView,
                                di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<SchoolMemberListView, SchoolMember>(context, arguments, view, di, lifecycleOwner) {

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class SchoolMemberListSortOption(val sortOrder: SortOrder, context: Any)
        : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { SchoolMemberListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.schoolDao.personHasPermissionWithSchool(account?.personUid ?: 0L,
                arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L, Role.PERMISSION_SCHOOL_UPDATE)
    }

    private fun updateListOnView() {

        val schoolRole = if (arguments.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF)) {
            SchoolMember.SCHOOL_ROLE_TEACHER
        } else {
            SchoolMember.SCHOOL_ROLE_STUDENT
        }

        val schoolUid: Long = if (arguments.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF)) {
            arguments.get(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF)?.toLong()?:0L
        } else {
            arguments.get(UstadView.ARG_SCHOOLMEMBER_FILTER_STUDENTS)?.toLong()?:0L
        }

        view.list = when(currentSortOrder) {
            SortOrder.ORDER_NAME_ASC ->
                repo.schoolMemberDao.findAllActiveMembersAscBySchoolAndRoleUidAsc(
                    schoolUid, schoolRole, mSearchQuery)
            SortOrder.ORDER_NAME_DSC ->
                repo.schoolMemberDao.findAllActiveMembersDescBySchoolAndRoleUidAsc(
                    schoolUid, schoolRole, mSearchQuery)
        }
    }

    fun handleEnrolMember(schoolUid: Long, personUid: Long, role:Int){

        GlobalScope.launch {
            db.enrollPersonToSchool(getSystemTimeInMillis(), schoolUid, personUid, role)
//            db.schoolMemberDao.enrollPersonToSchool(UMCalendarUtil.getDateInMilliPlusDays(0),
//                    0, schoolUid, personUid, role)
        }
    }

    override fun handleClickEntry(entry: SchoolMember) {

        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.schoolMemberPersonUid.toString()), context)
        }

    }

    override fun handleClickCreateNewFab() {
        view.addMember()
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? SchoolMemberListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}