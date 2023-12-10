package com.ustadmobile.view.site.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailUiState
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.SiteTermsAndLangName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField2
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.yesOrNoString
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.UstadFab
import mui.material.*
import react.FC
import react.Props
import react.create
import mui.material.List
import react.ReactNode

import mui.icons.material.DriveFileRenameOutline as DriveFileRenameOutlineIcon
import mui.icons.material.Luggage as LuggageIcon
import mui.icons.material.HowToRegRounded as HowToRegRoundedIcon


external interface SiteDetailProps : Props {
    var uiState: SiteDetailUiState
    var onClickLang: (SiteTermsAndLangName) -> Unit
}

val SiteDetailComponent2 = FC<SiteDetailProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        List {
            UstadDetailField2 {
                leadingContent = DriveFileRenameOutlineIcon.create()
                labelContent = ReactNode(strings[MR.strings.name_key])
                valueContent = ReactNode(props.uiState.site?.siteName.toString())
            }

            UstadDetailField2 {
                leadingContent = LuggageIcon.create()
                labelContent = ReactNode(strings[MR.strings.guest_login_enabled])
                valueContent = ReactNode(strings.yesOrNoString(props.uiState.site?.guestLogin))
            }

            UstadDetailField2 {
                leadingContent = HowToRegRoundedIcon.create()
                labelContent = ReactNode(strings[MR.strings.registration_allowed])
                valueContent = ReactNode(strings.yesOrNoString(props.uiState.site?.registrationAllowed))
            }

            UstadDetailHeader {
                header = ReactNode(strings[MR.strings.terms_and_policies])
            }

            props.uiState.siteTerms.forEach { item ->
                ListItem {
                    disablePadding = true
                    ListItemButton {
                        onClick = {
                            props.onClickLang(item)
                        }
                        ListItemText {
                            + item.langDisplayName
                        }
                    }
                }
            }
        }
    }
}

val SiteDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SiteDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(SiteDetailUiState())

    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }

    SiteDetailComponent2 {
        uiState = uiStateVal
    }

}


@Suppress("unused")
val SiteDetailPreview = FC<Props> {
    SiteDetailComponent2 {
        uiState = SiteDetailUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsAndLangName(
                    terms = SiteTerms(),
                    langDisplayName = "Polish",
                )
            )
        )
    }
}
