package com.ustadmobile.util.ext

import mui.material.BaseTextFieldProps
import mui.material.StandardTextFieldProps
import web.html.HTMLInputElement

inline var BaseTextFieldProps.onTextChange: (String) -> Unit

    get() = throw IllegalStateException("Write only!")

    set(crossinline value) {
        unsafeCast<StandardTextFieldProps>().onChange = {
            value(it.target.unsafeCast<HTMLInputElement>().value)
        }
    }

