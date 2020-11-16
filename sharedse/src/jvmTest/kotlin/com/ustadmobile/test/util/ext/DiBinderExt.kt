package com.ustadmobile.test.util.ext

import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.DoorTag.Companion.TAG_REPO
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import org.kodein.di.*
import javax.naming.InitialContext

fun DI.Builder.bindDbAndRepoWithEndpoint(endpointScope: EndpointScope, clientMode: Boolean = true) {
    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
        val dbName = sanitizeDbNameFromUrl(context.url)
        InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
        spy(UmAppDatabase.getInstance(Any(), dbName).also {
            it.clearAllTables()
            it.preload()
        })
    }

    bind<UmAppDatabase>(tag = TAG_REPO) with scoped(endpointScope).singleton {
        spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository(Any(),
                if(clientMode) { context.url } else { "http://localhost/dummy" },
                "", defaultHttpClient(), null))
    }

}