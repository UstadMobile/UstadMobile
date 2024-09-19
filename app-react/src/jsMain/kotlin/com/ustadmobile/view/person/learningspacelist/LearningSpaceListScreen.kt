package com.ustadmobile.view.person.learningspacelist

import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListUiState
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.hooks.useStringProvider
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import react.ReactNode


external interface LearningSpaceListScreenProps : Props {
    var uiState: LearningSpaceListUiState
    var onClickNext: () -> Unit
}

val LearningSpaceListComponent2 = FC<LearningSpaceListScreenProps> { props ->

    val strings = useStringProvider()


    ListItem {
        key = "0"
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
}

val LearningSpaceListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LearningSpaceListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(LearningSpaceListUiState())

    LearningSpaceListComponent2 {
        this.uiState = uiState
        onClickNext = viewModel::onClickNext
    }
}
