package com.ustadmobile.core.test.clientservertest

import com.ustadmobile.core.db.UmAppDatabase
import org.kodein.di.DI

data class ClientServerIntegrationTestContext(
    val serverDi: DI,
    val serverDb: UmAppDatabase,
    val clients: List<ClientServerTestClient>,
)
