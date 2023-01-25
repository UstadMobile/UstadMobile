package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ScopedGrantListUiState
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.view.components.UstadBlankIcon
import mui.material.*
import react.*

external interface ScopedGrantListScreenProps : Props {

    var uiState: ScopedGrantListUiState

    var onClickScopedGrant: (ScopedGrantWithName) -> Unit

    var onClickAddScopedGrant: () -> Unit

}

val ScopedGrantListScreenComponent2 = FC<ScopedGrantListScreenProps> { props ->

    val strings = useStringsXml()

    Container {

        UstadAddListItem {
            text = strings[MessageID.add]
            onClickAdd = { props.onClickAddScopedGrant() }
        }

        List{
            props.uiState.scopedGrantList
                .forEach { scopedGrant ->
                    ListItem{
                        ListItemButton {
                            onClick = {
                                props.onClickScopedGrant(scopedGrant)
                            }

                            ListItemIcon{
                                UstadBlankIcon()
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