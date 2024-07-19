package com.ustadmobile.view.clazz.inviteViaContact

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import js.array.ReadonlyArray
import js.objects.jso
import mui.material.Autocomplete
import mui.material.AutocompleteProps
import mui.material.Chip
import mui.material.TextField
import mui.material.Typography
import react.Props
import react.FC
import react.*

external interface InviteViaContactProps : Props {
    var uiState: InviteViaContactUiState
    var onChipSubmitClick: (String) -> Unit
    var onChipRemoved: (String) -> Unit
}

val InviteViaContactScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        InviteViaContactViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(InviteViaContactUiState())

    InviteViaContactComponent2 {
        uiState = uiStateVal
        onChipSubmitClick=viewModel::onClickChipSubmit
        onChipRemoved=viewModel::onChipRemoved
    }
}

private val InviteViaContactComponent2 = FC<InviteViaContactProps> { props ->
    val strings = useStringProvider()
    var chipList by useState { emptyArray<String>() }
    val uiState = props.uiState

    useEffect(uiState.chips) {
        chipList = uiState.chips.map { it.text }.toTypedArray()

    }


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
                                    label = ReactNode(option)
                                    //Here - can send onDelete to the viewmodel
                                    onDelete = {
                                     props.onChipRemoved(option)
                                        chipList = chipList.filterIndexed { i, _ ->
                                            i != index
                                        }.toTypedArray()
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

                value = chipList
                onChange = { event, value, reason, detail ->
                    detail?.option?.let { props.onChipSubmitClick(it) }

                    chipList = value.unsafeCast<Array<String>>()
                }
            }
        }

    }
}
