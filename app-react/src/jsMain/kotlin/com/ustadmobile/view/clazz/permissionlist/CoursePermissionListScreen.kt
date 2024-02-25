package com.ustadmobile.view.clazz.permissionlist

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListUiState
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListViewModel
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import react.FC
import react.Props
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.core.jso
import mui.material.Dialog
import react.create
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import react.ReactNode
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import mui.material.DialogTitle

val CoursePermissionListScreen = FC<Props> {

    val strings = useStringProvider()

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CoursePermissionListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CoursePermissionListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    val terminologyEntries = useCourseTerminologyEntries(uiStateVal.courseTerminology)

    CoursePermissionListComponent {
        uiState = uiStateVal
        onClickEntry = viewModel::onClickEntry
        courseTerminologyEntries = terminologyEntries
    }



    Dialog {
        open = uiStateVal.addOptionsVisible

        onClose = { _, _ ->
            viewModel.onDismissAddOptions()
        }

        DialogTitle {
            + strings[MR.strings.grant_permission_to]
        }

        List {
            ListItem {
                ListItemButton {
                    id = "teachers_button"
                    onClick = {
                        viewModel.onClickAddNewForRole(ClazzEnrolment.ROLE_TEACHER)
                    }
                    ListItemText {
                        primary = ReactNode(
                            courseTerminologyResource(terminologyEntries, strings, MR.strings.teachers_literal)
                        )
                    }
                }
            }

            ListItem {
                ListItemButton {
                    id = "students_button"
                    onClick = {
                        viewModel.onClickAddNewForRole(ClazzEnrolment.ROLE_STUDENT)
                    }

                    ListItemText {
                        primary = ReactNode(
                            courseTerminologyResource(terminologyEntries, strings, MR.strings.students)
                        )
                    }
                }
            }

            ListItem {
                ListItemButton {
                    id = "select_person_button"
                    onClick = {
                        viewModel.onClickAddNewForPerson()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.select_person])
                    }
                }
            }
        }
    }

    UstadFab {
        fabState = appState.fabState
    }

}

external interface CoursePermissionListProps: Props {

    var uiState: CoursePermissionListUiState

    var onClickEntry: (CoursePermission) -> Unit

    var courseTerminologyEntries: List<TerminologyEntry>

}

val CoursePermissionListComponent = FC<CoursePermissionListProps> { props ->

    val infiniteQueryResult = usePagingSource(props.uiState.permissionsList, true)

    val muiAppState = useMuiAppState()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }


        content = virtualListContent {
            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { "${it.coursePermission?.cpUid}" }
            ) { item ->
                CoursePermissionListItem.create {
                    coursePermission = item
                    permissionLabels = props.uiState.permissionLabels
                    courseTerminologyEntries = props.courseTerminologyEntries
                }
            }
        }

        UstadStandardContainer {
            List {
                VirtualListOutlet()
            }
        }
    }

}
