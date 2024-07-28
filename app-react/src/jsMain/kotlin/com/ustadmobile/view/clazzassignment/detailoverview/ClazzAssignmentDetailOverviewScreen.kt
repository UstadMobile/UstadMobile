package com.ustadmobile.view.clazzassignment.detailoverview

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailoverviewSubmissionUiState
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useDateFormatter
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useEmptyFlow
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import kotlinx.datetime.TimeZone
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState as UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.mui.components.UstadCourseAssignmentMarkListItem
import web.cssom.Height
import web.cssom.pct
import js.objects.jso
import mui.icons.material.Done as DoneIcon
import mui.icons.material.DoneAll as DoneAllIcon
import mui.icons.material.EventAvailable as EventAvailableIcon
import mui.icons.material.Add as AddIcon
import mui.icons.material.Groups as GroupsIcon
import mui.icons.material.Person as PersonIcon
import mui.icons.material.Group as GroupIcon
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTimeFormatter
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.view.clazzassignment.AssignmentCommentTextFieldListItem
import com.ustadmobile.view.clazzassignment.CourseAssignmentSubmissionComponent
import com.ustadmobile.view.clazzassignment.CourseAssignmentSubmissionFileListItem
import com.ustadmobile.view.clazzassignment.UstadCommentListItem
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import emotion.react.css
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import mui.system.sx
import react.dom.html.ReactHTML.input
import react.useRef
import react.useRequiredContext
import web.cssom.Contain
import web.cssom.Overflow
import web.cssom.TextAlign
import web.file.File
import web.html.HTMLInputElement
import web.html.InputType
import web.url.URL

val ASSIGNMENT_STATUS_MAP = mapOf(
    CourseAssignmentSubmission.NOT_SUBMITTED to DoneIcon,
    CourseAssignmentSubmission.SUBMITTED to DoneIcon,
    CourseAssignmentSubmission.MARKED to DoneAllIcon,
)

external interface ClazzAssignmentDetailOverviewScreenProps : Props {

    var uiState: ClazzAssignmentDetailOverviewUiState

    var editableSubmissionFlow: Flow<ClazzAssignmentDetailoverviewSubmissionUiState>

    var newPrivateCommentFlow: Flow<String>

    var newCourseCommentFlow: Flow<String>

    var onChangeSubmissionText: (String) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onChangeCourseComment: (String) -> Unit

    var onChangePrivateComment: (String) -> Unit

    var onClickSubmitCourseComment: () -> Unit

    var onClickSubmitPrivateComment: () -> Unit

    var onClickSubmitSubmission: () -> Unit

    var onClickCourseGroupSet: () -> Unit

    var onAddFile: (File) -> Unit

    var onRemoveSubmissionFile: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit

    var onToggleSubmissionExpandCollapse: (CourseAssignmentSubmission) -> Unit

    var onClickSubmissionFile: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit

    var onDeleteComment: (Comments) -> Unit

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

    val refreshCommandFlow = useEmptyFlow<RefreshCommand>()

    val courseCommentMediatorResult = useDoorRemoteMediator(
        props.uiState.courseComments, refreshCommandFlow
    )

    val courseCommentInfiniteQueryResult = usePagingSource(
        courseCommentMediatorResult.pagingSourceFactory, true, 50
    )

    val privateCommentMediatorResult = useDoorRemoteMediator(
        props.uiState.privateComments, refreshCommandFlow
    )

    val privateCommentIninfiteQueryResult = usePagingSource(
        privateCommentMediatorResult.pagingSourceFactory, true, 50
    )

    val courseTerminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    val inputRef = useRef<HTMLInputElement>(null)

    val timeFormatterVal = useTimeFormatter()

    val dateFormatterVal = useDateFormatter()

    /**
     * Used to handle submission file selection.
     */
    input {
        type = InputType.file
        ref = inputRef
        id = "assignment_file_input"

        //Note: if the value is not set then React doesn't recognize this as a controlled component
        // Components should not change between controlled and uncontrolled. We are just using the
        // input to get the file from the onChange event.
        value = ""

        css {
            asDynamic().display = "none"
        }

        onChange = {
            val file = it.target.files?.get(0)
            if (file != null) {
                props.onAddFile(file)
            }
        }
    }

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        id = "VirtualList"

        content = virtualListContent {
            item("block_header") {
                UstadCourseBlockHeader.create {
                    sx {
                        paddingTop = theme.spacing(2)
                    }

                    block = props.uiState.courseBlock
                    picture = props.uiState.courseBlockPicture
                }
            }

            //Header section - description, deadline, etc
            item("header_section_item") {
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
                if(props.uiState.activeUserCanSubmit) {
                    item("submission_header_item") {
                        UstadDetailHeader.create {
                            val suffix = if(props.uiState.isGroupSubmission) {
                                "(${strings.format(MR.strings.group_number, props.uiState.submitterUid.toString())})"
                            }else {
                                ""
                            }
                            header = ReactNode("${strings[MR.strings.your_submission]} $suffix")
                        }
                    }
                }

                if(props.uiState.submissionTextFieldVisible) {
                    item(key = "assignment_text_item") {
                        CourseAssignmentSubmissionEditComponent.create {
                            stateFlow = props.editableSubmissionFlow
                            overviewUiState = props.uiState
                            onChangeSubmissionText = props.onChangeSubmissionText
                        }
                    }
                }

                if(props.uiState.addFileSubmissionVisible) {
                    item(key = "add_file_button_item") {
                        ListItem.create {
                            ListItemButton {
                                id = "add_file_button"
                                onClick = {
                                    inputRef.current?.click()
                                }
                                ListItemIcon {
                                    AddIcon()
                                }

                                ListItemText {
                                    primary = ReactNode(strings[MR.strings.add_file])
                                    secondary = ReactNode(
                                "${strings[MR.strings.file_type_chosen]} $caFileType " +
                                        "${strings[MR.strings.number_of_files]}: ${props.uiState.assignment?.caNumberOfFiles} " +
                                        "${strings[MR.strings.size_limit]}: ${props.uiState.assignment?.caSizeLimit}"
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    list = props.uiState.editableSubmissionFiles,
                    key = { "attachment_${it.submissionFile?.casaUid}" }
                ) {
                    CourseAssignmentSubmissionFileListItem.create {
                        file = it
                        onRemove = props.onRemoveSubmissionFile
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

                props.uiState.submissions.forEachIndexed { index, submissionItem ->
                    val isCollapsedVal = submissionItem.submission.casUid in props.uiState.collapsedSubmissions
                    item(key = "submission_${submissionItem.submission.casUid}") {
                        CourseAssignmentSubmissionComponent.create {
                            submission = submissionItem.submission
                            submissionNum = props.uiState.submissions.size - index
                            isCollapsed = isCollapsedVal
                            onToggleExpandCollapse = {
                                props.onToggleSubmissionExpandCollapse(submissionItem.submission)
                            }
                        }
                    }

                    if(!isCollapsedVal) {
                        items(
                            list = submissionItem.files,
                            key = { "submitted_file_${it.submissionFile?.casaUid}"}
                        ) {
                            CourseAssignmentSubmissionFileListItem.create {
                                file = it
                                onClick = props.onClickSubmissionFile
                            }
                        }
                    }
                }

                //List of grades awarded
                item(key = "grades_scoring_header") {
                    UstadDetailHeader.create {
                        header = ReactNode(strings[MR.strings.grades_scoring])
                    }
                }

                item(key = "grade_filter_chips") {
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
                            localDateTimeNow = props.uiState.localDateTimeNow,
                            dayOfWeekStrings = props.uiState.dayOfWeekStringMap,
                        )
                        timeFormatter = timeFormatterVal
                        dateFormatter = dateFormatterVal
                    }
                }
            }

            if(props.uiState.showClassComments) {
                //Course comments
                item(key = "course_comments_header_item") {
                    ListItem.create {
                        ListItemText {
                            primary = ReactNode(strings[MR.strings.course_comments])
                        }
                    }
                }

                item(key = "course_comment_textfield_item") {
                    AssignmentCommentTextFieldListItem.create {
                        onChange = props.onChangeCourseComment
                        label = ReactNode(strings[MR.strings.add_class_comment])
                        value = props.newCourseCommentFlow
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
                        timeFormatter = timeFormatterVal
                        dateFormatter = dateFormatterVal
                        dateTimeNow = props.uiState.localDateTimeNow
                        dayOfWeekMap = props.uiState.dayOfWeekStringMap
                        showModerateOptions = props.uiState.showModerateOptions
                        onDeleteComment = props.onDeleteComment
                    }
                }
            }


            //Private comments
            if(props.uiState.showPrivateComments) {
                item(key = "private_comments_header_item") {
                    ListItem.create {
                        ListItemText {
                            primary = ReactNode(strings[MR.strings.private_comments])
                        }
                    }
                }

                /*
                 * Note: React element Key for textfield MUST be set: otherwise the course comment
                 * textfield and the private comment textfield DOM elements can be switched over
                 * (e.g. when the key is the list index, React will see it as being the same element,
                 * regardless of the DOM ID value)
                 */
                item(key = "private_comment_textfield_item") {
                    AssignmentCommentTextFieldListItem.create {
                        onChange = props.onChangePrivateComment
                        label = ReactNode(strings[MR.strings.add_private_comment])
                        value = props.newPrivateCommentFlow
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
                        timeFormatter = timeFormatterVal
                        dateFormatter = dateFormatterVal
                        dateTimeNow = props.uiState.localDateTimeNow
                        dayOfWeekMap = props.uiState.dayOfWeekStringMap
                        showModerateOptions = props.uiState.showModerateOptions
                        onDeleteComment = props.onDeleteComment
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
        editableSubmissionFlow = viewModel.editableSubmissionUiState
        newCourseCommentFlow = viewModel.newCourseCommentText
        newPrivateCommentFlow = viewModel.newPrivateCommentText
        onChangeSubmissionText = viewModel::onChangeSubmissionText
        onChangeCourseComment = viewModel::onChangeCourseCommentText
        onChangePrivateComment = viewModel::onChangePrivateCommentText
        onClickSubmitCourseComment = viewModel::onClickSubmitCourseComment
        onClickSubmitPrivateComment = viewModel::onClickSubmitPrivateComment
        onClickSubmitSubmission = viewModel::onClickSubmit
        onClickFilterChip = viewModel::onClickMarksFilterChip
        onClickCourseGroupSet = viewModel::onClickCourseGroupSet
        onAddFile = { file ->
            val uri = URL.createObjectURL(file)
            viewModel.onAddSubmissionFile(
                uri = uri,
                fileName = file.name,
                mimeType = file.type,
                size = file.size.toLong(),
            )
        }
        onRemoveSubmissionFile = viewModel::onRemoveSubmissionFile
        onToggleSubmissionExpandCollapse = viewModel::onToggleSubmissionExpandCollapse
        onClickSubmissionFile = viewModel::onOpenSubmissionFile
        onDeleteComment = viewModel::onDeleteComment
    }
}
