package com.ustadmobile.test.rules

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.asPerson
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.port.android.impl.UstadApp
import com.ustadmobile.test.port.android.util.getApplicationDi
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.net.URL

class UmAppDatabaseAndroidClientRule(
    val account: UmAccount = UmAccount(DEFAULT_ACTIVE_USER_PERSONUID,
        firstName = "Test", lastName = "User",
        username = "theanswer", auth = "", endpointUrl = "http://localhost/"),
    val controlServerUrl: String? = null
) : TestWatcher()  {

    private var dbInternal: UmAppDatabase? = null

    private var repoInternal: UmAppDatabase? = null

    private var appDbServer: Pair<Int, String>? = null

    val db: UmAppDatabase
        get() = dbInternal ?: throw IllegalStateException("Rule not started!")

    val repo: UmAppDatabase
        get() =  repoInternal ?: throw IllegalStateException("Rule not started!")

    var endpointUrl: String? = null
        private set

    //The person object inserted into the database for the default active account
    private lateinit var accountPerson: Person

    override fun starting(description: Description) {
        val di = (getApplicationContext() as UstadApp).di
        val httpClient: HttpClient = di.direct.instance()

        if(description.getAnnotation(UmAppDatabaseServerRequiredTest::class.java) != null) {
            val controlServerUrlVal = controlServerUrl ?: throw IllegalStateException(
                    "${description.methodName} annotated as requiring a server but controlServerUrl not specified")
            val controlServerUrlUrlObj = URL(controlServerUrl)
            appDbServer = runBlocking { httpClient.get("$controlServerUrlVal/servers/newServer").body() }
            endpointUrl = "${controlServerUrlUrlObj.protocol}://${controlServerUrlUrlObj.host}:${appDbServer?.first}/"
            account.endpointUrl = endpointUrl!!
        }

        val endpoint = Endpoint(endpointUrl ?: account.endpointUrl)
        val accountManager: UstadAccountManager by di.instance()

        dbInternal = di.direct.on(endpoint).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

        repoInternal = di.direct.on(endpoint).instance<UmAppDatabase>(tag = DoorTag.TAG_REPO)

        repo.siteDao.insert(Site().apply {
            siteName = "Test Site"
            authSalt = randomString(12)
        })

        accountPerson = runBlocking {
            repo.insertPersonAndGroup(account.asPerson())
        }
        accountManager.startLocalTestSessionBlocking(accountPerson,
            endpointUrl ?: account.endpointUrl)
    }


    override fun finished(description: Description) {
        super.finished(description)
        val di = (getApplicationContext<UstadApp>() as DIAware).di
        val httpClient: HttpClient = di.direct.instance()
        appDbServer?.also { server ->
            runBlocking { httpClient.get("$controlServerUrl/servers/close/${server.first}") }
            endpointUrl = null
            appDbServer = null
        }

        dbInternal = null
        repoInternal = null
    }

    fun insertPersonAndStartSession(person: Person, isAdmin: Boolean = false) : Person{
        runBlocking {
            if(person.personUid == account.personUid) {
                person.personGroupUid = accountPerson.personGroupUid
                repoInternal!!.personDao.update(person)
                accountPerson = person
            }else {
                repoInternal!!.insertPersonAndGroup(person)
            }


            if(isAdmin || person.admin) {
                repo.grantScopedPermission(person, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES,
                    ScopedGrant.ALL_ENTITIES)
            }

            val di = getApplicationDi()
            val accountManager: UstadAccountManager = di.direct.instance()
            accountManager.startLocalTestSessionBlocking(person, account.endpointUrl)
        }

        return person
    }


    companion object {

        const val DEFAULT_ACTIVE_USER_PERSONUID = 42L

    }
}