package com.ustadmobile.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.controller.ClazzMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.view.ext.*
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class ClazzMemberListComponent(mProps: RProps):UstadListComponent<PersonWithClazzEnrolmentDetails, PersonWithClazzEnrolmentDetails>(mProps),
    ClazzMemberListView{

    private var mPresenter: ClazzMemberListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    override val listPresenter: UstadListPresenter<*, in PersonWithClazzEnrolmentDetails>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzMemberListView.VIEW_NAME

    private lateinit var students: List<PersonWithClazzEnrolmentDetails>

    private val studentListObserver = ObserverFnWrapper<List<PersonWithClazzEnrolmentDetails>>{
        setState {
            students = it
        }
    }

    override var studentList: DoorDataSourceFactory<Int, PersonWithClazzEnrolmentDetails>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(studentListObserver)
            liveData?.observe(this, studentListObserver)
        }

    private lateinit var pendingStudents: List<PersonWithClazzEnrolmentDetails>

    private val pendingStudentObserver = ObserverFnWrapper<List<PersonWithClazzEnrolmentDetails>>{
        setState {
            pendingStudents = it
        }
    }

    override var pendingStudentList: DoorDataSourceFactory<Int, PersonWithClazzEnrolmentDetails>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(pendingStudentObserver)
            liveData?.observe(this, pendingStudentObserver)
        }

    override var addTeacherVisible: Boolean = false
        set(value) {
            field = value
            showCreateNewItem = value
        }

    override var addStudentVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private var filterByClazzUid: Long = 0

    override fun onCreate() {
        super.onCreate()
        createNewTextId = MessageID.add_a_teacher
        filterByClazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
        mPresenter = ClazzMemberListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderHeaderView() {
        createListSectionTitle(getString(MessageID.teachers_literal))
    }

    override fun RBuilder.renderListItem(item: PersonWithClazzEnrolmentDetails) {
        createListItemWithPersonAttendanceAndPendingRequests(item.personUid, item.fullName(), attendance = item.attendance,
            attendanceLabel = getString(MessageID.x_percent_attended), student = false)
    }

    override fun RBuilder.renderFooterView() {
        if(::students.isInitialized && students.isNotEmpty()){
           createMemberList(students, getString(MessageID.students),
               ClazzEnrolment.ROLE_STUDENT, MessageID.add_a_student)
        }


        if(::pendingStudents.isInitialized && pendingStudents.isNotEmpty()){
            createMemberList(pendingStudents, getString(MessageID.pending_requests),
                ClazzEnrolment.ROLE_STUDENT_PENDING, pending = true)
        }
    }

    override fun handleClickEntry(entry: PersonWithClazzEnrolmentDetails) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun handleClickCreateNewEntry() {
        navigateToPickNewMember(ClazzEnrolment.ROLE_TEACHER)
    }

    private fun RBuilder.createMemberList(members: List<PersonWithClazzEnrolmentDetails>,
                                          sectionTitle: String, role: Int,
                                          createNewLabel: Int = 0,
                                          pending: Boolean = false){
        styledDiv {
            css(defaultMarginTop)
            createListSectionTitle(sectionTitle)
        }

        styledDiv {
            val createNewItem = CreateNewItem(createNewLabel != 0, createNewLabel){
                navigateToPickNewMember(role)
            }
            mPresenter?.let { presenter ->
                renderMembers(presenter,members, createNewItem, pending){ entry ->
                    if(createNewLabel != 0){
                        handleClickEntry(entry)
                    }
                }
            }
        }
    }

    private fun navigateToPickNewMember(role: Int) {
        val args = mutableMapOf(
            PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ to filterByClazzUid.toString(),
            UstadView.ARG_FILTER_BY_ENROLMENT_ROLE to role.toString(),
            UstadView.ARG_CLAZZUID to (arguments[UstadView.ARG_CLAZZUID] ?: "-1"),
            UstadView.ARG_GO_TO_COMPLETE to ClazzEnrolmentEditView.VIEW_NAME,
            UstadView.ARG_POPUPTO_ON_FINISH to ClazzMemberListView.VIEW_NAME,
            ClazzMemberListView.ARG_HIDE_CLAZZES to true.toString(),
            UstadView.ARG_SAVE_TO_DB to true.toString()).also {

            if(role == ClazzEnrolment.ROLE_STUDENT){
                it[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            }
        }

        navigateToPickEntityFromList(
            PersonListView.VIEW_NAME, args,
            overwriteDestination = true,
            navOptions = DEFAULT_NAV_OPTION(ClazzEnrolment.serializer())
        )
    }
}

interface MemberListProps: ListProps<PersonWithClazzEnrolmentDetails>{
    var pending: Boolean
}

class MembersListComponent(mProps: MemberListProps):
    UstadSimpleList<MemberListProps>(mProps){

    override fun RBuilder.renderListItem(item: PersonWithClazzEnrolmentDetails) {
        val presenter = props.presenter as ClazzMemberListPresenter
        createListItemWithPersonAttendanceAndPendingRequests(item.personUid, item.fullName(),props.pending,item.attendance,
            getString(MessageID.x_percent_attended),
            onClickAccept = {
                presenter.handleClickPendingRequest(item, true)
            }, onClickDecline = {
                presenter.handleClickPendingRequest(item, false)
            })
    }
}

fun RBuilder.renderMembers(presenter: ClazzMemberListPresenter,
                           members: List<PersonWithClazzEnrolmentDetails>,
                           createNewItem: CreateNewItem = CreateNewItem(),
                           pending: Boolean = false,
                           onEntryClicked: ((PersonWithClazzEnrolmentDetails) -> Unit)? = null) = child(MembersListComponent::class) {
    attrs.entries = members
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.presenter = presenter
    attrs.pending = pending
}


