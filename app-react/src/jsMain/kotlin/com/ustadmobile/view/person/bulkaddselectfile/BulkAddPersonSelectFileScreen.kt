package com.ustadmobile.view.person.bulkaddselectfile

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.person.bulkaddselectfile.BulkAddPersonSelectFileUiState
import com.ustadmobile.core.viewmodel.person.bulkaddselectfile.BulkAddPersonSelectFileViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import emotion.react.css
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.input
import react.useRef
import web.cssom.Display
import web.html.HTMLInputElement
import web.html.InputType
import web.url.URL
import mui.icons.material.InsertDriveFile as InsertDriveFileIcon
import com.ustadmobile.core.MR
import com.ustadmobile.mui.components.ThemeContext
import mui.material.Button
import mui.material.ButtonVariant
import react.useRequiredContext


external interface BulkAddPersonSelectFileProps: Props {
    var uiState: BulkAddPersonSelectFileUiState
    var onClickSelectFile: () -> Unit
    var onClickImportButton : () -> Unit
    var onClickGetTemplate: () -> Unit
}

val BulkAddPersonSelectFileComponent = FC<BulkAddPersonSelectFileProps> { props ->
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(theme.spacing(2))

            ListItem {
                ListItemIcon {
                    InsertDriveFileIcon()
                }

                ListItemText {
                    primary = ReactNode(props.uiState.selectedFileName ?: strings[MR.strings.none_key])
                    secondary = ReactNode(strings[MR.strings.file_selected])
                }
            }

            Button {
                id = "select_file_button"
                variant = ButtonVariant.outlined
                fullWidth = true
                disabled = !props.uiState.fieldsEnabled

                onClick = {
                    props.onClickSelectFile()
                }

                + strings[MR.strings.select_file]
            }

            Button {
                id = "get_template_button"
                variant = ButtonVariant.outlined
                fullWidth = true
                disabled = !props.uiState.fieldsEnabled

                onClick = {
                    props.onClickGetTemplate()
                }

                + strings[MR.strings.get_template]
            }

            Button {
                id = "import_button"
                variant = ButtonVariant.contained
                fullWidth = true
                disabled = !props.uiState.importButtonEnabled

                onClick = {
                    props.onClickImportButton()
                }

                + strings[MR.strings.import_key]
            }
        }
    }
}

val BulkAddPersonSelectFileScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        BulkAddPersonSelectFileViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(BulkAddPersonSelectFileUiState())

    val fileInputRef = useRef<HTMLInputElement>(null)


    input {
        ref = fileInputRef
        type = InputType.file
        id = "bulk_import_file"
        accept = ".csv,text/csv"

        css {
            display = "none".unsafeCast<Display>()
        }

        onChange = {
            it.target.files?.item(0)?.also { file ->
                viewModel.onFileSelected(URL.createObjectURL(file), file.name)
            }
        }
    }

    BulkAddPersonSelectFileComponent {
        uiState = uiStateVal
        onClickSelectFile = {
            fileInputRef.current?.click()
        }
        onClickImportButton = viewModel::onClickImportButton
        onClickGetTemplate = viewModel::onClickGetTemplate
    }
}

