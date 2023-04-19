package com.ustadmobile.view.clazzdetailoverview

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.viewmodel.ClazzDetailOverviewUiState
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.material.*
import mui.material.Stack
import mui.material.List
import mui.material.StackDirection
import mui.system.responsive
import react.*
//DO NOT import mui.icons.material.[*] - this will lead to severe performance issues.
import mui.icons.material.Group
import mui.icons.material.Event
import mui.icons.material.Login


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


    val clazzDateRangeFormatted = useFormattedDateRange(
        props.uiState.clazz?.clazzStartTime ?: 0L,
    props.uiState.clazz?.clazzEndTime ?: 0L,
        props.uiState.clazz?.clazzTimeZone ?: "UTC"
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
                valueText = ReactNode(numMembers)
                labelText = strings[MessageID.members]
            }

            if (props.uiState.clazzCodeVisible) {
                UstadDetailField {
                    icon = Login.create()
                    valueText = ReactNode(props.uiState.clazz?.clazzCode ?: "")
                    labelText = strings[MessageID.class_code]
                    onClick = {
                        props.onClickClassCode(props.uiState.clazz?.clazzCode ?: "")
                    }
                }
            }

            if (props.uiState.clazzSchoolUidVisible){
                UstadDetailField {
                    icon = mui.icons.material.School.create()
                    valueText = ReactNode(props.uiState.clazz?.clazzSchool?.schoolName ?: "")
                }
            }

            if (props.uiState.clazzDateVisible){
                UstadDetailField {
                    icon = Event.create()
                    valueText = ReactNode(clazzDateRangeFormatted)
                }
            }

            if (props.uiState.clazzHolidayCalendarVisible){
                UstadDetailField {
                    icon = Event.create()
                    valueText = ReactNode(props.uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: "")
                }
            }

            List {
                Typography {
                    + strings[MessageID.schedule]
                }

                props.uiState.scheduleList.forEach { scheduleItem ->
                    ClazzDetailOverviewScheduleListItem {
                        schedule = scheduleItem
                    }
                }
            }

            List {
                props.uiState.courseBlockList.forEach { courseBlockItem ->

                    ClazzDetailOverviewCourseBlockListItem {
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


val ICON_SIZE = 40.0.px


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
                    cbIndentLevel = 0
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
