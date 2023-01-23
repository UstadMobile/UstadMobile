package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.ScopedGrantListUiState
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface ScopedGrantListScreenProps : Props {

    var uiState: ScopedGrantListUiState

    var onClickScopedGrant: (ScopedGrantWithName) -> Unit

}

val ScopedGrantListScreenComponent2 = FC<ScopedGrantListScreenProps> { props ->

    Container {

        List{
            props.uiState.scopedGrantList
                .forEach { scopedGrant ->
                    ListItem{
                        ListItemButton {
                            onClick = {
                                props.onClickScopedGrant(scopedGrant)
                            }

                            ListItemText {
                                primary = ReactNode(scopedGrant.name ?: "")
                            }
                        }
                    }
                }
        }
    }
}

val ScopedGrantListScreenPreview = FC<Props> {

    val uiStateVar : ScopedGrantListUiState by useState {
        ScopedGrantListUiState(

        )
    }

    ScopedGrantListScreenComponent2 {
        uiState = uiStateVar
    }
}