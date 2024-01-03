package com.ustadmobile.view.clazzassignment.detailoverview

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import kotlinx.datetime.TimeZone
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.useState
import react.ReactNode
import react.create
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState as UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.mui.components.UstadCourseAssignmentMarkListItem
import com.ustadmobile.wrappers.quill.ReactQuill
import web.cssom.Height
import web.cssom.pct
import js.core.jso
import mui.icons.material.Done as DoneIcon
import mui.icons.material.DoneAll as DoneAllIcon
import mui.icons.material.EventAvailable as EventAvailableIcon
import mui.icons.material.Add as AddIcon
import mui.icons.material.InsertDriveFile as InsertDriveFileIcon
import mui.icons.material.Groups as GroupsIcon
import mui.icons.material.Person as PersonIcon
import mui.icons.material.Group as GroupIcon
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.view.clazzassignment.AssignmentCommentTextFieldListItem
import com.ustadmobile.view.clazzassignment.UstadCommentListItem
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import kotlinx.coroutines.Dispatchers
import mui.system.sx
import react.useRequiredContext
import web.cssom.Contain
import web.cssom.Overflow
import web.cssom.TextAlign

val ASSIGNMENT_STATUS_MAP = mapOf(
    CourseAssignmentSubmission.NOT_SUBMITTED to DoneIcon,
    CourseAssignmentSubmission.SUBMITTED to DoneIcon,
    CourseAssignmentSubmission.MARKED to DoneAllIcon,
)

external interface ClazzAssignmentDetailOverviewScreenProps : Props {

    var uiState: ClazzAssignmentDetailOverviewUiState

    var onChangeSubmissionText: (String) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onChangeCourseComment: (String) -> Unit

    var onChangePrivateComment: (String) -> Unit

    var onClickSubmitCourseComment: () -> Unit

    var onClickSubmitPrivateComment: () -> Unit

    var onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickAddFileSubmission: () -> Unit

    var onClickSubmitSubmission: () -> Unit

    var onClickCourseGroupSet: () -> Unit

}

private val ClazzAssignmentDetailOverviewScreenComponent2 = FC<ClazzAssignmentDetailOverviewScreenProps> { props ->

    val strings = useStringProvider()

    val theme by useRequiredContext(ThemeContext)

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val policyMessageId = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS
        .firstOrNull { it.value == props.uiState.assignment?.caSubmissionPolicy }?.stringResource
        ?: MR.strings.submit_all_at_once_submission_policy


    val caFileType = strings[
        SubmissionConstants.FILE_TYPE_MAP[
            props.uiState.assignment?.caFileType] ?: MR.strings.document
    ]

    val muiAppState = useMuiAppState()

    val courseCommentInfiniteQueryResult = usePagingSource(
        props.uiState.courseComments, true, 50
    )

    val privateCommentIninfiteQueryResult = usePagingSource(
        props.uiState.privateComments, true, 50
    )

    val courseTerminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            //Header section - description, deadline, etc
            item {
                Stack.create {
                    UstadRawHtml {
                        html = props.uiState.courseBlock?.cbDescription ?: ""
                    }

                    if (props.uiState.cbDeadlineDateVisible) {
                        UstadDetailField {
                            valueText = ReactNode(
                                "$formattedDateTime (${TimeZone.currentSystemDefault().id})"
                            )
                            labelText = strings[MR.strings.deadline]
                            icon = EventAvailableIcon.create()
                            onClick = { }
                        }
                    }

                    UstadDetailField2 {
                        valueContent = ReactNode(strings[policyMessageId])
                        labelContent = ReactNode(strings[MR.strings.submission_policy])
                        leadingContent = (ASSIGNMENT_STATUS_MAP[
                            props.uiState.assignment?.caSubmissionPolicy] ?: DoneIcon).create()
                    }

                    props.uiState.courseGroupSet?.also { groupSet ->
                        UstadDetailField2 {
                            valueContent = ReactNode(groupSet.cgsName ?: "")
                            labelContent = ReactNode(strings[MR.strings.group_submission])
                            leadingContent = GroupsIcon.create()
                            onClick = {
                                props.onClickCourseGroupSet()
                            }
                        }
                    }

                    UstadDetailField2 {
                        valueContent = ReactNode(props.uiState.assignment?.let {
                            if(it.caMarkingType == ClazzAssignment.MARKED_BY_COURSE_LEADER) {
                                courseTerminologyResource(
                                    terminologyEntries = courseTerminologyEntries,
                                    stringProvider = strings,
                                    stringResource = MR.strings.teacher,
                                )
                            }else {
                                strings[MR.strings.peers]
                            }
                        } ?: "")
                        labelContent = ReactNode(strings[MR.strings.marked_by])
                        leadingContent = when(props.uiState.assignment?.caMarkingType) {
                            ClazzAssignment.MARKED_BY_COURSE_LEADER -> PersonIcon.create()
                            ClazzAssignment.MARKED_BY_PEERS -> GroupIcon.create()
                            else -> null
                        }
                    }

                    UstadAssignmentSubmissionHeader {
                        uiState = props.uiState
                    }
                }
            }


            if (props.uiState.unassignedErrorVisible) {
                item("unassigned_error") {
                    ListItem.create {
                        ListItemText {
                            primary = ReactNode(props.uiState.unassignedError ?: "")
                        }
                    }
                }
            }

            //submission section
            if(props.uiState.activeUserIsSubmitter) {
                item {
                    UstadDetailHeader.create {
                        val suffix = if(props.uiState.isGroupSubmission) {
                            "(${strings.format(MR.strings.group_number, props.uiState.submitterUid.toString())})"
                        }else {
                            ""
                        }
                        header = ReactNode("${strings[MR.strings.your_submission]} $suffix")
                    }
                }

                if(props.uiState.submissionTextFieldVisible) {
                    item {
                        Stack.create {
                            direction = responsive(StackDirection.column)

                            ReactQuill {
                                id = "assignment_text"
                                value = props.uiState.latestSubmission?.casText ?: ""
                                onChange = props.onChangeSubmissionText
                                readOnly = !props.uiState.activeUserCanSubmit
                            }

                            props.uiState.currentSubmissionLength?.also { submissionLength ->
                                val limitTypeMessageId = if(props.uiState.assignment?.caTextLimitType == ClazzAssignment.TEXT_CHAR_LIMIT) {
                                    MR.strings.characters
                                }else {
                                    MR.strings.words
                                }
                                + "${strings[limitTypeMessageId]}: $submissionLength / "
                                + "${props.uiState.assignment?.caTextLimit} "
                            }
                        }

                    }
                }

                if(props.uiState.addFileVisible) {
                    item {
                        ListItem.create {
                            ListItemButton {
                                id = "add_file"
                                onClick = {
                                    props.onClickAddFileSubmission()
                                }
                                ListItemIcon {
                                    AddIcon { }
                                }

                                ListItemText {
                                    primary =
                                        ReactNode(strings[MR.strings.add_file].uppercase())
                                    secondary = ReactNode(
                                        "${strings[MR.strings.file_type_chosen]} $caFileType " +
                                            strings[MR.strings.max_number_of_files]
                                                .replace(
                                                    "%1\$s",
                                                    (props.uiState.assignment?.caNumberOfFiles
                                                        ?: 0)
                                                        .toString()
                                                )
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    list = props.uiState.latestSubmissionAttachments ?: emptyList(),
                    key = { "sa_${it.casaUid}"}
                ) { submissionAttachment ->
                    ListItem.create {
                        ListItemButton {
                            ListItemIcon {
                                InsertDriveFileIcon { }
                            }

                            ListItemText {
                                primary = ReactNode(submissionAttachment.casaFileName ?: "")
                            }
                        }
                    }
                }

                if (props.uiState.submitSubmissionButtonVisible) {
                    item(key = "item_submit_button") {
                        Button.create {
                            onClick = { props.onClickSubmitSubmission() }
                            id = "submit_button"
                            disabled = !props.uiState.fieldsEnabled
                            variant = ButtonVariant.contained
                            fullWidth = true

                            +strings[MR.strings.submit].uppercase()
                        }
                    }
                }

                props.uiState.submissionError?.also { submissionError ->
                    item(key = "submit_error") {
                        Typography.create {
                            sx {
                                color = theme.palette.error.main
                                textAlign = TextAlign.center
                            }

                            + submissionError
                        }
                    }
                }

                //List of grades awarded
                item {
                    UstadDetailHeader.create {
                        header = ReactNode(strings[MR.strings.grades_scoring])
                    }
                }

                item {
                    ListItem.create {
                        UstadListFilterChipsHeader {
                            filterOptions = props.uiState.gradeFilterChips
                            selectedChipId = props.uiState.selectedChipId
                            enabled = props.uiState.fieldsEnabled
                            onClickFilterChip = { props.onClickFilterChip(it) }
                        }
                    }
                }

                items(
                    list = props.uiState.visibleMarks,
                    key = { "mark_${it.courseAssignmentMark?.camUid}"}
                ) { markItem ->
                    UstadCourseAssignmentMarkListItem.create {
                        uiState = UstadCourseAssignmentMarkListItemUiState(
                            mark = markItem,
                        )
                    }
                }
            }

            if(props.uiState.showClassComments) {
                //Course comments
                item {
                    ListItem.create {
                        ListItemText {
                            primary = ReactNode(strings[MR.strings.class_comments])
                        }
                    }
                }

                item {
                    AssignmentCommentTextFieldListItem.create {
                        onChange = props.onChangeCourseComment
                        label = ReactNode(strings[MR.strings.add_class_comment])
                        value = props.uiState.newCourseCommentText
                        activeUserPersonName = props.uiState.activeUserPersonName
                        activeUserPictureUri = props.uiState.activeUserPictureUri
                        textFieldId = "course_comment_textfield"
                        onClickSubmit = props.onClickSubmitCourseComment
                    }
                }

                infiniteQueryPagingItems(
                    items = courseCommentInfiniteQueryResult,
                    key = { "cc_${it.comment.commentsUid}" }
                ) { comment ->
                    UstadCommentListItem.create {
                        commentsAndName = comment
                    }
                }
            }


            //Private comments
            if(props.uiState.showPrivateComments) {
                item {
                    ListItem.create {
                        ListItemText {
                            primary = ReactNode(strings[MR.strings.private_comments])
                        }
                    }
                }

                item {
                    AssignmentCommentTextFieldListItem.create {
                        onChange = props.onChangePrivateComment
                        label = ReactNode(strings[MR.strings.add_private_comment])
                        value = props.uiState.newPrivateCommentText
                        activeUserPersonName = props.uiState.activeUserPersonName
                        activeUserPictureUri = props.uiState.activeUserPictureUri
                        textFieldId = "private_comment_textfield"
                        onClickSubmit = props.onClickSubmitPrivateComment
                    }
                }

                infiniteQueryPagingItems(
                    items = privateCommentIninfiteQueryResult,
                    key = { "pc_${it.comment.commentsUid}" }
                ) { comment ->
                    UstadCommentListItem.create {
                        commentsAndName = comment
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }

}

val ClazzAssignmentDetailOverviewScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(
        ClazzAssignmentDetailOverviewUiState(), Dispatchers.Main.immediate
    )

    ClazzAssignmentDetailOverviewScreenComponent2 {
        uiState = uiStateVal
        onChangeSubmissionText = viewModel::onChangeSubmissionText
        onChangeCourseComment = viewModel::onChangeCourseCommentText
        onChangePrivateComment = viewModel::onChangePrivateCommentText
        onClickSubmitCourseComment = viewModel::onClickSubmitCourseComment
        onClickSubmitPrivateComment = viewModel::onClickSubmitPrivateComment
        onClickSubmitSubmission = viewModel::onClickSubmit
        onClickFilterChip = viewModel::onClickMarksFilterChip
        onClickCourseGroupSet = viewModel::onClickCourseGroupSet
    }
}

val ClazzAssignmentDetailOverviewScreenPreview = FC<Props> {

    var uiStateVar by useState {
        ClazzAssignmentDetailOverviewUiState(
            assignment = ClazzAssignment().apply {
                caRequireTextSubmission = true
            },
            courseBlock = CourseBlock().apply {
                cbDeadlineDate = 1685509200000L
                cbDescription = "Complete your assignment or <b>else</b>"
            },
            submitterUid = 42L,
            addFileVisible = true,
            latestSubmission = CourseAssignmentSubmission().apply {
                casText = ""
            },
            latestSubmissionAttachments = listOf(
                CourseAssignmentSubmissionAttachment().apply {
                    casaUid = 1L
                    casaFileName = "File.pdf"
                },
            ),
            markList = listOf(
                CourseAssignmentMarkAndMarkerName(
                    courseAssignmentMark = CourseAssignmentMark().apply {
                        camMarkerSubmitterUid = 2
                        camMarkerComment = "Comment"
                        camMark = 8.1f
                        camPenalty = 0.9f
                        camMaxMark = 10f
                        camLct = systemTimeInMillis()
                    },
                    markerFirstNames = "John",
                    markerLastName = "Smith",
                )
            ),
            courseComments = {
                ListPagingSource(listOf(
                    CommentsAndName().apply {
                        comment = Comments().apply {
                            commentsUid = 1
                            commentsText = "This is a very difficult assignment."
                        }
                        firstNames = "Bob"
                        lastName = "Dylan"
                    }
                ))
            },
            privateComments = {
                ListPagingSource(
                    listOf(
                        CommentsAndName().apply {
                            comment = Comments().apply {
                                commentsUid = 2
                                commentsText = "Can I please have extension? My rabbit ate my homework."
                            }
                            firstNames = "Bob"
                            lastName = "Dylan"
                        }
                    ),
                )
            },
        )
    }

    ClazzAssignmentDetailOverviewScreenComponent2 {
        uiState = uiStateVar
        onClickDeleteSubmission = {}
        onChangeSubmissionText = {text ->
            uiStateVar = uiStateVar.copy(
                latestSubmission = uiStateVar.latestSubmission?.shallowCopy {
                    casText = text
                },
            )
        }
        onChangeCourseComment = {
            uiStateVar = uiStateVar.copy(
                newCourseCommentText = it
            )
        }
    }
}