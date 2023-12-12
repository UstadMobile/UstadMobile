package com.ustadmobile.view.site.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.site.edit.SiteEditUiState
import com.ustadmobile.core.viewmodel.site.edit.SiteEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadEditHeader
import com.ustadmobile.view.components.UstadSwitchField
import web.cssom.px
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
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

    UstadStandardContainer {
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)

            UstadTextField {
                value = props.uiState.site?.siteName ?: ""
                label = ReactNode(strings[MR.strings.name_key] + "*")
                error = props.uiState.siteNameError != null
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy {
                            siteName = it
                        }
                    )
                }
                helperText = ReactNode(props.uiState.siteNameError ?: strings[MR.strings.required])
            }

            UstadSwitchField {
                label = strings[MR.strings.guest_login_enabled]
                checked = props.uiState.site?.guestLogin ?: false
                onChanged = {
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy{
                            guestLogin = it
                        }
                    )
                }
            }

            UstadSwitchField {
                label = strings[MR.strings.registration_allowed]
                checked = props.uiState.site?.registrationAllowed ?: false
                onChanged = {
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy {
                            registrationAllowed = it
                        }
                    )
                }
            }

            UstadEditHeader {
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

val SiteEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SiteEditViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(SiteEditUiState())

    SiteEditComponent2 {
        uiState = uiStateVal
        onSiteChanged = viewModel::onEntityChanged
    }

}

@Suppress("unused")
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
