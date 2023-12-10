package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.site.edit.SiteEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadSwitchField
import web.cssom.px
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.create

external interface SiteEditProps: Props {
    var uiState: SiteEditUiState
    var onSiteChanged: (Site?) -> Unit
    var onClickLang: (SiteTermsWithLanguage) -> Unit
    var onDeleteClick: (SiteTermsWithLanguage) -> Unit
    var onClickAddItem: () -> Unit
}

val SiteEditComponent2 = FC<SiteEditProps> { props ->

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.site?.siteName ?: ""
                label = strings[MR.strings.first_names]
                error = props.uiState.siteNameError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy {
                            siteName = it
                        })
                }
            }

            UstadSwitchField{
                label = strings[MR.strings.guest_login_enabled]
                checked = props.uiState.site?.guestLogin ?: true
                onChanged = {
                    props.onSiteChanged(props.uiState.site?.shallowCopy{
                        guestLogin = it
                    })
                }
            }

            UstadSwitchField{
                label = strings[MR.strings.registration_allowed]
                checked = props.uiState.site?.registrationAllowed ?: true
                onChanged = {
                    props.uiState.site?.shallowCopy {
                        registrationAllowed = it
                    }
                }
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MR.strings.terms_and_policies]
            }

            List{
                ListItem {
                    disablePadding = true

                    ListItemButton {
                        onClick = {
                            props.onClickAddItem()
                        }

                        ListItemIcon {
                            Add{}
                        }

                        ListItemText {
                            + (strings[MR.strings.terms_and_policies])
                        }
                    }
                }

                props.uiState.siteTerms.forEach { item ->
                    ListItem {
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            onClick = {
                                props.onDeleteClick(item)
                            }
                            Delete {}
                        }

                        ListItemButton {
                            ListItemIcon {
                                UstadBlankIcon { }
                            }

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

val SiteEditPreview = FC<Props> {
    SiteEditComponent2{
        uiState = SiteEditUiState(
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