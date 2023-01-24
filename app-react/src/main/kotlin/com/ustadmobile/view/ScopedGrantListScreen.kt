package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ScopedGrantListUiState
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import mui.icons.material.Add
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface ScopedGrantListScreenProps : Props {

    var uiState: ScopedGrantListUiState

    var onClickScopedGrant: (ScopedGrantWithName) -> Unit

    var onClickAddScopedGrant: () -> Unit

}

val ScopedGrantListScreenComponent2 = FC<ScopedGrantListScreenProps> { props ->

    val strings = useStringsXml()

    Container {

        ListItem {
            ListItemButton{

                onClick = {
                    props.onClickAddScopedGrant()
                }

                ListItemIcon {
                    Add{}
                }

                ListItemText{
                    primary = ReactNode((strings[MessageID.add]))
                }
            }
        }

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
            scopedGrantList = listOf(
                ScopedGrantWithName().apply {
                    name = "First Item"
                },
                ScopedGrantWithName().apply {
                    name = "Second Item"
                },
                ScopedGrantWithName().apply {
                    name = "Third Item"
                }
            )
        )
    }

    ScopedGrantListScreenComponent2 {
        uiState = uiStateVar
    }
}