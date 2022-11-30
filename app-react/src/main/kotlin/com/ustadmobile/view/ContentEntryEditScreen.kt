package com.ustadmobile.view

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.viewmodel.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.AlignItems
import csstype.px
import dom.html.HTMLInputElement
import mui.material.*
import mui.material.Stack
import mui.material.Switch
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.dom.events.ChangeEvent

external interface ContentEntryEditScreenProps : Props {

    var uiState: ContentEntryEditUiState

    var onCourseBlockChange: (CourseBlock?) -> Unit

    var onClickUpdateContent: () -> Unit

    var onContentChanged: (ContentEntryWithBlockAndLanguage?) -> Unit

    var onChangeCompress: (ChangeEvent<HTMLInputElement>, Boolean) -> Unit

    var onChangePubliclyAccessible: (ChangeEvent<HTMLInputElement>, Boolean) -> Unit

    var onClickLanguage: () -> Unit

}

val ContentEntryEditScreenPreview = FC<Props> {
    val strings = useStringsXml()
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
                minScoreVisible = true,
                gracePeriodVisible = true,
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

            UstadTextEditField {
                value = props.uiState.entity?.title ?: ""
                label = strings[MessageID.title]
                error = props.uiState.titleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            title = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.description ?: ""
                label = strings[MessageID.description]
                enabled = props.uiState.fieldsEnabled
                onChange = {
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

            UstadTextEditField {
                value = props.uiState.entity?.author ?: ""
                label = strings[MessageID.entry_details_author]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            author = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.publisher ?: ""
                label = strings[MessageID.entry_details_publisher]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            publisher = it
                        }
                    )
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.entity?.licenseType ?: 0
                options = LicenceConstants.LICENSE_MESSAGE_IDS
                label = strings[MessageID.licence]
                id = (props.uiState.entity?.licenseType ?: 0).toString()
                onChange = {
                    props.onContentChanged(
                        props.uiState.entity?.shallowCopy {
                            licenseType = it?.value ?: 0
                        }
                    )
                }
            }

            if (props.uiState.containerStorageOptionVisible){
                UstadMessageIdDropDownField {
                    value = props.uiState.entity?.licenseType ?: 0
                    options = PersonConstants.GENDER_MESSAGE_IDS
                    label = strings[MessageID.content_creation_storage_option_title]
                    id = (props.uiState.entity?.licenseType ?: 0).toString()
                    onChange = {
                        props.onContentChanged(
                            props.uiState.entity?.shallowCopy {
                                licenseType = it?.value ?: 0
                            }
                        )
                    }
                }
            }

            if (props.uiState.contentCompressVisible){
                SwitchRow {
                    text = strings[MessageID.compress]
                    checked = props.uiState.compressionEnabled
                    onChange = props.onChangeCompress
                }
            }

            SwitchRow {
                text = strings[MessageID.publicly_accessible]
                checked = props.uiState.entity?.publik ?: false
                onChange = props.onChangePubliclyAccessible
            }

            UstadTextEditField {
                value = props.uiState.entity?.language?.name ?: ""
                label = strings[MessageID.language]
                readOnly = true
                enabled = props.uiState.fieldsEnabled
                onClick = props.onClickLanguage
                onChange = {}
            }
        }
    }
}

external interface SwitchRowProps : Props {

    var text: String

    var checked: Boolean

    var onChange: (ChangeEvent<HTMLInputElement>, Boolean) -> Unit

}

private val SwitchRow = FC<SwitchRowProps> { props ->

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(20.px)
        sx {
          alignItems = AlignItems.center
        }

        Typography {
            + props.text
        }

        Switch {
            checked= props.checked
            onChange = props.onChange
        }
    }
}