package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.PermissionConstants
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.viewmodel.ScopedGrantListUiState
import com.ustadmobile.lib.db.entities.Role
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

        List{

            UstadAddListItem {
                text = strings[MessageID.add]
                onClickAdd = { props.onClickAddScopedGrant() }
            }

            props.uiState.scopedGrantList
                .forEach { scopedGrant ->

                    val permissions = PermissionConstants.PERMISSION_MESSAGE_IDS.filter{
                        scopedGrant.sgPermissions.hasFlag(it.flagVal)}.map {
                        strings[it.messageId] }.joinToString()

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
                                secondary = ReactNode(permissions)
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
                    sgUid = 1
                    name = "First Item"
                    sgPermissions = Role.PERMISSION_PERSON_DELEGATE+ Role.PERMISSION_SCHOOL_UPDATE
                },
                ScopedGrantWithName().apply {
                    sgUid = 2
                    name = "Second Item"
                    sgPermissions = Role.PERMISSION_PERSON_DELEGATE+
                            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE
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