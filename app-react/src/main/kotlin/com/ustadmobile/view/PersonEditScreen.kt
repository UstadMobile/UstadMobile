package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.useViewModel
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants.GENDER_MESSAGE_IDS_AND_UNSET
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.PersonEditUiState
import com.ustadmobile.core.viewmodel.PersonEditViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadSelectField
import mui.system.Container
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface PersonEditScreenProps : Props{
    var uiState: PersonEditUiState

    var onPersonChanged: (PersonWithAccount?) -> Unit

    var onApprovalPersonParentJoinChanged: (PersonParentJoin?) -> Unit
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
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            firstNames = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.person?.lastName ?: ""
                label = strings[MessageID.last_name]
                error = props.uiState.lastNameError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            lastName = it
                    })
                }
            }

            UstadSelectField<MessageIdOption2> {
                value = props.uiState.person?.gender?.toString() ?: Person.GENDER_UNSET.toString()
                options = GENDER_MESSAGE_IDS_AND_UNSET
                label = strings[MessageID.gender_literal]
                id = (props.uiState.person?.gender ?: 0).toString()
                itemValue = { it.value.toString() }
                itemLabel = { ReactNode(if(it.messageId == 0) " " else strings[it.messageId]) }
                onChange = { messageIdOpt ->
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            gender = messageIdOpt.value
                    })
                }
            }

            if (props.uiState.parentalEmailVisible){
                UstadTextEditField {
                    value = props.uiState.approvalPersonParentJoin?.ppjEmail ?: ""
                    label = strings[MessageID.parents_email_address]
                    error = props.uiState.parentContactError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onApprovalPersonParentJoinChanged(
                            props.uiState.approvalPersonParentJoin?.shallowCopy {
                                ppjEmail = it
                            })
                    }
                }
            }

            UstadDateEditField {
                timeInMillis = props.uiState.person?.dateOfBirth ?: 0
                label = strings[MessageID.birthday]
                timeZoneId = UstadMobileConstants.UTC
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            dateOfBirth = it
                    })
                }
            }

            UstadTextEditField {
                error = props.uiState.parentContactError
                enabled = props.uiState.fieldsEnabled
                label = strings[MessageID.parents_email_address]
                onChange = {
                    props.onApprovalPersonParentJoinChanged(
                        props.uiState.approvalPersonParentJoin?.shallowCopy {
                            ppjEmail = it
                        })
                }
            }

            UstadTextEditField {
                value = props.uiState.person?.phoneNum ?: ""
                label = strings[MessageID.phone_number]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            phoneNum = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.person?.emailAddr ?: ""
                label = strings[MessageID.email]
                error = props.uiState.emailError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            emailAddr = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.person?.personAddress ?: ""
                label = strings[MessageID.address]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            personAddress = it
                    })
                }
            }

            if (props.uiState.usernameVisible){
                UstadTextEditField {
                    value = props.uiState.person?.username ?: ""
                    label = strings[MessageID.username]
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onPersonChanged(
                            props.uiState.person?.shallowCopy {
                                username = it
                        })
                    }
                }
            }

            if (props.uiState.passwordVisible){
                UstadTextEditField {
                    value = props.uiState.person?.newPassword ?: ""
                    label = strings[MessageID.password]
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onPersonChanged(
                            props.uiState.person?.shallowCopy {
                                newPassword = it
                        })
                    }
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
                phoneNum = "0799999"
                emailAddr = "Bob@gmail.com"
                gender = 1
                username = "Bob12"
                dateOfBirth = 0
                personOrgId = "123"
                personAddress = "Herat"
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

val PersonEditScreen = FC<UstadScreenProps> { props ->
    val viewModel = useViewModel(
        onAppUiStateChange = props.onAppUiStateChanged
    ) { di, savedStateHandle ->
        PersonEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(PersonEditUiState())

    PersonEditComponent2 {
        uiState = uiStateVar
        onPersonChanged = viewModel::onEntityChanged
        onApprovalPersonParentJoinChanged = viewModel::onApprovalPersonParentJoinChanged
    }
}
