package com.ustadmobile.wrappers.draftjs

import react.FC
import react.Props
import react.useState

val EditorPreview = FC<Props> {
    var editorState by useState { EditorState.createEmpty() }

    Editor {
        state = editorState
        onChange = {
            editorState = it
        }
    }

}
