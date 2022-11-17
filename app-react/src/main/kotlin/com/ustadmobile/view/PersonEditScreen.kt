package com.ustadmobile.view

import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants.GENDER_MESSAGE_IDS
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.PersonEditUiState
import com.ustadmobile.lib.db.entities.PersonParentJoin
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

            UstadMessageIdDropDownField {
                value = props.uiState.person?.gender ?: 0
                options = GENDER_MESSAGE_IDS
                label = strings[MessageID.gender_literal]
                id = (props.uiState.person?.gender ?: 0).toString()
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            gender = it?.value ?: 0
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
                dateOfBirth = 12
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
