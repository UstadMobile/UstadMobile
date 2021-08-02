package com.ustadmobile.view

import androidx.paging.DataSource
import com.ustadmobile.core.controller.SchoolMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.SchoolMemberWithPerson
import com.ustadmobile.view.ext.createListSectionTitle
import com.ustadmobile.view.ext.createListItemWithPersonAttendanceAndPendingRequests
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.RBuilder
import react.RProps
import react.setState

class SchoolMemberListComponent(mProps: RProps): UstadListComponent<SchoolMember, SchoolMemberWithPerson>(mProps),
    SchoolMemberListView {

    private var mPresenter: SchoolMemberListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolMemberDao

    override val listPresenter: UstadListPresenter<*, in SchoolMemberWithPerson>?
        get() = mPresenter

    var roleStudent: Boolean = false

    override fun RBuilder.renderListItem(item: SchoolMemberWithPerson) {
        createListItemWithPersonAttendanceAndPendingRequests(item.person?.personUid ?: 0,
            item.person?.fullName() ?: "", student = roleStudent)
    }

    override fun handleClickEntry(entry: SchoolMemberWithPerson) {
        mPresenter?.handleClickEntry(entry)
    }

    override val viewName: String
        get() = SchoolMemberListView.VIEW_NAME

    override fun addMember() {
        TODO("Not yet implemented")
    }

    private var pendingStudents: List<SchoolMemberWithPerson>? = null

    override var pendingStudentList: DataSource.Factory<Int, SchoolMemberWithPerson>? = null
        set(value) {
            field = value
            GlobalScope.launch {
                val data = value?.getData(0, 1000)
                if(data != null){
                    setState {
                        pendingStudents = data
                    }
                }
            }
        }

    override fun onCreate() {
        super.onCreate()

        val filterByRole = arguments[UstadView.ARG_FILTER_BY_ROLE]?.toInt() ?: 0
        roleStudent = filterByRole != Role.ROLE_SCHOOL_STAFF_UID

        val addNewStringId = if (filterByRole == Role.ROLE_SCHOOL_STAFF_UID) {
            MessageID.teacher
        } else {
            MessageID.student
        }
        fabManager?.visible = true
        createNewTextId = addNewStringId
        fabManager?.icon = "add"
        fabManager?.text = getString(addNewStringId)

        mPresenter = SchoolMemberListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderFooterView() {
        if(roleStudent){
            pendingStudents?.let { students ->

                createListSectionTitle(getString(MessageID.pending_requests))

                child(MembersListComponent::class){
                    attrs.entries = students
                    mPresenter?.let {
                        attrs.presenter = it
                    }
                    attrs.onEntryClicked = { student ->
                        mPresenter?.handleClickEntry(student)
                    }
                    attrs.createNewItem = CreateNewItem()
                }
            }
        }
    }

    class MembersListComponent(mProps: ListProps<SchoolMemberWithPerson>):
        UstadSimpleList<ListProps<SchoolMemberWithPerson>>(mProps){

        override fun RBuilder.renderListItem(item: SchoolMemberWithPerson) {
            val presenter = props.presenter as SchoolMemberListPresenter
            createListItemWithPersonAttendanceAndPendingRequests(item.person?.personUid ?: 0,
                item.person?.fullName() ?: "",true,
                onClickAccept = {
                    presenter.handleClickPendingRequest(item, true)
                },
                onClickDecline = {
                    presenter.handleClickPendingRequest(item, false)
                })
        }
    }
}