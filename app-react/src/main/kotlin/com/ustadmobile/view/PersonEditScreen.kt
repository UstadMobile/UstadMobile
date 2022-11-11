package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.PersonEditUiState
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadExposedDropDownMenuField
import com.ustadmobile.mui.components.UstadTextEditField
import mui.system.Container
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props

external interface PersonEditScreenProps : Props{
    var uiState: PersonEditUiState

    var onPersonChanged: (PersonWithAccount) -> Unit
}

val PersonEditComponent2 = FC <PersonEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)

            UstadTextEditField {
                value = props.uiState.person?.firstNames ?: ""
                label = strings[MessageID.first_names]
                error = props.uiState.firstNameError
                onChange = {
                    //props.onPersonChanged()
                }
            }

            UstadTextEditField {
                value = props.uiState.person?.lastName ?: ""
                label = strings[MessageID.last_name]
                error = props.uiState.lastNameError
                onChange = {
                    //props.onPersonChanged()
                }
            }

            UstadExposedDropDownMenuField {
                value = "Female"
                options = listOf("Female", "Male", "Other")
                label = "Gender"
                id = "gender"
                itemText = { it.toString() }
                itemValue = { it.toString() }
            }

            UstadDateEditField {

            }
        }
    }
}

val PersonEditScreenPreview = FC<Props> {
    PersonEditComponent2 {
        uiState = PersonEditUiState(
            person = PersonWithAccount().apply {
                firstNames = "Bob"
                lastName = "Jones"
            }
        )
    }
}
