package com.ustadmobile.view.clazz.inviteviacontact

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.InviteViaContactChip
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.ClazzInviteViaContactUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.ClazzInviteViaContactViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import js.array.ReadonlyArray
import js.objects.jso
import mui.base.AutocompleteChangeReason
import mui.material.Autocomplete
import mui.material.AutocompleteProps
import mui.material.Chip
import mui.material.TextField
import mui.material.Typography
import react.Props
import react.FC
import react.*

external interface ClazzInviteViaContactProps : Props {
    var uiState: ClazzInviteViaContactUiState
    var onChipSubmitClick: (String) -> InviteViaContactChip
    var onChipsRemoved: (List<String>) -> Unit
    var onTextFieldValueChanged: (String) -> Unit
}

val ClazzInviteViaContactScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzInviteViaContactViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzInviteViaContactUiState())

    ClazzInviteViaContactComponent2 {
        uiState = uiStateVal
        onChipSubmitClick=viewModel::onClickChipSubmit
        onChipsRemoved=viewModel::onChipsRemoved
        onTextFieldValueChanged=viewModel::onTextFieldValueChanged
    }
}

private val ClazzInviteViaContactComponent2 = FC<ClazzInviteViaContactProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        maxWidth = "lg"

        Typography {
            +strings[MR.strings.add_username_email_phone]
        }

        Autocomplete {
            +jso<AutocompleteProps<String>> {
                options = emptyArray()
                freeSolo = true
                multiple = true
                renderTags =
                    { value: ReadonlyArray<String>, getTagProps: Function<*>, /* AutocompleteRenderGetTagProps */ownerState: AutocompleteProps<String> ->
                        ReactNode(
                            value.mapIndexed { index, option ->
                                Chip.create {
                                    key = "contact_chip_$option"
                                    label = ReactNode(option)
                                    onDelete = {
                                        props.onChipsRemoved(listOf(option))
                                    }
                                }

                            }.toTypedArray()
                        )
                    }

                renderInput = { inputParams ->
                    TextField.create {
                        +inputParams
                    }
                }
                onInputChange = { event, value, _ ->
                    props.onTextFieldValueChanged(value)
                }
                value = props.uiState.chips.map { it.text }.toTypedArray()
                onChange = { event, value, reason, detail ->
                    val submittedText = detail?.option

                    when {
                        submittedText != null && reason == AutocompleteChangeReason.createOption -> {
                            props.onChipSubmitClick(submittedText)
                        }

                        submittedText != null && reason == AutocompleteChangeReason.removeOption -> {
                            props.onChipsRemoved(listOf(submittedText))
                        }
                    }
                }
            }
        }
    }
}
