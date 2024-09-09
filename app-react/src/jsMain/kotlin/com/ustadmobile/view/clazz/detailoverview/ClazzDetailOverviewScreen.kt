package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazz.defaultCourseBannerImageIndex
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
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

val ClazzDetailOverviewComponent = FC<ClazzDetailOverviewProps> { props ->

    val strings = useStringProvider()

    val clazzDateRangeFormatted = useFormattedDateRange(
        props.uiState.clazz?.clazzStartTime ?: 0L,
    props.uiState.clazz?.clazzEndTime ?: 0L,
        props.uiState.clazz?.clazzTimeZone ?: "UTC"
    )

    val courseBlocks = props.uiState.courseBlockList.mapNotNull { it.courseBlock }

    val hasModules = props.uiState.hasModules

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val coursePictureUri = props.uiState.clazzAndDetail?.coursePicture?.coursePictureUri
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

            items(
                list = props.uiState.displayBlockList,
                key ={ "cb${it.courseBlock?.cbUid}" }
            ) { courseBlockItem ->
                ClazzDetailOverviewCourseBlockListItem.create {
                    courseBlock = courseBlockItem
                    showGrade = props.uiState.clazzAndDetail?.activeUserIsStudent ?: false
                    allCourseBlocks = courseBlocks
                    blockStatuses = props.uiState.blockStatusesForActiveUser
                    showExpandCollapse = hasModules
                    onClickCourseBlock = props.onClickCourseBlock
                    expanded = (courseBlockItem.courseBlock?.cbUid ?: 0) !in props.uiState.collapsedBlockUids
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

    ClazzDetailOverviewComponent {
        uiState = uiStateVal
        listRefreshCommandFlow = viewModel.listRefreshCommandFlow
        onClickCourseBlock = viewModel::onClickCourseBlock
        onClickClazzCode = viewModel::onClickClazzCode
        onClickPermissions = viewModel::onClickPermissions
    }
}

