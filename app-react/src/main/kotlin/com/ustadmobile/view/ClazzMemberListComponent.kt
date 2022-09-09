package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzMemberListPresenter
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.view.ext.renderListItemWithPersonAttendanceAndPendingRequests
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css

class ClazzMemberListComponent(mProps: UmProps):UstadListComponent<PersonWithClazzEnrolmentDetails, PersonWithClazzEnrolmentDetails>(mProps),
    ClazzMemberListView{

    private var mPresenter: ClazzMemberListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    override val listPresenter: UstadListPresenter<*, in PersonWithClazzEnrolmentDetails>?
        get() = mPresenter

    private var addNewStudentText = getString(MessageID.add_a_student)

    private var teacherSectionHeaderText = getString(MessageID.teachers_literal)

    private var studentSectionHeaderText = getString(MessageID.students)

    private var students: List<PersonWithClazzEnrolmentDetails> = listOf()

    private val studentListObserver = ObserverFnWrapper<List<PersonWithClazzEnrolmentDetails>>{
        setState {
            students = it
        }
    }

    override var studentList: DataSourceFactory<Int, PersonWithClazzEnrolmentDetails>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(studentListObserver)
            liveData?.observe(this, studentListObserver)
        }

    private var pendingStudents: List<PersonWithClazzEnrolmentDetails> = listOf()

    private val pendingStudentObserver = ObserverFnWrapper<List<PersonWithClazzEnrolmentDetails>>{
        setState {
            pendingStudents = it
        }
    }

    override var pendingStudentList: DataSourceFactory<Int, PersonWithClazzEnrolmentDetails>? = null
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

    override var termMap: Map<String, String>? = null
        set(value) {
            field = value
           setState {
               addNewEntryText = value?.get(TerminologyKeys.ADD_TEACHER_KEY).toString()
               teacherSectionHeaderText = value?.get(TerminologyKeys.TEACHERS_KEY).toString()
               addNewStudentText = value?.get(TerminologyKeys.ADD_STUDENT_KEY).toString()
               studentSectionHeaderText = value?.get(TerminologyKeys.STUDENTS_KEY).toString()
           }
        }

    private var filterByClazzUid: Long = 0

    override fun onCreateView() {
        super.onCreateView()
        showEmptyState = false
        addNewEntryText = getString(MessageID.add_a_teacher)
        filterByClazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
        mPresenter = ClazzMemberListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListHeaderView() {
        renderListSectionTitle(teacherSectionHeaderText)
    }

    override fun RBuilder.renderListItem(item: PersonWithClazzEnrolmentDetails) {
        renderListItemWithPersonAttendanceAndPendingRequests(
            item.personUid, item.fullName(),
            attendance = item.attendance,
            attendanceLabel = getString(MessageID.x_percent_attended),
            student = false
        )
    }

    override fun RBuilder.renderListFooterView() {
        umGridContainer(rowSpacing = GridSpacing.spacing2) {
            createMemberList(students,
                studentSectionHeaderText,
                ClazzEnrolment.ROLE_STUDENT,
                addNewStudentText,
                addStudentVisible)

            if(pendingStudents.isNotEmpty()){
                createMemberList(pendingStudents,
                    getString(MessageID.pending_requests),
                    ClazzEnrolment.ROLE_STUDENT_PENDING,
                    showCreateNewItem = addTeacherVisible,
                    pending = true)
            }
        }
    }

    override fun handleClickEntry(entry: PersonWithClazzEnrolmentDetails) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun handleClickAddNewEntry() {
        mPresenter?.handlePickNewMemberClicked(ClazzEnrolment.ROLE_TEACHER)
    }

    private fun RBuilder.createMemberList(
        members: List<PersonWithClazzEnrolmentDetails>,
        sectionTitle: String,
        role: Int,
        createNewLabel: String = "",
        showCreateNewItem: Boolean = true,
        pending: Boolean = false){

        umGridContainer(rowSpacing = GridSpacing.spacing1) {
            css(StyleManager.defaultDoubleMarginTop)
            umItem(GridSize.cells12){
                renderListSectionTitle(sectionTitle)
            }

            umItem(GridSize.cells12){
                val createNewItem = CreateNewItem(showCreateNewItem, createNewLabel){
                    mPresenter?.handlePickNewMemberClicked(role)
                }
                mPresenter?.let { presenter ->
                    renderMembers(presenter,members, createNewItem, pending){ entry ->
                        handleClickEntry(entry)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}

interface MemberListProps: SimpleListProps<PersonWithClazzEnrolmentDetails>{
    var pending: Boolean
}

class MembersListComponent(mProps: MemberListProps):
    UstadSimpleList<MemberListProps>(mProps){

    override fun RBuilder.renderListItem(item: PersonWithClazzEnrolmentDetails, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            val presenter = props.presenter as ClazzMemberListPresenter
            renderListItemWithPersonAttendanceAndPendingRequests(
                item.personUid, item.fullName(),
                props.pending, item.attendance,
                getString(MessageID.x_percent_attended),
                onClickAccept = {
                    presenter.handleClickPendingRequest(item, true)
                }, onClickDecline = {
                    presenter.handleClickPendingRequest(item, false)
                })
        }
    }
}

fun RBuilder.renderMembers(
    presenter: ClazzMemberListPresenter,
    members: List<PersonWithClazzEnrolmentDetails>,
    createNewItem: CreateNewItem = CreateNewItem(),
    pending: Boolean = false,
    onEntryClicked: ((PersonWithClazzEnrolmentDetails) -> Unit)? = null
) = child(MembersListComponent::class) {
    attrs.entries = members
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.presenter = presenter
    attrs.pending = pending
}


