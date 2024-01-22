package com.ustadmobile.view.contententry.importlink

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkUiState
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useRequiredContext

external interface ContentEntryImportLinkProps: Props {
    var uiState: ContentEntryImportLinkUiState
    var onClickNext: () -> Unit
    var onUrlChange: (String) -> Unit
}

val ContentEntryImportLinkComponent2 = FC<ContentEntryImportLinkProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {

        Stack{
            val theme by useRequiredContext(ThemeContext)

            direction = responsive(StackDirection.column)
            spacing = responsive(theme.spacing(1))

            UstadTextField {
                id = "import_url"
                value = props.uiState.url
                label = ReactNode(strings[MR.strings.enter_url])
                error = props.uiState.linkError != null
                disabled = !props.uiState.fieldsEnabled
                helperText = props.uiState.linkError?.let { ReactNode(it) }
                onTextChange = {
                    props.onUrlChange(it)
                }
                onKeyUp = {
                    if(it.key == "Enter") {
                        props.onClickNext()
                    }
                }
            }

            Typography {
                sx {
                    paddingLeft = theme.spacing(1)
                    paddingRight = theme.spacing(1)
                }
                variant = TypographyVariant.body2
                + strings[MR.strings.supported_link]
            }
        }
    }

}


val ContentEntryImportLinkScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryImportLinkViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryImportLinkUiState())

    ContentEntryImportLinkComponent2 {
        uiState = uiStateVal
        onUrlChange = viewModel::onChangeLink
        onClickNext = viewModel::onClickNext
    }

}
