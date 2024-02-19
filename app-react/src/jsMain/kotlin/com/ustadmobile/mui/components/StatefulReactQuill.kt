package com.ustadmobile.mui.components

import com.ustadmobile.wrappers.quill.ReactQuill
import com.ustadmobile.wrappers.quill.ReactQuillProps
import react.FC
import react.useEffect
import react.useState

val StatefulReactQuill = FC<ReactQuillProps> { props ->
    var stateVar by useState {
        props.value
    }

    useEffect(props.value) {
        if(props.value != stateVar) {
            stateVar = props.value
        }
    }

    ReactQuill {
        value = stateVar
        onChange = {
            stateVar = it
            props.onChange(it)
        }
        id = props.id
        className = props.className
        placeholder = props.placeholder
        readOnly = props.readOnly
    }
}