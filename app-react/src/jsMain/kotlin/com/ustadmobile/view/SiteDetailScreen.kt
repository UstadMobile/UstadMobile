package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import web.cssom.px
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

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField {
                icon = AccountBalanceRounded.create()
                labelText = strings[MR.strings.name_key]
                valueText = ReactNode(props.uiState.site?.siteName.toString())
            }

            UstadDetailField {
                icon = ManageSearchOutlined.create()
                labelText = strings[MR.strings.guest_login_enabled]
                valueText = ReactNode(props.uiState.site?.guestLogin.toString())
            }

            UstadDetailField {
                icon = HowToRegRounded.create()
                labelText = strings[MR.strings.registration_allowed]
                valueText = ReactNode(props.uiState.site?.registrationAllowed.toString())
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MR.strings.terms_and_policies]
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