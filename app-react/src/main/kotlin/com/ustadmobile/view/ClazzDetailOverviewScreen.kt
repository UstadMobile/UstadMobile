package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.viewmodel.ClazzDetailOverviewUiState
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadClazzAssignmentListItem
import com.ustadmobile.mui.components.UstadContentEntryListItem
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.view.components.UstadBlankIcon
import csstype.Padding
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.Stack
import mui.material.List
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*

external interface ClazzDetailOverviewProps : Props {

    var uiState: ClazzDetailOverviewUiState

    var onClickClassCode: (String) -> Unit

    var onClickCourseDiscussion: (CourseDiscussion?) -> Unit

    var onClickCourseExpandCollapse: (CourseBlockWithCompleteEntity) -> Unit

    var onClickTextBlock: (CourseBlockWithCompleteEntity) -> Unit

    var onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit
}

val ClazzDetailOverviewComponent2 = FC<ClazzDetailOverviewProps> { props ->

    val strings = useStringsXml()

    val numMembers = strings[MessageID.x_teachers_y_students]
        .replace("%1\$d", (props.uiState.clazz?.numTeachers ?: 0).toString())
        .replace("%2\$d", (props.uiState.clazz?.numStudents ?: 0).toString())

    val clazzStartTime = useFormattedTime(
        timeInMillisSinceMidnight = (props.uiState.clazz?.clazzStartTime ?: 0).toInt(),
    )

    val clazzEndTime = useFormattedTime(
        timeInMillisSinceMidnight = (props.uiState.clazz?.clazzEndTime ?: 0).toInt(),
    )

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography{
                + (props.uiState.clazz?.clazzDesc ?: "")
            }

            UstadDetailField {
                icon = Group.create()
                valueText = numMembers
                labelText = strings[MessageID.members]
            }

            if (props.uiState.clazzCodeVisible) {
                UstadDetailField {
                    icon = Login.create()
                    valueText = props.uiState.clazz?.clazzCode ?: ""
                    labelText = strings[MessageID.class_code]
                    onClick = {
                        props.onClickClassCode(props.uiState.clazz?.clazzCode ?: "")
                    }
                }
            }

            if (props.uiState.clazzSchoolUidVisible){
                TextImageRow {
                    icon = mui.icons.material.School
                    text = props.uiState.clazz?.clazzSchool?.schoolName ?: ""
                }
            }

            if (props.uiState.clazzDateVisible){
                TextImageRow {
                    icon = Event
                    text = "$clazzStartTime - $clazzEndTime"
                }
            }

            if (props.uiState.clazzHolidayCalendarVisible){
                TextImageRow {
                    icon = Event
                    text = props.uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: ""
                }
            }

            List {
                Typography {
                    + strings[MessageID.schedule]
                }

                props.uiState.scheduleList.forEach { schedule ->
                    val fromTimeFormatted = useFormattedTime(
                        timeInMillisSinceMidnight = schedule.sceduleStartTime.toInt(),
                    )

                    val toTimeFormatted = useFormattedTime(
                        timeInMillisSinceMidnight = schedule.scheduleEndTime.toInt(),
                    )

                    val text = "${strings[ScheduleConstants.SCHEDULE_FREQUENCY_MESSAGE_ID_MAP[schedule.scheduleFrequency] ?: 0]} " +
                            " ${strings[ScheduleConstants.DAY_MESSAGE_ID_MAP[schedule.scheduleDay] ?: 0]  } " +
                            " $fromTimeFormatted - $toTimeFormatted "

                    ListItem{
                        sx {
                            padding = Padding(
                                top = 0.px,
                                bottom = 0.px,
                                left = 22.px,
                                right = 0.px
                            )
                        }

                        UstadBlankIcon()

                        ListItemText{
                            primary = ReactNode(text)
                        }
                    }
                }
            }

            List {
                props.uiState.courseBlockList.forEach { courseBlockItem ->

                    CourseBlockListItem {
                        courseBlock = courseBlockItem
                        onClickCourseDiscussion = props.onClickCourseDiscussion
                        onClickCourseExpandCollapse = props.onClickCourseExpandCollapse
                        onClickTextBlock = props.onClickTextBlock
                        onClickAssignment = props.onClickAssignment
                        onClickContentEntry = props.onClickContentEntry
                        onClickDownloadContentEntry = props.onClickDownloadContentEntry
                    }

                }
            }
        }
    }
}

external interface TextImageRowProps : Props {

    var icon: SvgIconComponent

    var text: String

}

private val TextImageRow = FC<TextImageRowProps> { props ->

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(34.px)
        sx {
            padding = Padding(
                top = 10.px,
                bottom = 10.px,
                left = 38.px,
                right = 0.px
            )
        }

        Icon{
            + props.icon.create()
        }

        Typography {
            + props.text
        }
    }
}

external interface CourseBlockListItemProps : Props {

    var courseBlock: CourseBlockWithCompleteEntity

    var onClickCourseDiscussion: (CourseDiscussion?) -> Unit

    var onClickCourseExpandCollapse: (CourseBlockWithCompleteEntity) -> Unit

    var onClickTextBlock: (CourseBlockWithCompleteEntity) -> Unit

    var onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

private val CourseBlockListItem = FC<CourseBlockListItemProps> { props ->

    when(props.courseBlock.cbType){
        CourseBlock.BLOCK_MODULE_TYPE  -> {

            val trailingIcon = if(props.courseBlock.expanded)
                KeyboardArrowUp.create()
            else
                KeyboardArrowDown.create()

            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock.cbIndentLevel)
                    }

                   onClick = {
                       props.onClickCourseExpandCollapse(props.courseBlock)
                   }

                    ListItemIcon {
                        + Folder.create()
                    }

                    ListItemText {
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
                        secondary = ReactNode(props.courseBlock.cbDescription ?: "")
                    }
                }

                secondaryAction = Icon.create {
                    + trailingIcon
                }
            }
        }
        CourseBlock.BLOCK_DISCUSSION_TYPE -> {
            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock.cbIndentLevel)
                    }

                    onClick = {
                        props.onClickCourseDiscussion(props.courseBlock.courseDiscussion)
                    }

                    ListItemIcon {
                        + Forum.create()
                    }

                    ListItemText {
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
                        secondary = ReactNode(props.courseBlock.cbDescription ?: "")
                    }
                }
            }
        }
        CourseBlock.BLOCK_TEXT_TYPE -> {
            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock.cbIndentLevel)
                    }

                    onClick = {
                        props.onClickTextBlock(props.courseBlock)
                    }

                    ListItemIcon {
                        + Title.create()
                    }

                    ListItemText {
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
//                        secondary = { Html(courseBlock.cbDescription) },
                    }
                }
            }
        }
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
            UstadClazzAssignmentListItem {
                assignment = props.courseBlock.assignment
                    ?: ClazzAssignmentWithMetrics()
                courseBlock = props.courseBlock
                onClickAssignment = props.onClickAssignment
            }
        }
        CourseBlock.BLOCK_CONTENT_TYPE -> {
            UstadContentEntryListItem {
                contentEntry = props.courseBlock.entry
                    ?: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer()
                onClickContentEntry = props.onClickContentEntry
                onClickDownloadContentEntry = props.onClickDownloadContentEntry

            }
        }
    }
}

val ClazzDetailOverviewScreenPreview = FC<Props> {
    ClazzDetailOverviewComponent2 {
        uiState = ClazzDetailOverviewUiState(
            clazz = ClazzWithDisplayDetails().apply {
                clazzDesc = "Description"
                clazzCode = "abc123"
                clazzSchoolUid = 1
                clazzStartTime = ((14 * MS_PER_HOUR) + (30 * MS_PER_MIN)).toLong()
                clazzEndTime = 0
                clazzSchool = School().apply {
                    schoolName = "School Name"
                }
                clazzHolidayCalendar = HolidayCalendar().apply {
                    umCalendarName = "Holiday Calendar"
                }
            },
            scheduleList = listOf(
                Schedule().apply {
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                },
                Schedule().apply {
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                }
            ),
            courseBlockList = listOf(
                CourseBlockWithCompleteEntity().apply {
                    cbUid = 1
                    cbTitle = "Module"
                    cbDescription = "Description"
                    cbType = CourseBlock.BLOCK_MODULE_TYPE
                },
                CourseBlockWithCompleteEntity().apply {
                    cbUid = 2
                    cbTitle = "Main discussion board"
                    cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
                },
                CourseBlockWithCompleteEntity().apply {
                    cbUid = 3
                    cbDescription = "Description"
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    assignment = ClazzAssignmentWithMetrics().apply {
                        caTitle = "Assignment"
                        fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
                        progressSummary = AssignmentProgressSummary().apply {
                            submittedStudents = 5
                            markedStudents = 10
                        }
                    }
                },
                CourseBlockWithCompleteEntity().apply {
                    cbUid = 4
                    cbType = CourseBlock.BLOCK_CONTENT_TYPE
                    entry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                        title = "Content Entry"
                        scoreProgress = ContentEntryStatementScoreProgress().apply {
                            success = StatementEntity.RESULT_SUCCESS
                            progress = 70
                        }
                    }
                },
                CourseBlockWithCompleteEntity().apply {
                    cbUid = 5
                    cbTitle = "Text Block Module"
                    cbDescription = "<pre>\n" +
                            "            GeeksforGeeks\n" +
                            "                         A Computer   Science Portal   For Geeks\n" +
                            "        </pre>"
                    cbType = CourseBlock.BLOCK_TEXT_TYPE
                }
            ),
            clazzCodeVisible = true
        )
    }
}
