package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.LanguageEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import web.cssom.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props

external interface LanguageEditProps : Props {
    var uiState: LanguageEditUiState
    var onLanguageChanged: (Language?) -> Unit
}

val LanguageEditComponent2 = FC<LanguageEditProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.language?.name ?: ""
                label = strings[MessageID.name]
                error = props.uiState.languageNameError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onLanguageChanged(
                        props.uiState.language?.shallowCopy {
                            name = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.language?.iso_639_1_standard ?: ""
                label = strings[MessageID.two_letter_code]
                error = props.uiState.languageNameError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onLanguageChanged(
                        props.uiState.language?.shallowCopy {
                            iso_639_1_standard = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.language?.iso_639_2_standard ?: ""
                label = strings[MessageID.three_letter_code]
                error = props.uiState.languageNameError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onLanguageChanged(
                        props.uiState.language?.shallowCopy {
                            iso_639_2_standard = it
                        }
                    )
                }
            }
        }
    }

}

val LanguageEditPreview = FC<Props> {
    LanguageEditComponent2 {
        uiState = LanguageEditUiState(
            language = Language().apply {
                name = "fa"
            }
        )
    }
}