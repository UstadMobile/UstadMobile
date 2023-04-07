@file:JsModule("draft-js")

package com.ustadmobile.wrappers.draftjs

import react.FC
import react.Props

external interface EditorProps: Props {

    var state: EditorState

    var onChange: (EditorState) -> Unit

}

external val Editor: FC<EditorProps>
