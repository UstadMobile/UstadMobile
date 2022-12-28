package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ContentEntryImportLinkUiState
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import io.ktor.http.*
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

val ContentEntryImportLinkComponent2 = FC<ContentEntryImportLinkProps> {props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(16.px)

            UstadTextEditField {
                value = props.uiState.url.toString()
                label = strings[MessageID.enter_url]
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
                + strings[MessageID.supported_link]
            }

            Button {

                onClick = { props.onClickNext }
                variant = ButtonVariant.contained

                + strings[MessageID.next]
            }

        }
    }

}

val ContentEntryImportLinkScreenPreview = FC<Props> {
    ContentEntryImportLinkComponent2{
        uiState = ContentEntryImportLinkUiState(
            url = "this is a link for example"
        )
    }
}