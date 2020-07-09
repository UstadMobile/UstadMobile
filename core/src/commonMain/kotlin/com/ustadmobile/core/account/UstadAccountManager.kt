package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.copyOnWriteListOf
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.*
import io.ktor.client.statement.HttpStatement
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.jvm.Synchronized

@Serializable
data class UstadAccounts(var currentAccount: String,
                         val storedAccounts: List<UmAccount>,
                         val lastUsed: Map<String, Long> = mapOf())

class UstadAccountManager(val systemImpl: UstadMobileSystemImpl, val appContext: Any,
                          val di: DI,
                          val httpClient: HttpClient = defaultHttpClient()) {

    data class DbPair(val db: UmAppDatabase, val repo: UmAppDatabase)

    data class LoginResponse(val statusCode: Int, val umAccount: UmAccount?)

    interface DbOpener {

        fun openDb(context: Any, name: String) : UmAppDatabase

    }

    private class DefaultDbOpener: DbOpener {
        override fun openDb(context: Any, name: String) = UmAppDatabase.getInstance(context, name)
    }

    private val _activeAccount: AtomicRef<UmAccount>

    private val _storedAccounts: MutableList<UmAccount>

    private val _storedAccountsLive: DoorMutableLiveData<List<UmAccount>>

    private val _activeAccountLive: DoorMutableLiveData<UmAccount>

    private var accountLastUsedTimeMap = mutableMapOf<String, Long>()

    init {
        val accounts: UstadAccounts = systemImpl.getAppPref(ACCOUNTS_PREFKEY, appContext)?.let {
            Json.parse(UstadAccounts.serializer(), it)
        } ?: defaultAccount.let { defAccount ->
            UstadAccounts(defaultAccount.userAtServer, listOf(defAccount))
        }

        _storedAccounts = copyOnWriteListOf(*accounts.storedAccounts.toTypedArray())
        _storedAccountsLive = DoorMutableLiveData(_storedAccounts)
        val activeAccountVal = accounts.storedAccounts.first { it.userAtServer == accounts.currentAccount }
        _activeAccount = atomic(activeAccountVal)
        _activeAccountLive = DoorMutableLiveData(activeAccountVal)

        accountLastUsedTimeMap.putAll(accounts.lastUsed)
    }

    private val defaultAccount: UmAccount
        get() = UmAccount(0L, "guest", "",
                (systemImpl.getManifestPreference(MANIFEST_DEFAULT_SERVER, appContext) ?: MANIFEST_URL_FALLBACK),
                "Guest", "User")



    var activeAccount: UmAccount
        get() = _activeAccount.value

        @Synchronized
        set(value) {
            val activeUserAtServer = value.userAtServer
            if(!_storedAccounts.any { it. userAtServer == activeUserAtServer}) {
                addAccount(value)
            }

            _activeAccount.value = value
            _activeAccountLive.sendValue(value)
        }

    val activeAccountLive: DoorLiveData<UmAccount>
        get() = _activeAccountLive

    val storedAccounts: List<UmAccount>
        get() = _storedAccounts.toList()

    val storedAccountsLive: DoorLiveData<List<UmAccount>>
        get() = _storedAccountsLive


    suspend fun register(person: Person, password: String, endpointUrl: String, replaceActiveAccount: Boolean = false): UmAccount {
        val httpStmt = httpClient.get<HttpStatement>() {
            url("${endpointUrl.removeSuffix("/")}/auth/register")
            parameter("person", Json.stringify(Person.serializer(), person))
            parameter("password", password)
        }

        val (account: UmAccount?, status: Int) = httpStmt.execute { response ->
            if(response.status.value == 200) {
                Pair(response.receive<UmAccount>(), 200)
            }else {
                Pair(null, response.status.value)
            }
        }


        if(status == 200 && account != null) {
            account.endpointUrl = endpointUrl
            activeAccount = account

            return account
        }else if(status == 409){
            throw IllegalArgumentException("Conflict: username already taken")
        }else {
            throw IllegalStateException("register request: non-OK status code: $status")
        }
    }

    @Synchronized
    private fun addAccount(account: UmAccount) {
        _storedAccounts += account
        _storedAccountsLive.sendValue(_storedAccounts.toList())
    }


    @Synchronized
    fun removeAccount(account: UmAccount, autoFallback: Boolean = true) {
        _storedAccounts.removeAll { it.userAtServer == account.userAtServer }

        if(autoFallback && activeAccount.userAtServer == account.userAtServer) {
            val nextAccount = accountLastUsedTimeMap.entries.sortedBy { it.value }.lastOrNull()?.let {lastUsedUserAtServer ->
                _storedAccounts.firstOrNull { it.userAtServer == lastUsedUserAtServer.key }
            } ?: defaultAccount

            activeAccount = nextAccount
        }

        _storedAccountsLive.sendValue(_storedAccounts.toList())
    }


    suspend fun login(username: String, password: String, endpointUrl: String, replaceActiveAccount: Boolean = false): UmAccount {
        val repo: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = UmAppDatabase.TAG_REPO)
        val nodeId = (repo as? DoorDatabaseSyncRepository)?.clientId
                ?: throw IllegalStateException("Could not open repo for endpoint $endpointUrl")

        val httpStmt = httpClient.post<HttpStatement> {
            url("${endpointUrl.removeSuffix("/")}/auth/login")
            parameter("username", username)
            parameter("password", password)
            header("X-nid", nodeId)

        }


        val loginResponse = httpStmt.execute { response ->
            val responseAccount = if (response.status.value == 200) {
                response.receive<UmAccount>()
            } else {
                null
            }

            LoginResponse(response.status.value, responseAccount)
        }

        val responseAccount = loginResponse.umAccount
        if (loginResponse.statusCode == 403) {
            throw UnauthorizedException("Access denied")
        }else if(responseAccount == null || !(loginResponse.statusCode == 200 || loginResponse.statusCode == 204)) {
            throw IllegalStateException("Server error - response ${loginResponse.statusCode}")
        }


        responseAccount.endpointUrl = endpointUrl
        addAccount(responseAccount)

        if(replaceActiveAccount) {
            removeAccount(activeAccount, autoFallback = false)
        }

        activeAccount = responseAccount

        return responseAccount
    }


    companion object {

        const val ACCOUNTS_PREFKEY = "um.accounts"

        const val MANIFEST_DEFAULT_SERVER = "defaultApiUrl"

        const val MANIFEST_URL_FALLBACK = "http://localhost/"

    }

}