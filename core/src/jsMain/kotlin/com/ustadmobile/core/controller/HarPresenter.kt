package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.HarView

@ExperimentalStdlibApi
actual class HarPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                             view: HarView, db: UmAppDatabase,
                                             repository: UmAppDatabase,
                                             localHttp: String) :
        HarPresenterCommon(context, arguments, view,db, repository, localHttp) {


}