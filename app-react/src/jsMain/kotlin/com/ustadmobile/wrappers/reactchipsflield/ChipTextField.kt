@file:JsModule("mui-chips-input")
@file:JsNonModule


package com.ustadmobile.wrappers.reactchipsflield

import react.FC
import react.Props
import react.PropsWithChildren

/*
 * Kotlin-JS wrapper for Chip text field https://github.com/viclafouch/mui-chips-input
 */



external interface ChipTextFieldProps : Props {
    var value: Array<String>
    var onChange: (Array<String>) -> Unit

}

@JsName("default")
external val ChipTextField: FC<ChipTextFieldProps>