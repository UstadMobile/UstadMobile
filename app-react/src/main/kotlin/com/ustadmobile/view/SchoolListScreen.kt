package com.ustadmobile.view

import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SchoolListUiState
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import csstype.JustifyContent
import mui.icons.material.AccountBalance
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface SchoolListScreenProps: Props {

    var uiState: SchoolListUiState

    var onClickSchool: (SchoolWithMemberCountAndLocation) -> Unit

}

val SchoolListScreenComponent2 = FC<SchoolListScreenProps> { props ->

    Container{

        val strings = useStringsXml()

        props.uiState.schoolList.forEach { school ->
            ListItem{
                ListItemButton {
                    sx {
                        justifyContent = JustifyContent.end
                    }
                    onClick = {
                        props.onClickSchool(school)
                    }

                    ListItemText {
                        primary = ReactNode(school.schoolName ?: "")
                    }

                    ListItemSecondaryAction {
                        Icon{
                            + AccountBalance.create()
                        }
                    }
                }
            }
        }

    }

}

val SchoolListScreenPreview = FC<Props> {
    SchoolListScreenComponent2{
        uiState = SchoolListUiState(
            schoolList = listOf(
                SchoolWithMemberCountAndLocation().apply {
                    schoolName = "School A"
                    schoolAddress = "Nairobi, Kenya"
                }
            )
        )
    }
}