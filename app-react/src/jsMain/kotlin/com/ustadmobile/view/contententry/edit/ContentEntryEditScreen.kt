package com.ustadmobile.view.contententry.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.contententry.stringResource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.wrappers.quill.ReactQuill
import kotlinx.coroutines.Dispatchers
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.FormControl
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Select
import mui.material.Stack
import mui.material.TextField
import mui.material.Typography
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import web.cssom.px

external interface ContentEntryEditScreenProps : Props {

    var uiState: ContentEntryEditUiState

    var onClickUpdateContent: () -> Unit

    var onContentEntryChanged: (ContentEntry?) -> Unit

    var onSetCompressionLevel: (CompressionLevel) -> Unit

}

val ContentEntryEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(
        ContentEntryEditUiState(), Dispatchers.Main.immediate
    )

    ContentEntryEditScreenComponent {
        uiState = uiStateVal
        onContentEntryChanged = viewModel::onContentEntryChanged
        onSetCompressionLevel = viewModel::onSetCompressionLevel
    }
}

private val ContentEntryEditScreenComponent = FC<ContentEntryEditScreenProps> { props ->

    val strings = useStringProvider()
    val updateContentText =
        if (!props.uiState.importError.isNullOrBlank())
            strings[MR.strings.file_required_prompt]
        else
            strings[MR.strings.file_selected]

    UstadStandardContainer {

        Stack {
            spacing = responsive(20.px)

            if (props.uiState.updateContentVisible){
                Button {
                    onClick = { props.onClickUpdateContent }
                    variant = ButtonVariant.contained

                    + strings[MR.strings.update_content].uppercase()
                }

                Typography {
                    + updateContentText
                }
            }

            if (props.uiState.entity?.entry?.leaf == true){
                Typography {
                    + strings[MR.strings.supported_files]
                }
            }


            TextField {
                value = props.uiState.entity?.entry?.title ?: ""
                id = "content_title"
                label = ReactNode(strings[MR.strings.title]  + "*")
                helperText = ReactNode(props.uiState.titleError ?: strings[MR.strings.required])
                error = props.uiState.titleError != null
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.entry?.shallowCopy {
                            title = it
                        }
                    )
                }
            }



            ReactQuill {
                value = props.uiState.entity?.entry?.description ?: ""
                id = "description_quill"
                placeholder = strings[MR.strings.description]
                readOnly = !props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.entry?.shallowCopy {
                            description = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.entry?.author ?: ""
                label = strings[MR.strings.entry_details_author]
                id = "content_author"
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.entry?.shallowCopy {
                            author = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.entry?.publisher ?: ""
                label = strings[MR.strings.entry_details_publisher]
                id = "content_publisher"
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.entry?.shallowCopy {
                            publisher = it
                        }
                    )
                }
            }

            UstadMessageIdSelectField {
                value = props.uiState.entity?.entry?.licenseType ?: ContentEntry.LICENSE_TYPE_UNSPECIFIED
                options = LicenceConstants.LICENSE_MESSAGE_IDS
                label = strings[MR.strings.licence]
                id = "content_license"
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.entry?.shallowCopy {
                            licenseType = it.value
                        }
                    )
                }
            }

            FormControl {
                fullWidth = true

                InputLabel {
                    id = "compression_label"
                    + strings[MR.strings.compression]
                }

                Select {
                    value = props.uiState.entity?.contentJobItem?.cjiCompressionLevel?.toString()
                        ?: CompressionLevel.MEDIUM.value.toString()
                    id = "compression"
                    labelId = "compression_label"
                    label = ReactNode(strings[MR.strings.compression])
                    disabled = !props.uiState.fieldsEnabled
                    fullWidth = true
                    onChange = { event, _ ->
                        val selectedVal = ("" + event.target.value)
                        selectedVal.toIntOrNull()?.also {
                            props.onSetCompressionLevel(CompressionLevel.forValue(it))
                        }
                    }

                    CompressionLevel.entries.forEach {
                        MenuItem {
                            value = it.value.toString()
                            + strings[it.stringResource]
                        }
                    }
                }
            }
        }
    }
}
