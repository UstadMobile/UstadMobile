package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.PersonParentJoinConstants
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinAndMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.view.components.UstadMessageIdSelectField
import web.cssom.px
import js.core.jso
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.useState
import react.dom.html.ReactHTML.div

external interface ParentalConsentManagementScreenProps : Props {

    var uiState: ParentalConsentManagementUiState

    var onClickConsent: () -> Unit

    var onClickDoNotConsent: () -> Unit

    var onClickChangeConsent: () -> Unit

    var onChangeRelation: (PersonParentJoin?) -> Unit

}

val ParentalConsentManagementComponent2 = FC<ParentalConsentManagementScreenProps> { props ->

    val strings = useStringProvider()

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography {
                + strings[MR.strings.parent_consent_explanation]
            }

            if (props.uiState.relationshipVisible){

                UstadMessageIdSelectField {
                    value = props.uiState.parentJoinAndMinor?.personParentJoin?.ppjRelationship ?: 0
                    options = PersonParentJoinConstants.RELATIONSHIP_MESSAGE_IDS
                    label = strings[MR.strings.relationship]
                    onChange = {
                        props.onChangeRelation(
                            props.uiState.parentJoinAndMinor?.personParentJoin?.shallowCopy {
                                ppjRelationship = it.value
                            }
                        )
                    }
                }
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MR.strings.terms_and_policies]
            }


            div {
                dangerouslySetInnerHTML = jso {
                    __html = props.uiState.siteTerms?.termsHtml ?: ""
                }
            }

            if (props.uiState.consentButtonVisible){
                Button {
                    onClick = { props.onClickConsent }
                    variant = ButtonVariant.contained
                    disabled = !props.uiState.fieldsEnabled

                    + strings[MR.strings.i_consent].uppercase()
                }
            }

            if (props.uiState.dontConsentButtonVisible){
                Button {
                    onClick = { props.onClickDoNotConsent }
                    variant = ButtonVariant.outlined
                    disabled = !props.uiState.fieldsEnabled

                    + strings[MR.strings.i_do_not_consent].uppercase()
                }
            }

            if (props.uiState.changeConsentButtonVisible){
                Button {
                    onClick = { props.onClickChangeConsent }
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


