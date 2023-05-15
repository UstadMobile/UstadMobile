package com.ustadmobile.view.clazzassignment.detailoverview

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import csstype.px
import kotlinx.datetime.TimeZone
import mui.material.*
import mui.system.responsive
import mui.material.List
import react.FC
import react.Props
import react.useState
import react.ReactNode
import react.create
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItemUiState as UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.mui.components.UstadCourseAssignmentMarkListItem
import com.ustadmobile.wrappers.quill.ReactQuill
import csstype.Height
import csstype.pct
import js.core.jso
import mui.icons.material.Done
import mui.icons.material.DoneAll
import mui.icons.material.EventAvailable
import mui.icons.material.Add
import mui.icons.material.InsertDriveFile as InsertDriveFileIcon
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.view.clazzassignment.AssignmentCommentTextFieldListItem
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet

val ASSIGNMENT_STATUS_MAP = mapOf(
    CourseAssignmentSubmission.NOT_SUBMITTED to Done.create(),
    CourseAssignmentSubmission.SUBMITTED to Done.create(),
    CourseAssignmentSubmission.MARKED to DoneAll.create()
)

external interface ClazzAssignmentDetailOverviewScreenProps : Props {

    var uiState: ClazzAssignmentDetailOverviewUiState

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickMark: (CourseAssignmentMarkWithPersonMarker) -> Unit

    var onChangeCourseComment: (String) -> Unit

    var onChangePrivateComment: (String) -> Unit

    var onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickAddTextSubmission: () -> Unit

    var onClickAddFileSubmission: () -> Unit

    var onClickSubmitSubmission: () -> Unit

}

private val ClazzAssignmentDetailOverviewScreenComponent2 = FC<ClazzAssignmentDetailOverviewScreenProps> { props ->

    val strings = useStringsXml()

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val caSubmissionPolicyText =
        strings[SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS[
            props.uiState.assignment?.caSubmissionPolicy
                ?: ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE].messageId]


    val caFileType = strings[
        SubmissionConstants.FILE_TYPE_MAP[
            props.uiState.assignment?.caFileType] ?: MessageID.document
    ]

    val muiAppState = useMuiAppState()

    val courseCommentInfiniteQueryResult = usePagingSource(
        props.uiState.courseComments, true, 50
    )

    val privateCommentIninfiteQueryResult = usePagingSource(
        props.uiState.privateComments, true, 50
    )


    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = csstype.Contain.strict
            overflowY = csstype.Overflow.scroll
        }

        content = virtualListContent {
            //Header section - description, deadline, etc
            item {
                Stack.create {
                    spacing = responsive(20.px)

                    UstadRawHtml {
                        html = props.uiState.courseBlock?.cbDescription ?: ""
                    }

                    if (props.uiState.cbDeadlineDateVisible) {
                        UstadDetailField {
                            valueText = ReactNode(
                                "$formattedDateTime (${TimeZone.currentSystemDefault().id})"
                            )
                            labelText = strings[MessageID.deadline]
                            icon = EventAvailable.create()
                            onClick = { }
                        }
                    }

                    UstadDetailField {
                        valueText = ReactNode(caSubmissionPolicyText)
                        labelText = strings[MessageID.submission_policy]
                        icon = (ASSIGNMENT_STATUS_MAP[
                            props.uiState.assignment?.caSubmissionPolicy] ?: Done.create())
                        onClick = { }
                    }

                    UstadAssignmentSubmissionHeader {
                        uiState = props.uiState.submissionHeaderUiState
                    }
                }
            }


            if (props.uiState.unassignedErrorVisible) {
                ListItem {
                    ListItemText {
                        primary = ReactNode(props.uiState.unassignedError ?: "")
                    }
                }
            }

            //submission section
            if(props.uiState.activeUserIsSubmitter) {
                item {
                    UstadDetailHeader.create {
                        header = ReactNode(strings[MessageID.your_submission])
                    }
                }

                if(props.uiState.submissionTextFieldVisible) {
                    item {
                        ReactQuill.create {
                            id = "assignment_text"
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
                                    Add { }
                                }

                                ListItemText {
                                    primary =
                                        ReactNode(strings[MessageID.add_file].uppercase())
                                    secondary = ReactNode(
                                        "${strings[MessageID.file_type_chosen]} $caFileType " +
                                            strings[MessageID.max_number_of_files]
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
                    item {
                        Button.create {
                            onClick = { props.onClickSubmitSubmission() }
                            disabled = !props.uiState.fieldsEnabled
                            variant = ButtonVariant.contained
                            fullWidth = true

                            +strings[MessageID.submit].uppercase()
                        }
                    }

                }

                //List of grades awarded
                item {
                    UstadDetailHeader.create {
                        header = ReactNode(strings[MessageID.grades_class_age])
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
                    list = props.uiState.markList,
                    key = { "mark_${it.camUid}"}
                ) { markItem ->
                    UstadCourseAssignmentMarkListItem.create {
                        onClickMark = props.onClickMark
                        uiState = UstadCourseAssignmentMarkListItemUiState(
                            mark = markItem,
                            block = props.uiState.courseBlock ?: CourseBlock()
                        )
                    }
                }
            }

            //Course comments
            item {
                ListItem.create {
                    ListItemText {
                        primary = ReactNode(strings[MessageID.class_comments])
                    }
                }
            }

            item {
                AssignmentCommentTextFieldListItem.create {
                    onChange = props.onChangeCourseComment
                    label = ReactNode(strings[MessageID.add_class_comment])
                    value = props.uiState.newCourseCommentText
                    activeUserPersonUid = props.uiState.activeUserPersonUid
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

            //Private comments
            if(props.uiState.activeUserIsSubmitter) {
                item {
                    ListItem.create {
                        ListItemText {
                            primary = ReactNode(strings[MessageID.private_comments])
                        }
                    }
                }

                item {
                    AssignmentCommentTextFieldListItem.create {
                        onChange = props.onChangePrivateComment
                        label = ReactNode(strings[MessageID.add_private_comment])
                        value = props.uiState.newPrivateCommentText
                        activeUserPersonUid = props.uiState.activeUserPersonUid
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
            submissionTextFieldVisible = true,
            hasFilesToSubmit = true,
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
                CourseAssignmentMarkWithPersonMarker().apply {
                    marker = Person().apply {
                        firstNames = "John"
                        lastName = "Smith"
                        isGroup = true
                        camMarkerSubmitterUid = 2
                        camMarkerComment = "Comment"

                    }
                }
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
        onChangeCourseComment = {
            uiStateVar = uiStateVar.copy(
                newCourseCommentText = it
            )
        }
    }
}