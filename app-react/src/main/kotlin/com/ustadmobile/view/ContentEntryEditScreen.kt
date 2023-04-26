package com.ustadmobile.view

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.viewmodel.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.inputCursor
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadDropDownField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSwitchField
import csstype.Cursor
import csstype.px
import js.core.jso
import mui.material.Container
import mui.material.TextField
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Typography
import mui.material.FormControlVariant
import mui.material.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange

external interface ContentEntryEditScreenProps : Props {

    var uiState: ContentEntryEditUiState

    var onCourseBlockChange: (CourseBlock?) -> Unit

    var onClickUpdateContent: () -> Unit

    var onContentChanged: (ContentEntryWithBlockAndLanguage?) -> Unit

    var onChangeCompress: (Boolean) -> Unit

    var onChangePubliclyAccessible: (Boolean) -> Unit

    var onClickLanguage: () -> Unit

    var onSelectContainerStorageDir: (ContainerStorageDir?) -> Unit

}

val ContentEntryEditScreenPreview = FC<Props> {

    ContentEntryEditScreenComponent2 {
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
                gracePeriodVisible = true,
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

private val ContentEntryEditScreenComponent2 = FC<ContentEntryEditScreenProps> { props ->

    val strings = useStringsXml()
    val updateContentText =
        if (!props.uiState.importError.isNullOrBlank())
            strings[MessageID.file_required_prompt]
        else
            strings[MessageID.file_selected]

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            if (props.uiState.updateContentVisible){
                Button {
                    onClick = { props.onClickUpdateContent }
                    variant = ButtonVariant.contained

                    + strings[MessageID.update_content].uppercase()
                }

                Typography {
                    + updateContentText
                }
            }

            if (props.uiState.entity?.leaf == true){
                Typography {
                    + strings[MessageID.supported_files]
                }
            }

            TextField {
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.title ?: ""
                label = ReactNode(strings[MessageID.title])
                error = props.uiState.titleError != null
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            title = it
                        }
                    )
                }
            }

            TextField {
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.description ?: ""
                label = ReactNode(strings[MessageID.description])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            description = it
                        }
                    )
                }
            }

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onCourseBlockChange
            }

            TextField {
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.author ?: ""
                label = ReactNode(strings[MessageID.entry_details_author])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            author = it
                        }
                    )
                }
            }

            TextField {
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.publisher ?: ""
                label = ReactNode(strings[MessageID.entry_details_publisher])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            publisher = it
                        }
                    )
                }
            }

            UstadMessageIdSelectField {
                value = props.uiState.entity?.licenseType ?: 0
                options = LicenceConstants.LICENSE_MESSAGE_IDS
                label = strings[MessageID.licence]
                id = (props.uiState.entity?.licenseType ?: 0).toString()
                onChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            licenseType = it.value
                        }
                    )
                }
            }

            if (props.uiState.containerStorageOptionVisible){
                UstadDropDownField {
                    value = props.uiState.selectedContainerStorageDir
                    label = strings[MessageID.content_creation_storage_option_title]
                    options = props.uiState.storageOptions
                    itemLabel = { ReactNode((it as? ContainerStorageDir)?.name ?: "") }
                    itemValue = { (it as? ContainerStorageDir)?.name ?: "" }
                    onChange = {
                        props.onSelectContainerStorageDir(it as? ContainerStorageDir)
                    }
                }
            }

            if (props.uiState.contentCompressVisible){
                UstadSwitchField {
                    checked= props.uiState.compressionEnabled ?: false
                    onChanged = { props.onChangeCompress(it) }
                    label = strings[MessageID.compress]
                    enabled = props.uiState.fieldsEnabled
                }
            }

            UstadSwitchField {
                checked= props.uiState.entity?.publik ?: false
                onChanged = { props.onChangePubliclyAccessible(it) }
                label = strings[MessageID.publicly_accessible]
                enabled = props.uiState.fieldsEnabled
            }

            TextField {
                sx {
                    inputCursor = Cursor.pointer
                }
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.language?.name ?: ""
                label = ReactNode(strings[MessageID.language])
                disabled = !props.uiState.fieldsEnabled
                inputProps = jso {
                    readOnly = true
                }
                onClick = {
                    props.onClickLanguage()
                }
                onChange = {}
            }
        }
    }
}
