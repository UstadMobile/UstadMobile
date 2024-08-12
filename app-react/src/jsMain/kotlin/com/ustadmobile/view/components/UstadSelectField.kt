package com.ustadmobile.view.components

import js.objects.jso
import mui.material.*
import react.*


external interface UstadSelectFieldProps<T: Any>: Props {

    var value: T

    var options: List<T>

    var itemValue: (T) -> String

    var itemLabel: (T) -> ReactNode

    var onChange: (T) -> Unit

    var label: String

    var id: String

    var fullWidth: Boolean

    var enabled: Boolean?

    var error: Boolean?

    var helperText: ReactNode?

}

private val UstadSelectFieldFC = FC<UstadSelectFieldProps<Any>> { props ->

    val selectVal = try {
        props.itemValue(props.value)
    }catch(e: Exception){
        //Current value not in options list, maybe an unset field.
        //throw IllegalArgumentException("SelectField(id=${props.id}): cannot get value. Not in options list maybe?", e)
        "__unset__"
    }

    FormControl {
        fullWidth = true

        InputLabel {
            id = "${props.id}_label"
            +props.label
        }

        Select {
            value = selectVal
            id = props.id
            labelId = "${props.id}_label"
            label = ReactNode(props.label)
            disabled = !(props.enabled ?: true)
            fullWidth = props.fullWidth
            error = props.error
            onChange = { event, _ ->
                val selectedVal = ("" + event.target.value)
                val selectedItem = props.options.firstOrNull {
                    props.itemValue(it) ==  selectedVal
                }

                if(selectedItem != null) {
                    props.onChange(selectedItem)
                }else {
                    console.log("UstadSelect: User selected item not in value options")
                }
            }

            if(props.value !in props.options) {
                MenuItem {
                    value = "__unset__"
                    + ""
                }
            }

            props.options.forEach { option ->
                MenuItem {
                    value = props.itemValue(option)
                    + props.itemLabel(option)
                }
            }
        }

        props.helperText?.also {
            FormHelperText {
                +it
            }
        }
    }

}

/**
 * As per :
 * https://github.com/JetBrains/kotlin-wrappers/blob/c64e209ff5ffc35c2c1fa0d94a259b24ac848123/kotlin-react-select/src/main/kotlin/react/select/Select.ext.kt#L7
 */
fun <Option: Any> ChildrenBuilder.UstadSelectField(
    block: @ReactDsl  UstadSelectFieldProps<Option>.() -> Unit
) {
    child(UstadSelectFieldFC, jso(block))
}

data class PreviewOption(val value: String, val label: String)

val UstadSelectFieldPreview = FC<Props> {

    var currentValue by useState { PreviewOption("1", "one") }

    UstadSelectField<PreviewOption> {
        options = listOf(PreviewOption("1", "one"), PreviewOption("2", "two"))
        itemValue = { it.value }
        itemLabel = { ReactNode(it.label) }
        onChange = {
            currentValue = it
        }
        label = "Preview"
        id = "testselect"
        value = currentValue
    }
}
