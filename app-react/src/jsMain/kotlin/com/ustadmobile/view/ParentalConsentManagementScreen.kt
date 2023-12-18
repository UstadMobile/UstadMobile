package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.PersonParentJoinConstants
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementUiState
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.hooks.useFormattedDate
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinAndMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadRawHtml
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.UstadMessageIdSelectField
import kotlinx.datetime.TimeZone
import web.cssom.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface ParentalConsentManagementScreenProps : Props {

    var uiState: ParentalConsentManagementUiState

    var onClickConsent: () -> Unit

    var onClickDoNotConsent: () -> Unit

    var onClickChangeConsent: () -> Unit

    var onChangeRelation: (PersonParentJoin?) -> Unit

}

val ParentalConsentManagementComponent2 = FC<ParentalConsentManagementScreenProps> { props ->

    val strings = useStringProvider()
    val minorDateOfBirth = useFormattedDate(
        timeInMillis = props.uiState.parentJoinAndMinor?.minorPerson?.dateOfBirth ?: 0L,
        timezoneId = TimeZone.UTC.id
    )
    val statusDate = useFormattedDateAndTime(
        props.uiState.parentJoinAndMinor?.personParentJoin?.ppjApprovalTiemstamp ?: 0L,
        timezoneId = TimeZone.currentSystemDefault().id
    )
    

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            if(props.uiState.consentStatusVisible) {
                props.uiState.consentStatusText?.also { consentStatusText ->
                    +strings.format(consentStatusText, statusDate)
                }

                Divider()
            }

            UstadRawHtml {
                html = strings.format(MR.strings.parent_consent_explanation,
                    props.uiState.parentJoinAndMinor?.minorPerson?.fullName() ?: "",
                    minorDateOfBirth,
                    props.uiState.appName).replace("\\n", "<br/>")
            }

            if (props.uiState.relationshipVisible){

                UstadMessageIdSelectField {
                    value = props.uiState.parentJoinAndMinor?.personParentJoin?.ppjRelationship ?: 0
                    options = PersonParentJoinConstants.RELATIONSHIP_MESSAGE_IDS
                    label = strings[MR.strings.relationship] + "*"
                    onChange = {
                        props.onChangeRelation(
                            props.uiState.parentJoinAndMinor?.personParentJoin?.shallowCopy {
                                ppjRelationship = it.value
                            }
                        )
                    }
                    error = props.uiState.relationshipError
                    helperText = ReactNode(props.uiState.relationshipError ?: strings[MR.strings.required])
                }
            }

            UstadDetailHeader {
                header = ReactNode(strings[MR.strings.terms_and_policies])
            }

            Divider()

            UstadRawHtml {
                html = props.uiState.siteTerms?.termsHtml ?: ""
            }

            Divider()

            if (props.uiState.consentButtonVisible){
                Button {
                    onClick = { props.onClickConsent() }
                    variant = ButtonVariant.contained
                    disabled = !props.uiState.fieldsEnabled

                    + strings[MR.strings.i_consent].uppercase()
                }
            }

            if (props.uiState.dontConsentButtonVisible){
                Button {
                    onClick = { props.onClickDoNotConsent() }
                    variant = ButtonVariant.outlined
                    disabled = !props.uiState.fieldsEnabled

                    + strings[MR.strings.i_do_not_consent].uppercase()
                }
            }

            if (props.uiState.changeConsentButtonVisible){
                Button {
                    onClick = { props.onClickChangeConsent() }
                    variant = ButtonVariant.contained
                    disabled = !props.uiState.fieldsEnabled

                    props.uiState.changeConsentLabel?.also {
                        +strings[it]
                    }
                }
            }
        }
    }
}

val ParentalConsentManagementScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ParentalConsentManagementViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(
        ParentalConsentManagementUiState()
    )

    ParentalConsentManagementComponent2 {
        uiState = uiStateVal
        onChangeRelation = viewModel::onEntityChanged
        onClickConsent = viewModel::onClickConsent
        onClickDoNotConsent = viewModel::onClickDontConsent
        onClickChangeConsent = viewModel::onClickChangeConsent
    }

}

@Suppress("unused")
val ParentalConsentManagementPreview = FC<Props> {

    val uiStateVar : ParentalConsentManagementUiState by useState {
        ParentalConsentManagementUiState(
            siteTerms = SiteTerms().apply {
                termsHtml = "hello <b>world</b>"
            },
            parentJoinAndMinor = PersonParentJoinAndMinorPerson().apply {

                minorPerson = Person().apply {
                    firstNames = "Pit"
                    lastName = "The Younger"
                }
            },
            fieldsEnabled = true
        )
    }

    ParentalConsentManagementComponent2 {
        uiState = uiStateVar
    }
}


