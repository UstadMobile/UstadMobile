package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.CourseTerminologyEditUiState
import com.ustadmobile.lib.db.entities.TerminologyEntry
import com.ustadmobile.mui.components.UstadTextEditField
import mui.material.List
import mui.material.ListItem
import mui.system.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface CourseTerminologyEditScreenProps : Props {

    var uiState: CourseTerminologyEditUiState

    var onTerminologyTermChanged: (TerminologyEntry?) -> Unit

    var onCtTitleChanged: (String?) -> Unit

}

val CourseTerminologyEditScreenComponent2 = FC <CourseTerminologyEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(2)

            UstadTextEditField {
                value = props.uiState.entity?.ctTitle ?: ""
                label = strings[MessageID.name]
                error = props.uiState.titleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCtTitleChanged(it)
                }
            }

            + strings[MessageID.your_words_for]

            List {
                props.uiState.terminologyTermList.forEach { terminologyTerm ->
                    ListItem {

                        UstadTextEditField {
                            fullWidth = true
                            value = terminologyTerm.term ?: ""
                            label = strings[terminologyTerm.messageId]
                            error = terminologyTerm.errorMessage
                            enabled = props.uiState.fieldsEnabled
                            onChange = {
                                props.onTerminologyTermChanged(
                                    terminologyTerm.copy(
                                        term = it
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

val CourseTerminologyEditScreenPreview = FC<Props> {

    val uiStateVal by useState {
        CourseTerminologyEditUiState(
            terminologyTermList = listOf(
                TerminologyEntry(
                    id = "1",
                    term = "First",
                    messageId = MessageID.teacher
                ),
                TerminologyEntry(
                    id = "2",
                    term = "Second",
                    messageId = MessageID.student
                ),
                TerminologyEntry(
                    id = "3",
                    term = "Third",
                    messageId = MessageID.add_a_teacher
                )
            )
        )
    }

    CourseTerminologyEditScreenComponent2 {
        uiState = uiStateVal
    }
}