package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.viewmodel.SiteEnterLinkUiState
import com.ustadmobile.lib.db.entities.Site
import react.dom.html.ReactHTML.img
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.icons.material.Add
import mui.material.*
import mui.system.Container
import mui.system.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useState

external interface SiteEnterLinkProps : Props {
    var uiState: SiteEnterLinkUiState

    var onClickNext: () -> Unit

    var onClickNewLearningEnvironment: () -> Unit

    var onEditTextValueChange: (Site?) -> Unit
}

val SiteEnterLinkComponent2 = FC <SiteEnterLinkProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)


            img {
                src = "${"img/illustration_connect.svg"}?fit=crop&auto=format"
                alt = "illustration connect"
                height = 300.0
            }

            Typography {
                + strings[MessageID.please_enter_the_linK]
            }

            UstadTextEditField {
                value = props.uiState.site?.siteName
                label = strings[MessageID.site_link]
                error = props.uiState.linkError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onEditTextValueChange(
                        props.uiState.site?.shallowCopy {
                            siteName = it
                        })
                }
            }

            Box{
                sx {
                    height = 20.px
                }
            }

            Button {
                onClick = { props.onClickNext }
                variant = ButtonVariant.contained

                + strings[MessageID.next]
            }

            Typography {
                align = TypographyAlign.center
                + strings[MessageID.or].uppercase()
            }

            Button {
                onClick = { props.onClickNewLearningEnvironment }
                variant = ButtonVariant.text

                startIcon = Add.create()

                + strings[MessageID.create_a_new_learning_env].uppercase()
            }
        }
    }
}

val SiteEnterLinkScreenPreview = FC<Props> {

    val uiStateVar by useState {
        SiteEnterLinkUiState()
    }

    SiteEnterLinkComponent2 {
        uiState = uiStateVar
    }
}