package com.ustadmobile.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.controller.SchoolMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.ext.observeResult
import com.ustadmobile.view.ext.createListSectionTitle
import com.ustadmobile.view.ext.createListItemWithPersonAttendanceAndPendingRequests
import react.RBuilder
import react.RProps
import react.setState

class SchoolMemberListComponent(mProps: RProps): UstadListComponent<SchoolMember, SchoolMemberWithPerson>(mProps),
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

    private var filterByRole: Int = 0

    override val viewName: String
        get() = SchoolMemberListView.VIEW_NAME

    override fun addMember() {
        val args = if (addPersonKeyName == "Person_" + Role.ROLE_SCHOOL_STAFF_UID.toString()) {
            mapOf(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString())
        } else {
            mapOf(
                PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString(),
                UstadView.ARG_CODE_TABLE to School.TABLE_ID.toString())
        }
        mPresenter?.handleAddMemberClicked(args, addPersonKeyName)
    }

    private var pendingStudents: List<SchoolMemberWithPerson>? = null

    private val observer = ObserverFnWrapper<List<SchoolMemberWithPerson>>{
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
        addPersonKeyName = "Person_${arguments[UstadView.ARG_FILTER_BY_ROLE]}"
        filterByRole = arguments[UstadView.ARG_FILTER_BY_ROLE]?.toInt() ?: 0
        filterBySchoolUid = arguments[UstadView.ARG_FILTER_BY_SCHOOLUID]?.toLong() ?: 0
        roleStudent = filterByRole != Role.ROLE_SCHOOL_STAFF_UID

        addNewStringId = if (filterByRole == Role.ROLE_SCHOOL_STAFF_UID) {
            MessageID.teacher
        } else {
            MessageID.student
        }

        showCreateNewItem = true
        createNewText = "${getString(MessageID.add_new)} $addNewStringId"

        fabManager?.visible = true
        fabManager?.icon = "add"
        fabManager?.text = getString(addNewStringId)

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
            Person.serializer(), addPersonKeyName) {
            val memberAdded = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleEnrolMember(filterBySchoolUid, memberAdded.personUid,
                arguments[UstadView.ARG_FILTER_BY_ROLE]?.toInt() ?: 0)
        }

        mPresenter = SchoolMemberListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.renderListItem(item: SchoolMemberWithPerson) {
        createListItemWithPersonAttendanceAndPendingRequests(item.person?.personUid ?: 0,
            item.person?.fullName() ?: "", student = roleStudent)
    }

    override fun handleClickEntry(entry: SchoolMemberWithPerson) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun RBuilder.renderFooterView() {
        if(roleStudent){
            pendingStudents?.let { students ->

                createListSectionTitle(getString(MessageID.pending_requests))

                child(MembersListComponent::class){
                    attrs.entries = students
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        pendingStudents = null
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