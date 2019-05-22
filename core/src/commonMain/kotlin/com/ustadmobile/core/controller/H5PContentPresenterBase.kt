package com.ustadmobile.core.controller

import com.ustadmobile.core.view.H5PContentView

expect abstract class H5PContentPresenterBase(context: Any, arguments: Map<String, String?>, view: H5PContentView): UstadBaseController<H5PContentView> {

    suspend fun mountH5PDist(): String

    suspend fun mountH5PContainer(containerUid: Long): String

}