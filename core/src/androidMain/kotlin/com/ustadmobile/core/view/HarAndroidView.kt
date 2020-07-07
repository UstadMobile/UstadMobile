package com.ustadmobile.core.view

import com.ustadmobile.core.impl.HarWebViewClient

@ExperimentalStdlibApi
interface HarAndroidView : HarView {

    fun setChromeClient(client: HarWebViewClient)

}