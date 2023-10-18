package com.ustadmobile.view.contententry.edit

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSwitchField
import web.cssom.px
import mui.material.*
import mui.material.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface ContentEntryEditScreenProps : Props {

    var uiState: ContentEntryEditUiState

    var onCourseBlockChange: (CourseBlock?) -> Unit

    var onClickUpdateContent: () -> Unit

    var onContentEntryChanged: (ContentEntry?) -> Unit

    var onChangeCompress: (Boolean) -> Unit

    var onChangePubliclyAccessible: (Boolean) -> Unit

    var onClickLanguage: () -> Unit

    var onSelectContainerStorageDir: (ContainerStorageDir?) -> Unit

}

val ContentEntryEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryEditUiState())

    ContentEntryEditScreenComponent {
        uiState = uiStateVal
        onContentEntryChanged = viewModel::onContentEntryChanged
    }
}

val ContentEntryEditScreenPreview = FC<Props> {

    ContentEntryEditScreenComponent {
        uiState = ContentEntryEditUiState(
            entity = ContentEntryWithBlockAndLanguage().apply {
                leaf = true
            },
            updateContentVisible = true,
            metadataResult = MetadataResult(
                entry = ContentEntryWithLanguage(),
                pluginId = 0
            ),
            courseBlockEditUiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 78
                    cbCompletionCriteria = 14
                },
            ),
            storageOptions = listOf(
                ContainerStorageDir(
                    name = "Device Memory",
                    dirUri = ""
                ),
                ContainerStorageDir(
                    name = "Memory Card",
                    dirUri = ""
                ),
            ),
            selectedContainerStorageDir = ContainerStorageDir(
                name = "Device Memory",
                dirUri = ""
            )
        )
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

            if (props.uiState.entity?.leaf == true){
                Typography {
                    + strings[MR.strings.supported_files]
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.title ?: ""
                id = "content_title"
                label = strings[MR.strings.title]
                error = props.uiState.titleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.shallowCopy {
                            title = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.description ?: ""
                id = "content_description"
                label = strings[MR.strings.description]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.shallowCopy {
                            description = it
                        }
                    )
                }
            }

            if(props.uiState.courseBlockVisible) {
                UstadCourseBlockEdit {
                    uiState = props.uiState.courseBlockEditUiState
                    onCourseBlockChange = props.onCourseBlockChange
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.author ?: ""
                label = strings[MR.strings.entry_details_author]
                id = "content_author"
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.shallowCopy {
                            author = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.publisher ?: ""
                label = strings[MR.strings.entry_details_publisher]
                id = "content_publisher"
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.shallowCopy {
                            publisher = it
                        }
                    )
                }
            }

            UstadMessageIdSelectField {
                value = props.uiState.entity?.licenseType ?: ContentEntry.LICENSE_TYPE_UNSPECIFIED
                options = LicenceConstants.LICENSE_MESSAGE_IDS
                label = strings[MR.strings.licence]
                id = "content_license"
                onChange = {
                    props.onContentEntryChanged(
                        props.uiState.entity?.shallowCopy {
                            licenseType = it.value
                        }
                    )
                }
            }

            if (props.uiState.contentCompressVisible){
                UstadSwitchField {
                    id = "content_compression_enabled"
                    checked= props.uiState.compressionEnabled
                    onChanged = { props.onChangeCompress(it) }
                    label = strings[MR.strings.compress]
                    enabled = props.uiState.fieldsEnabled
                }
            }

            UstadSwitchField {
                id = "content_publik"
                checked= props.uiState.entity?.publik ?: false
                onChanged = { props.onChangePubliclyAccessible(it) }
                label = strings[MR.strings.publicly_accessible]
                enabled = props.uiState.fieldsEnabled
            }

            UstadTextEditField {
                id = "content_language"
                value = props.uiState.entity?.language?.name ?: ""
                label = strings[MR.strings.language]
                readOnly = true
                enabled = props.uiState.fieldsEnabled
                onClick = props.onClickLanguage
                onChange = {}
            }
        }
    }
}
