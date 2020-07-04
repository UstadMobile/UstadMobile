package com.ustadmobile.port.android

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.port.android.generated.MessageIDMap
import org.kodein.di.*
class TestApp : BaseTestApp(), DIAware{

    override val di: DI by DI.lazy {
        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }
        bind<UstadAccountManager>() with singleton { UstadAccountManager.getInstance(instance(),
                applicationContext) }
        bind<UmAppDatabase>(tag = TAG_DB) with provider {}
    }

    override fun onCreate() {
        super.onCreate()
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        initPicasso(applicationContext)
    }
}