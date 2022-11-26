package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.core.viewmodel.SiteDetailUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML

external interface SiteDetailProps : Props {
    var uiState: SiteDetailUiState
    var onClickLang: () -> Unit
}

val SiteDetailComponent2 = FC<SiteDetailProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField {
                icon = AccountBalanceRounded.create()
                labelText = strings[MessageID.name]
                valueText = props.uiState.site?.siteName.toString()
            }

            UstadDetailField {
                icon = ManageSearchOutlined.create()
                labelText = "Guest login enabled"
                valueText = props.uiState.site?.guestLogin.toString()
            }

            UstadDetailField {
                icon = HowToRegRounded.create()
                labelText = "Registration allowed"
                valueText = props.uiState.site?.registrationAllowed.toString()
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.terms_and_policies]
            }

            props.uiState.siteTerms.forEach {
                Button {
                    onClick = { props.onClickLang }
                    variant = ButtonVariant.outlined
                    + it.stLanguage.toString()
                }
            }

//            ReactHTML.img {
//                src = "${""}?w=164&h=164&fit=crop&auto=format"
//                alt = "user image"
//            }

//            Divider { orientation = Orientation.horizontal }

//            Typography {
//                variant = TypographyVariant.h6
//                + strings[MessageID.basic_details]
//            }

//            Divider { orientation = Orientation.horizontal }

//            Typography {
//                variant = TypographyVariant.h6
//                + strings[MessageID.contact_details]
//            }

//            Divider { orientation = Orientation.horizontal }
        }
    }
}

val SiteDetailPreview = FC<Props> {
    SiteDetailComponent2 {
        uiState = SiteDetailUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsWithLanguage().apply {
                    stLanguage = Language().apply {
                        name = "fa"
                    }
                }
            )
        )
    }
}