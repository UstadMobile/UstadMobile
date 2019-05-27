package com.ustadmobile.core.controller

import com.ustadmobile.core.view.H5PContentView

actual abstract class  H5PContentPresenterBase actual constructor(context: Any, arguments: Map<String, String?>, view: H5PContentView): UstadBaseController<H5PContentView>(context, arguments, view){
    actual suspend fun mountH5PDist(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun mountH5PContainer(containerUid: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}