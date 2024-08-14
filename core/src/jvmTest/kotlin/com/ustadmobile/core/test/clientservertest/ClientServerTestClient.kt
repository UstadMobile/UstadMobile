package com.ustadmobile.core.test.clientservertest

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Represents a client in a ClientServerIntegrationTest. The client has its own DI.
 */
class ClientServerTestClient(
    val clientNum: Int,
    val di: DI,
    val diEndpointScope: EndpointScope,
    private val serverDi: DI,
    val serverUrl: String,
) {

    /**
     * Create a user and login as the given user. The user will be created on the server. The client
     * will then login using the UstadAccountManager.
     */
    suspend fun createUserAndLogin(
        person: Person = Person().apply {
            firstNames = "Test"
            lastName = "User $clientNum"
            username = "testuser$clientNum"
        },
        password: String = "test",
    ): UmAccount {
        //put the person
        val serverDb: UmAppDatabase = serverDi.direct.on(Endpoint(serverUrl)).instance(tag = DoorTag.TAG_DB)
        val addNewPersonUseCase = AddNewPersonUseCase(serverDb, null)
        val newPersonUid = addNewPersonUseCase(person)
        val serverAuthManager: AuthManager = serverDi.direct.on(Endpoint(serverUrl)).instance()
        serverAuthManager.setAuth(newPersonUid, password)
        return login(person.username ?: "", password)
    }

    suspend fun login(username: String, password: String) : UmAccount {
        val accountManager: UstadAccountManager = di.direct.instance()
        return accountManager.login(username, password, serverUrl)
    }

    fun close() {
        diEndpointScope.activeEndpointUrls.forEach {
            di.direct.instance<UstadAccountManager>().close()
            di.on(Endpoint(it)).direct.instance<UmAppDataLayer>().repository?.close()
            di.on(Endpoint(it)).direct.instance<UmAppDatabase>(tag = DoorTag.TAG_DB).close()
        }
    }

}
