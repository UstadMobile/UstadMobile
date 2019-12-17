package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.har.HarRequest
import com.ustadmobile.core.contentformats.har.HarResponse
import com.ustadmobile.core.view.HarView

abstract class HarPresenterCommon(context: Any, arguments: Map<String, String?>, view: HarView) :
        UstadBaseController<HarView>(context, arguments, view) {


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)


    }

    fun handleInterceptRequest(request: HarRequest): HarResponse {


        return HarResponse()
    }


    fun handleUrlLinkToContentEntry() {


    }

    fun handleUpNavigation() {


    }


}