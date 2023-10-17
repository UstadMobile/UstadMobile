package com.ustadmobile.view.contententry.importlink

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkUiState
import com.ustadmobile.mui.components.UstadTextEditField
import web.cssom.px
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props

external interface ContentEntryImportLinkProps: Props {
    var uiState: ContentEntryImportLinkUiState
    var onClickNext: () -> Unit
    var onUrlChange: (String?) -> Unit
}

val ContentEntryImportLinkComponent2 = FC<ContentEntryImportLinkProps> { props ->

    val strings = useStringProvider()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(16.px)

            UstadTextEditField {
                value = props.uiState.url.toString()
                label = strings[MR.strings.enter_url]
                error = props.uiState.linkError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onUrlChange(it)
                }
            }

            Typography {
                sx {
                    paddingLeft = 16.px
                    paddingRight = 16.px
                }
                variant = TypographyVariant.body2
                + strings[MR.strings.supported_link]
            }

            Button {

                onClick = { props.onClickNext }
                variant = ButtonVariant.contained

                + strings[MR.strings.next]
            }

        }
    }

}

val ContentEntryImportLinkScreenPreview = FC<Props> {
    ContentEntryImportLinkComponent2{
        uiState = ContentEntryImportLinkUiState(
            url = "site.com/dir"
        )
    }
}