package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SiteDetailUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.create
import mui.material.List
import react.ReactNode

import mui.icons.material.AccountBalanceRounded
import mui.icons.material.HowToRegRounded
import mui.icons.material.ManageSearchOutlined


external interface SiteDetailProps : Props {
    var uiState: SiteDetailUiState
    var onClickLang: (SiteTermsWithLanguage) -> Unit
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
                valueText = ReactNode(props.uiState.site?.siteName.toString())
            }

            UstadDetailField {
                icon = ManageSearchOutlined.create()
                labelText = strings[MessageID.guest_login_enabled]
                valueText = ReactNode(props.uiState.site?.guestLogin.toString())
            }

            UstadDetailField {
                icon = HowToRegRounded.create()
                labelText = strings[MessageID.registration_allowed]
                valueText = ReactNode(props.uiState.site?.registrationAllowed.toString())
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.terms_and_policies]
            }

            List {
                props.uiState.siteTerms.forEach { item ->
                    ListItem {
                        disablePadding = true
                        ListItemButton {
                            onClick = {
                                props.onClickLang(item)
                            }
                            ListItemText {
                                + (item.stLanguage?.name ?: "")
                            }
                        }
                    }
                }
            }
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