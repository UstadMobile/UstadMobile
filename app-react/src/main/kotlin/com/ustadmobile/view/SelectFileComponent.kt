package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import react.RBuilder


class SelectFileComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props), SelectFileView {

    override val viewNames: List<String>
        get() = listOf(SelectFileView.VIEW_NAME)

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.add_file)
    }

    override fun RBuilder.render() {
        umTextField(getString(MessageID.add_file)){
            attrs.asDynamic().accept = "*/*"
            attrs.asDynamic().type = "file"
            attrs.id = "upload-input"
            attrs.onChange = {
                console.log(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}