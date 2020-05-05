package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.HarView

expect class HarPresenter(context: Any, arguments: Map<String, String?>,
                          view: HarView,
                          isDownloadEnabled: Boolean,
                          repository: UmAppDatabase,
                          localHttp: String): HarPresenterCommon {


}