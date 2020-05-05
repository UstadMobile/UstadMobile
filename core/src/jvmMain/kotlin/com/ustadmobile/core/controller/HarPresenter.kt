package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.HarView

actual class HarPresenter actual constructor(context: Any, arguments: Map<String, String?>, view: HarView, isDownloadEnabled: Boolean, repository: UmAppDatabase, localHttp: String) : HarPresenterCommon(context, arguments, view, isDownloadEnabled, repository, localHttp) {


}