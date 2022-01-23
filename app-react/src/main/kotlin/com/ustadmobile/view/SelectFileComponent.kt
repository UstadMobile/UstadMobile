package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SelectFileView
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
//        umFormControl {
//            umTextField(getString(MessageID.add_file)) {
//                attrs.asDynamic().accept = "*/*"
//                attrs.asDynamic().type = "file"
//                attrs.id = "upload-input"
//                attrs.onChange = {
//                    GlobalScope.launch {
//                        startFileUpload(it.target.asDynamic().files.item(0))
//                    }
//                }
//            }
//        }
    }

    suspend fun startFileUpload(file: dynamic) {
//        val endPoint = "${accountManager.activeAccount.endpointUrl}contentupload/upload"
//        val res = (window.asDynamic().fetch(endPoint, json(
//            "method" to  "POST", "body" to file, "mode" to  "cors",
//            "headers" to json(
//                "Content-Type" to "multipart/*; boundary=file"
//            )
//        )) as Promise<dynamic>).await()
        //val data = (res.json() as Promise<dynamic>).await()

    }

}