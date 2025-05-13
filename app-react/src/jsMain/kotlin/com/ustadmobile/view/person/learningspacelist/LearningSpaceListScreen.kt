package com.ustadmobile.view.person.learningspacelist

import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListUiState
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.hooks.useStringProvider
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.ReactNode
import react.create
import web.cssom.px


external interface LearningSpaceListScreenProps : Props {
    var uiState: LearningSpaceListUiState
    var onClickNext: () -> Unit
    var onSelectLearningSpace: (String) -> Unit
}

val LearningSpaceListComponent2 = FC<LearningSpaceListScreenProps> { props ->
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
                    items(
                        list = props.uiState.learningSpaces,
                        key = { it.url }
                    ) { learningSpace ->
                        ListItem.create{
                            disablePadding = true
                            key = "1"
                            ListItemButton {
                                onClick = {
                                    props.onSelectLearningSpace(learningSpace.url)
                                }

                                ListItemText {
                                    primary = ReactNode(learningSpace.name)
                                    secondary = ReactNode(learningSpace.url)
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
