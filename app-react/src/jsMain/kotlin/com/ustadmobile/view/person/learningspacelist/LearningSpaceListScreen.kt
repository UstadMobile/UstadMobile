package com.ustadmobile.view.person.learningspacelist

import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListUiState
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.hooks.useStringProvider
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTransferStatusIcon
import com.ustadmobile.view.clazz.permissionlist.CoursePermissionListItem
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.emptyFlow
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.ReactNode
import react.create
import react.useMemo
import react.useRequiredContext
import web.cssom.Contain
import web.cssom.Display
import web.cssom.Height
import web.cssom.JustifyContent
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px


external interface LearningSpaceListScreenProps : Props {
    var uiState: LearningSpaceListUiState
    var onClickNext: () -> Unit
    var onSelectLearningSpace: (String) -> Unit
}

val LearningSpaceListComponent2 = FC<LearningSpaceListScreenProps> { props ->
    val refreshFlow = useMemo(dependencies = emptyArray()) {
        emptyFlow<RefreshCommand>()
    }

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.learningSpaceList, refreshFlow
    )

    val infiniteQueryResult = usePagingSource(
        mediatorResult.pagingSourceFactory, true
    )
    val strings = useStringProvider()
    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            ListItem {
                sx {
                    paddingLeft = 22.px
                }
                ListItemButton {
                    id = "enter_link_manually"
                    onClick = {
                        props.onClickNext()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.enter_link_manually])

                    }
                }

            }
            VirtualList {

                content = virtualListContent {
                    infiniteQueryPagingItems(
                        items = infiniteQueryResult,
                        key = { "${it.lsiUid}" }
                    ) { learningSpace ->
                        ListItem.create{
                            disablePadding = true
                            key = "1"
                            ListItemButton {
                                onClick = {
                                    learningSpace?.lsiUrl?.let { it1 -> props.onSelectLearningSpace(it1) }
                                }


                                ListItemText {

                                    primary = ReactNode(learningSpace?.lsiUrl ?: "")

                                }
                            }

                        }
                    }
                }

                UstadStandardContainer {
                    mui.material.List {
                        VirtualListOutlet()
                    }
                }
            }


        }
    }


}

val LearningSpaceListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LearningSpaceListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(LearningSpaceListUiState())

    LearningSpaceListComponent2 {
        this.uiState = uiState
        onClickNext = viewModel::onClickNext
        onSelectLearningSpace = viewModel::onSelectLearningSpace

    }
}
