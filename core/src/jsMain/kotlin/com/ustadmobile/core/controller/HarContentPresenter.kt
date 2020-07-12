package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.HarView
import org.kodein.di.DI

@ExperimentalStdlibApi
actual class HarContentPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                    view: HarView, db: UmAppDatabase,
                                                    repository: UmAppDatabase,
                                                    localHttp: String,  di: DI) :
        HarContentPresenterCommon(context, arguments, view,db, repository, localHttp, di) {


}