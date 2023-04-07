package com.ustadmobile.wrappers.draftjs

import react.FC
import react.Props
import react.useState

//Should use https://www.npmjs.com/package/react-draft-wysiwyg
val EditorPreview = FC<Props> {
    var edState by useState { EditorState.createEmpty() }

    println("Editor state = $edState")
    Editor {
        editorState = edState
        onChange = {
            println("Editor change")
            edState = it
        }
    }

}
