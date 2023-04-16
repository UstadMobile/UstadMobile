package com.ustadmobile.view.clazzdetail

import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.viewmodel.ClazzDetailUiState
import com.ustadmobile.mui.components.UstadScreenTabs
import react.*

val ClazzDetailScreen = FC<Props> {

}

external interface ClazzDetailProps : Props{
    var uiState: ClazzDetailUiState
}

val ClazzDetailComponent = FC<ClazzDetailProps> { props ->
    UstadScreenTabs {
        tabs = props.uiState.tabs
    }
}

val ClazzDetailPreview = FC<Props> {

    ClazzDetailComponent {
        uiState = ClazzDetailUiState(
            tabs = listOf(
                TabItem(ClazzDetailOverviewView.VIEW_NAME, mapOf("clazzUid" to "1"), "Course"),
                TabItem(ClazzMemberListView.VIEW_NAME, mapOf("clazzUid" to "1"), "Members")
            )
        )
    }

}
