package com.ustadmobile.view

import com.ustadmobile.core.controller.SchoolMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.*
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.view.ext.renderListItemWithPersonAttendanceAndPendingRequests
import com.ustadmobile.view.ext.renderListSectionTitle
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState

class SchoolMemberListComponent(mProps: UmProps): UstadListComponent<SchoolMember, SchoolMemberWithPerson>(mProps),
    SchoolMemberListView {

    private var mPresenter: SchoolMemberListPresenter? = null

    private lateinit var addPersonKeyName: String

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolMemberDao

    override val listPresenter: UstadListPresenter<*, in SchoolMemberWithPerson>?
        get() = mPresenter

    var roleStudent: Boolean = false

    private var addNewStringId: Int = 0

    private var filterBySchoolUid: Long = 0

    override fun addMember() {
        val args = if (addPersonKeyName == "Person_" + Role.ROLE_SCHOOL_STAFF_UID.toString()) {
            mapOf(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString())
        } else {
            mapOf(
                PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString(),
                UstadView.ARG_CODE_TABLE to School.TABLE_ID.toString())
        }
        mPresenter?.handleClickAddNewItem(args, addPersonKeyName)
    }

    private var pendingStudents: List<SchoolMemberWithPerson> = listOf()

    private val observer = ObserverFnWrapper<List<SchoolMemberWithPerson>>{
        if(it.isNullOrEmpty()) return@ObserverFnWrapper
        setState {
            pendingStudents = it
        }
    }

    override var pendingStudentList: DoorDataSourceFactory<Int, SchoolMemberWithPerson>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
        }

    override fun onCreateView() {
        super.onCreateView()
        val filterByRole = arguments[UstadView.ARG_FILTER_BY_ROLE]?.toInt() ?: 0
        addPersonKeyName = "Person_$filterByRole"
        filterBySchoolUid = arguments[UstadView.ARG_FILTER_BY_SCHOOLUID]?.toLong() ?: 0
        roleStudent = filterByRole != Role.ROLE_SCHOOL_STAFF_UID
        showEmptyState = true
        addNewStringId = if (filterByRole == Role.ROLE_SCHOOL_STAFF_UID) {
            MessageID.teacher
        } else {
            MessageID.student
        }
        addNewEntryText = "${getString(MessageID.add_new)} $addNewStringId"

        fabManager?.visible = true
        fabManager?.icon = "add"
        fabManager?.text = getString(addNewStringId)

        savedStateHandle?.observeResult(this,
            Person.serializer(), addPersonKeyName) {
            val memberAdded = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleEnrolMember(filterBySchoolUid, memberAdded.personUid, filterByRole)
        }

        mPresenter = SchoolMemberListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.renderListItem(item: SchoolMemberWithPerson) {
        renderListItemWithPersonAttendanceAndPendingRequests(item.person?.personUid ?: 0,
            item.person?.fullName() ?: "",
            student = roleStudent)
    }

    override fun handleClickEntry(entry: SchoolMemberWithPerson) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun handleClickCreateNewEntry() {
        mPresenter?.handleClickAddNewItem(arguments,
            addPersonKeyName)
    }

    override fun RBuilder.renderListFooterView() {
        if(roleStudent){

            if(pendingStudents.isNotEmpty()){
                renderListSectionTitle(getString(MessageID.pending_requests))
            }

            child(MembersListComponent::class){
                attrs.entries = pendingStudents
                attrs.onEntryClicked = { student ->
                    mPresenter?.handleClickEntry(student)
                }
                mPresenter?.let {
                    attrs.presenter = it
                }
                attrs.createNewItem = CreateNewItem()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    class MembersListComponent(mProps: SimpleListProps<SchoolMemberWithPerson>):
        UstadSimpleList<SimpleListProps<SchoolMemberWithPerson>>(mProps){

        override fun RBuilder.renderListItem(item: SchoolMemberWithPerson, onClick: (Event) -> Unit) {
            val presenter = props.presenter as SchoolMemberListPresenter
            renderListItemWithPersonAttendanceAndPendingRequests(item.person?.personUid ?: 0,
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