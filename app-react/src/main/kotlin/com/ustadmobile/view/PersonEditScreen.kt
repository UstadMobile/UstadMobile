package com.ustadmobile.view

import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants.GENDER_MESSAGE_IDS
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.PersonEditUiState
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import mui.system.Container
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface PersonEditScreenProps : Props{
    var uiState: PersonEditUiState

    var onPersonChanged: (PersonWithAccount?) -> Unit
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
                    props.onPersonChanged(props.uiState.person?.shallowCopy {
                        firstNames = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.person?.lastName ?: ""
                label = strings[MessageID.last_name]
                error = props.uiState.lastNameError
                onChange = {
                    props.onPersonChanged(props.uiState.person?.shallowCopy {
                        lastName = it
                    })
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.person?.gender ?: 0
                options = GENDER_MESSAGE_IDS
                label = "Gender"
                id = "gender"
                onChange = {
                    props.onPersonChanged(props.uiState.person?.shallowCopy {
                        gender = it?.value ?: 0
                    })
                }
            }

            UstadDateEditField {
                timeInMillis = props.uiState.person?.dateOfBirth ?: 0
                label = strings[MessageID.birthday]
                onChange = {
                    props.onPersonChanged(props.uiState.person?.shallowCopy {
                        dateOfBirth = it
                    })
                }
            }
        }
    }
}

val PersonEditScreenPreview = FC<Props> {

    var uiStateVar by useState {
        PersonEditUiState(
            person = PersonWithAccount().apply {
                firstNames = "Bob"
                lastName = "Jones"
            }
        )
    }

    PersonEditComponent2 {
        uiState = uiStateVar
        onPersonChanged = {
            uiStateVar = uiStateVar.copy(person = it)
        }
    }
}
