package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MIconButtonSize
import com.ccfraser.muirwik.components.button.mIconButton
import com.ustadmobile.core.controller.ClazzMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.clazzItemSecondaryDesc
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.display
import kotlinx.css.paddingBottom
import kotlinx.css.paddingTop
import kotlinx.css.px
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

    override var studentList: DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>? = null
        set(value) {
            field = value
            GlobalScope.launch {
                val data = value?.getData(0, 1000)
                setState {
                    if(data != null){
                        students = data
                    }
                }
            }
        }

    private lateinit var pendingStudents: List<PersonWithClazzEnrolmentDetails>

    override var pendingStudentList: DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>? = null
        set(value) {
            field = value
            GlobalScope.launch {
                val data = value?.getData(0, 1000)
                setState {
                    if(data != null){
                        pendingStudents = data
                    }
                }
            }
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
        mPresenter?.let { presenter ->
            createMemberListItem(presenter, item, getString(MessageID.x_percent_attended))
        }
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
            ClazzEnrolment::class, PersonListView.VIEW_NAME, args,
            overwriteDestination = true)
    }
}


private fun RBuilder.createMemberListItem(presenter: ClazzMemberListPresenter,
                                          item: PersonWithClazzEnrolmentDetails,
                                          percentageAttended: String,
                                          pending: Boolean = false){
    umGridContainer(MGridSpacing.spacing5) {
        css{
            paddingTop = 4.px
            paddingBottom = 4.px
        }
        umItem(MGridSize.cells3, MGridSize.cells2){
            umProfileAvatar(item.personUid, "person")
        }

        umItem(MGridSize.cells9, MGridSize.cells10){
            umItem(MGridSize.cells12){
                mTypography("${item.firstNames} ${item.lastName}",
                    variant = MTypographyVariant.h6,
                    color = MTypographyColor.textPrimary){
                    css (alignTextToStart)
                }
            }

            umGridContainer{
                umItem(MGridSize.cells1){
                    circleIndicator(item.attendance)
                }

                umItem(MGridSize.cells8){
                    val attendancesPercentage = percentageAttended.format(item.attendance * 100)
                    mTypography(attendancesPercentage,
                        color = MTypographyColor.textPrimary
                    ){
                        css{
                            +alignTextToStart
                            +clazzItemSecondaryDesc
                        }
                    }

                }

                umItem(MGridSize.cells3){
                    css{
                        display = displayProperty(pending, true)
                    }
                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells4){
                            mIconButton("check",
                                onClick = {
                                    presenter.handleClickPendingRequest(item, true)
                                },
                                className = "${StyleManager.name}-successClass",
                                size = MIconButtonSize.small)
                        }

                        umItem(MGridSize.cells4){
                            mIconButton("close",
                                onClick = {
                                    presenter.handleClickPendingRequest(item, false)
                                },
                                className = "${StyleManager.name}-errorClass",
                                size = MIconButtonSize.small)
                        }
                    }
                }
            }
        }
    }
}

interface MemberListProps: ListProps<PersonWithClazzEnrolmentDetails>{
    var pending: Boolean
}

class MembersListComponent(mProps: MemberListProps):
    UstadSimpleList<MemberListProps>(mProps){

    override fun RBuilder.renderListItem(item: PersonWithClazzEnrolmentDetails) {
        createMemberListItem(
            props.presenter as ClazzMemberListPresenter,
            item, getString(MessageID.x_percent_attended),
            props.pending)
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


