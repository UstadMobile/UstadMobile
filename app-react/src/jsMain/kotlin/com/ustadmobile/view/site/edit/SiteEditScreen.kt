package com.ustadmobile.view.site.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.viewmodel.site.edit.SiteEditUiState
import com.ustadmobile.core.viewmodel.site.edit.SiteEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadLanguageSelect
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadEditHeader
import com.ustadmobile.view.components.UstadSwitchField
import com.ustadmobile.wrappers.quill.ReactQuill
import web.cssom.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import kotlinx.coroutines.Dispatchers

external interface SiteEditProps: Props {
    var uiState: SiteEditUiState
    var onSiteChanged: (Site?) -> Unit
    var onChangeTermsLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit
    var onChangeTermsHtml: (String) -> Unit
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
                id = "site_name"
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
                id = "guest_login_enabled"
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
                id = "registration_allowed"
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

            UstadLanguageSelect {
                langList = props.uiState.uiLangs
                currentLanguage = props.uiState.currentSiteTermsLang
                onItemSelected = props.onChangeTermsLanguage
                fullWidth = true
                id = "terms_lang_select"
                label = ReactNode(strings[MR.strings.language])
                disabled = !props.uiState.fieldsEnabled
            }

            ReactQuill {
                value = props.uiState.currentSiteTermsHtml ?: ""
                id = "terms_html_edit"
                placeholder = strings[MR.strings.terms_and_policies]
                onChange = props.onChangeTermsHtml
            }
        }
    }

}

val SiteEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SiteEditViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(SiteEditUiState(), Dispatchers.Main.immediate)

    SiteEditComponent2 {
        uiState = uiStateVal
        onSiteChanged = viewModel::onEntityChanged
        onChangeTermsHtml = viewModel::onChangeTermsHtml
        onChangeTermsLanguage = viewModel::onChangeTermsLanguage
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
