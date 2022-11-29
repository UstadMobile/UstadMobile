package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.viewmodel.ContentEntryEditUiState
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.material.*
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props

external interface ContentEntryEditScreenProps : Props {

    var uiState: ContentEntryEditUiState

    var onClickUpdateContent: () -> Unit

    var onContentChanged: (ContentEntryWithBlockAndLanguage?) -> Unit
}

val ContentEntryEditScreenPreview = FC<Props> {
    val strings = useStringsXml()
    ContentEntryEditScreenComponent2 {
        uiState = ContentEntryEditUiState(
            updateContentVisible = true
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

                + updateContentText
            }

            if (props.uiState.entity?.leaf == true){
                + strings[MessageID.supported_files]
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
        }
    }
}