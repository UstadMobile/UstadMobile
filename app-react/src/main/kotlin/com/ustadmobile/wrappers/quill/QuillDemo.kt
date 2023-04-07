package com.ustadmobile.wrappers.quill

import react.FC
import react.Props
import react.useState

val QuillDemo = FC<Props> {

    var text by useState { "Hello Quill" }

    ReactQuill{
        value = text
        onChange = {
            text = it
        }
    }

}
