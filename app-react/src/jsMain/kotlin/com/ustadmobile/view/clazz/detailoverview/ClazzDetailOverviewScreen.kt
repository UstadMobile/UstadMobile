package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadRawHtml
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.*
import js.core.jso
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
import react.router.useLocation


external interface ClazzDetailOverviewProps : Props {

    var uiState: ClazzDetailOverviewUiState

    var onClickClassCode: (String) -> Unit

    var onClickCourseBlock: (CourseBlock) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit
}

val ClazzDetailOverviewComponent2 = FC<ClazzDetailOverviewProps> { props ->

    val strings = useStringProvider()



    val clazzDateRangeFormatted = useFormattedDateRange(
        props.uiState.clazz?.clazzStartTime ?: 0L,
    props.uiState.clazz?.clazzEndTime ?: 0L,
        props.uiState.clazz?.clazzTimeZone ?: "UTC"
    )

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val courseBlocksResult = usePagingSource(
        props.uiState.courseBlockList, true, 50
    )


    VirtualList{
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item {
                Stack.create {
                    direction = responsive(StackDirection.column)
                    spacing = responsive(10.px)


                    Typography{
                        UstadRawHtml {
                            html = (props.uiState.clazz?.clazzDesc ?: "")
                        }
                    }

                    UstadDetailField {
                        icon = Group.create()
                        valueText = ReactNode(
                            strings.format(
                                MR.strings.x_teachers_y_students,
                                (props.uiState.clazz?.numTeachers ?: 0).toString(),
                                (props.uiState.clazz?.numStudents ?: 0).toString(),
                            )
                        )
                        labelText = strings[MR.strings.members_key].capitalizeFirstLetter()
                    }

                    if (props.uiState.clazzCodeVisible) {
                        UstadDetailField {
                            icon = Login.create()
                            valueText = ReactNode(props.uiState.clazz?.clazzCode ?: "")
                            labelText = strings[MR.strings.class_code]
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
                            labelText = "${strings[MR.strings.start_date]} - ${strings[MR.strings.end_date]}"
                        }
                    }

                    if (props.uiState.clazzHolidayCalendarVisible){
                        UstadDetailField {
                            icon = Event.create()
                            valueText = ReactNode(props.uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: "")
                        }
                    }
                }
            }

            items(
                list = props.uiState.scheduleList,
                key = { "sc${it.scheduleUid}" }
            ) { scheduleItem ->
                ClazzDetailOverviewScheduleListItem.create {
                    schedule = scheduleItem
                }
            }

            infiniteQueryPagingItems(
                items = courseBlocksResult,
                key = { "cb${it.courseBlock?.cbUid}" }
            ) { courseBlockItem ->
                ClazzDetailOverviewCourseBlockListItem.create {
                    courseBlock = courseBlockItem
                    onClickCourseBlock = props.onClickCourseBlock
                    onClickContentEntry = props.onClickContentEntry
                    onClickDownloadContentEntry = props.onClickDownloadContentEntry
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }
}


val ICON_SIZE = 40.0.px


val ClazzDetailOverviewScreen = FC<Props> {
    val location = useLocation()

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzDetailOverviewViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiStateVal: ClazzDetailOverviewUiState by viewModel.uiState.collectAsState(
        ClazzDetailOverviewUiState()
    )

    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }

    ClazzDetailOverviewComponent2 {
        uiState = uiStateVal
        onClickCourseBlock = viewModel::onClickCourseBlock
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
                    scheduleUid = 1
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                },
                Schedule().apply {
                    scheduleUid = 2
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                }
            ),
            courseBlockList = {
                ListPagingSource(
                    listOf(
                        CourseBlockAndDisplayDetails(
                            courseBlock = CourseBlock().apply {
                                cbUid = 1
                                cbTitle = "Module"
                                cbDescription = "Description"
                                cbType = CourseBlock.BLOCK_MODULE_TYPE
                            },
                            expanded = true
                        ),
                        CourseBlockAndDisplayDetails(
                            courseBlock = CourseBlock().apply {
                                cbUid = 2
                                cbTitle = "Main discussion board"
                                cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
                            },
                        ),
                        CourseBlockAndDisplayDetails(
                            courseBlock = CourseBlock().apply {
                                cbUid = 3
                                cbDescription = "Description"
                                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                                cbIndentLevel = 0
                                cbTitle = "Assignment"
                            }
                        ),
                        CourseBlockAndDisplayDetails(
                            courseBlock = CourseBlock().apply {
                                cbUid = 4
                                cbType = CourseBlock.BLOCK_CONTENT_TYPE
                                cbTitle = "Content entry"
                            }
                        ),
                        CourseBlockAndDisplayDetails(
                            courseBlock = CourseBlock().apply {
                                cbUid = 5
                                cbTitle = "Text Block Module"
                                cbDescription = "<pre>\n" +
                                        "            GeeksforGeeks\n" +
                                        "                         A Computer   Science Portal   For Geeks\n" +
                                        "        </pre>"
                                cbType = CourseBlock.BLOCK_TEXT_TYPE
                            }
                        )
                    )
                )
            },
            clazzCodeVisible = true
        )
    }
}
