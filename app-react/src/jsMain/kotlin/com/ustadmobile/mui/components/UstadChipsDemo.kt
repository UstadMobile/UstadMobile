package com.ustadmobile.mui.components

import js.array.ReadonlyArray
import js.objects.jso
import mui.material.Autocomplete
import mui.material.AutocompleteProps
import mui.material.Chip
import mui.material.TextField
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useState

val UstadChipsDemo = FC<Props> {
    var chipList by useState { emptyArray<String>() }

    //https://stackoverflow.com/questions/75598135/chip-inside-a-material-textfield-with-multilines
    Autocomplete {
        + jso<AutocompleteProps<String>> {
            options = emptyArray()
            freeSolo = true
            multiple = true
            renderTags = { value: ReadonlyArray<String>, getTagProps: Function<*>, /* AutocompleteRenderGetTagProps */ownerState: AutocompleteProps<String> ->
                ReactNode(
                    value.mapIndexed { index, option ->
                        Chip.create {
                            label = ReactNode(option)

                            //Here - can send onDelete to the viewmodel
                            onDelete = {
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
                    + inputParams
                }
            }

            value = chipList
            onChange = { event, value, reason, detail ->
                chipList = value.unsafeCast<Array<String>>()
            }
        }
    }
}
