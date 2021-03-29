package com.ustadmobile.core.view

import com.ustadmobile.core.impl.HarWebViewClient

interface HarAndroidView : HarView {

    fun setChromeClient(client: HarWebViewClient)

}