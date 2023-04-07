
package com.ustadmobile.wrappers.quill

import react.ComponentClass
import react.FC
import react.Props

/**
 * An example of using an external React component by Scott_Huang@qq.com (Zhiliang.Huang@gmail.com)
 *
 * Run `npm install react-quill --save`
 * Add `require ("react-quill/dist/quill.snow.css")` to index.kt to include the CSS
 */

external interface ReactQuillProps : Props {
    var value: String
    var onChange: (String) -> Unit
}

@JsModule("react-quill")
external val ReactQuill: ComponentClass<ReactQuillProps>
