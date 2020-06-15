package com.ustadmobile.port.android

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.port.android.generated.MessageIDMap

class TestApp : BaseTestApp() {
    override fun onCreate() {
        super.onCreate()
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        initPicasso(applicationContext)
    }
}