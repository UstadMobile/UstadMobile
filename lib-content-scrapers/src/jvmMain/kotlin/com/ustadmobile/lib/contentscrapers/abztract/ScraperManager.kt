package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class ScraperManager(private val indexTotal: Int = 4,
                     private val scraperTotal: Int = 1, val endpoint: Endpoint, override val di: DI) : DIAware {

    val appDb: UmAppDatabase by on(endpoint).instance(tag = UmAppDatabase.TAG_DB)


}