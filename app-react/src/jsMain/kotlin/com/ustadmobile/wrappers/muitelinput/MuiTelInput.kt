@file:JsModule("mui-tel-input")
@file:JsNonModule

package com.ustadmobile.wrappers.muitelinput

import mui.material.TextFieldProps
import react.FC

external interface MuiTelInputProps: TextFieldProps {
    override var value: Any?
    var onChange: (String, MuiTelInputInfo) -> Unit
}

//As per https://viclafouch.github.io/mui-tel-input/docs/api-reference/
@Suppress("unused")
external interface MuiTelInputInfo {
    var countryCallingCode: String?

    var countryCode: String?

    var nationalNumber: String?

    var numberValue: String?

    var reason: String?
}

external val MuiTelInput: FC<MuiTelInputProps>
