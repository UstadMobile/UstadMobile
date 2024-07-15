package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazz.defaultCourseBannerImageIndex
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField2
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.mui.components.UstadRawHtml
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import emotion.react.css
import web.cssom.*
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import react.dom.html.ReactHTML.img
import react.router.useLocation
import mui.icons.material.Shield as ShieldIcon


external interface ClazzDetailOverviewProps : Props {

    var uiState: ClazzDetailOverviewUiState

    var listRefreshCommandFlow: Flow<RefreshCommand>?

    var onClickClazzCode: (String) -> Unit

    var onClickCourseBlock: (CourseBlock) -> Unit

    var onClickPermissions: () -> Unit
}

val ClazzDetailOverviewComponent2 = FC<ClazzDetailOverviewProps> { props ->

    val strings = useStringProvider()

    val clazzDateRangeFormatted = useFormattedDateRange(
        props.uiState.clazz?.clazzStartTime ?: 0L,
    props.uiState.clazz?.clazzEndTime ?: 0L,
        props.uiState.clazz?.clazzTimeZone ?: "UTC"
    )

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val courseBlocksMediator = useDoorRemoteMediator(
        props.uiState.courseBlockList, props.listRefreshCommandFlow ?: emptyFlow()
    )

    val courseBlocksResult = usePagingSource(
        courseBlocksMediator.pagingSourceFactory, true, 50
    )

    val coursePictureUri = props.uiState.clazz?.coursePicture?.coursePictureUri
        ?: "img/default_course_banners/${defaultCourseBannerImageIndex(props.uiState.clazz?.clazzName)}.webp"


    VirtualList{
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item("banner") {
                img.create {
                    css {
                        height = 192.px
                        width = 100.pct
                        objectFit = ObjectFit.cover
                    }
                    src = coursePictureUri
                }
            }

            if(props.uiState.quickActionBarVisible) {
                item("quick_action_bar") {
                    Stack.create {
                        direction = responsive(StackDirection.column)

                        Stack {
                            direction = responsive(StackDirection.row)

                            if(props.uiState.managePermissionVisible) {
                                UstadQuickActionButton {
                                    text = strings[MR.strings.permissions]
                                    icon = ShieldIcon.create()
                                    onClick = {
                                        props.onClickPermissions()
                                    }
                                }
                            }
                        }

                        Divider()
                    }
                }
            }

            item("description") {
                Typography.create {
                    UstadRawHtml {
                        html = (props.uiState.clazz?.clazzDesc ?: "")
                    }
                }
            }

            item("members_total") {
                UstadDetailField2.create {
                    leadingContent = Group.create()
                    valueContent = ReactNode(props.uiState.membersString)
                    labelContent = ReactNode(strings[MR.strings.members_key].capitalizeFirstLetter())
                }
            }

            if (props.uiState.clazzCodeVisible) {
                item(key = "clazz_code") {
                    UstadDetailField2.create {
                        leadingContent = Login.create()
                        valueContent = ReactNode(props.uiState.clazz?.clazzCode ?: "")
                        labelContent = ReactNode(strings[MR.strings.invite_code])
                        onClick = {
                            props.onClickClazzCode(props.uiState.clazz?.clazzCode ?: "")
                        }
                    }
                }
            }

            if(props.uiState.clazzDateVisible) {
                UstadDetailField2.create {
                    leadingContent = Event.create()
                    valueContent = ReactNode(clazzDateRangeFormatted)
                    labelContent = ReactNode("${strings[MR.strings.start_date]} - ${strings[MR.strings.end_date]}")
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
        listRefreshCommandFlow = viewModel.listRefreshCommandFlow
        onClickCourseBlock = viewModel::onClickCourseBlock
        onClickClazzCode = viewModel::onClickClazzCode
        onClickPermissions = viewModel::onClickPermissions
    }
}

@Suppress("unused")
val ClazzDetailOverviewScreenPreview = FC<Props> {
    ClazzDetailOverviewComponent2 {
        uiState = ClazzDetailOverviewUiState(
            clazz = ClazzWithDisplayDetails().apply {
                clazzDesc = "Description"
                clazzCode = "abc123"
                clazzSchoolUid = 1
                clazzStartTime = ((14 * MS_PER_HOUR) + (30 * MS_PER_MIN)).toLong()
                clazzEndTime = 0
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
