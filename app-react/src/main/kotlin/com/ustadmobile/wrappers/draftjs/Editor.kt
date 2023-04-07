@file:JsModule("react-draft-wysiwyg")

package com.ustadmobile.wrappers.draftjs

import react.FC
import react.Props

external interface EditorProps: Props {

    var editorState: EditorState

    var onChange: (EditorState) -> Unit

}

external val Editor: FC<EditorProps>
