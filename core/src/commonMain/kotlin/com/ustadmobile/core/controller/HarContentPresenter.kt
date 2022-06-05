package com.ustadmobile.core.controller

import com.ustadmobile.core.view.HarView
import org.kodein.di.DI


expect class HarContentPresenter(context: Any, arguments: Map<String, String>,
                                 view: HarView,
                                 localHttp: String,
                                 di: DI): HarContentPresenterCommon {


}