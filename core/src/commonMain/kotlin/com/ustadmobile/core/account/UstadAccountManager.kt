package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.jvm.Synchronized

@Serializable
data class UstadAccounts(var currentAccount: String,
                         val storedAccounts: List<UmAccount>,
                         val lastUsed: Map<String, Long> = mapOf())

class UstadAccountManager(val systemImpl: UstadMobileSystemImpl, val appContext: Any,
                          val di: DI) {

    data class ResponseWithAccount(val statusCode: Int, val umAccount: UmAccount?)

    private val _activeAccount: AtomicRef<UmAccount>

    private val _storedAccounts: MutableList<UmAccount>

    private val _storedAccountsLive: DoorMutableLiveData<List<UmAccount>>

    private val _activeAccountLive: DoorMutableLiveData<UmAccount>

    private var accountLastUsedTimeMap = mutableMapOf<String, Long>()

    val httpClient: HttpClient by di.instance()

    init {
        val accounts: UstadAccounts = systemImpl.getAppPref(ACCOUNTS_PREFKEY, appContext)?.let {
            safeParse(di, UstadAccounts.serializer(), it)
        } ?: defaultAccount.let { defAccount ->
            UstadAccounts(defAccount.userAtServer, listOf(defAccount))
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
                systemImpl.getAppConfigString(AppConfig.KEY_API_URL, MANIFEST_URL_FALLBACK, appContext) ?: MANIFEST_URL_FALLBACK,
                "Guest", "User")



    var activeAccount: UmAccount
        get() = _activeAccount.value

        @Synchronized
        set(value) {
            val activeUserAtServer = value.userAtServer
            if(!_storedAccounts.any { it. userAtServer == activeUserAtServer}) {
                addAccount(value)
            }

            accountLastUsedTimeMap[activeAccount.userAtServer] = getSystemTimeInMillis()
            _activeAccount.value = value
            _activeAccountLive.sendValue(value)
            commit()
        }

    val activeAccountLive: DoorLiveData<UmAccount>
        get() = _activeAccountLive

    val storedAccounts: List<UmAccount>
        get() = _storedAccounts.toList()

    val storedAccountsLive: DoorLiveData<List<UmAccount>>
        get() = _storedAccountsLive


    suspend fun register(person: PersonWithAccount, endpointUrl: String, makeAccountActive: Boolean = true): UmAccount = withContext(Dispatchers.Default){
        val httpStmt = httpClient.post<HttpStatement>() {
            url("${endpointUrl.removeSuffix("/")}/auth/register")
            parameter("person",  safeStringify(di, PersonWithAccount.serializer(), person))
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
            if(makeAccountActive){
                activeAccount = account
            }
            account
        }else if(status == 409){
            throw IllegalStateException("Conflict: username already taken")
        }else {
            throw Exception("register request: non-OK status code: $status")
        }
    }

    @Synchronized
    private fun addAccount(account: UmAccount, autoCommit: Boolean = true) {
        _storedAccounts += account
        _storedAccountsLive.sendValue(_storedAccounts.toList())
        takeIf { autoCommit }?.commit()
    }


    @Synchronized
    fun removeAccount(account: UmAccount, autoFallback: Boolean = true, autoCommit: Boolean = true) {
        _storedAccounts.removeAll { it.userAtServer == account.userAtServer }

        if(autoFallback && activeAccount.userAtServer == account.userAtServer) {
            val nextAccount = accountLastUsedTimeMap.entries.sortedBy { it.value }.lastOrNull()?.let {lastUsedUserAtServer ->
                _storedAccounts.firstOrNull { it.userAtServer == lastUsedUserAtServer.key }
            } ?: defaultAccount

            activeAccount = nextAccount
        }

        _storedAccountsLive.sendValue(_storedAccounts.toList())
        takeIf { autoCommit }?.commit()
    }

    @Synchronized
    fun commit() {
        val ustadAccounts = UstadAccounts(activeAccount.userAtServer,
            _storedAccounts, accountLastUsedTimeMap)
        systemImpl.setAppPref(ACCOUNTS_PREFKEY,
                safeStringify(di, UstadAccounts.serializer(), ustadAccounts), appContext)
    }


    suspend fun login(username: String, password: String, endpointUrl: String, replaceActiveAccount: Boolean = false): UmAccount = withContext(Dispatchers.Default){
        val repo: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = UmAppDatabase.TAG_REPO)
        val nodeId = (repo as? DoorDatabaseSyncRepository)?.clientId
                ?: throw IllegalStateException("Could not open repo for endpoint $endpointUrl")

        val loginResponse = httpClient.post<HttpResponse> {
            url("${endpointUrl.removeSuffix("/")}/auth/login")
            parameter("username", username)
            parameter("password", password)
            header("X-nid", nodeId)
            expectSuccess = false
        }

        if(loginResponse.status.value == 403) {
            throw UnauthorizedException("Access denied")
        }else if(loginResponse.status.value != 200){
            throw IllegalStateException("Server error - response ${loginResponse.status.value}")
        }

        val responseAccount = loginResponse.receive<UmAccount>()

        responseAccount.endpointUrl = endpointUrl
        addAccount(responseAccount, autoCommit = false)

        if(replaceActiveAccount) {
            removeAccount(activeAccount, autoFallback = false, autoCommit = false)
        }

        activeAccount = responseAccount

        //This should not be needed - as responseAccount can be smartcast, but will not otherwise compile
        responseAccount
    }

    suspend fun changePassword(username: String, currentPassword: String?, newPassword: String, endpointUrl: String): UmAccount? = withContext(Dispatchers.Default){
        val httpStmt = httpClient.post<HttpStatement> {
            url("${endpointUrl.removeSuffix("/")}/password/change")
            parameter("username", username)
            if(currentPassword != null) {
                parameter("currentPassword", currentPassword)
            }
            parameter("newPassword", newPassword)
            expectSuccess = false
        }

        val changePasswordResponse = httpStmt.execute { response ->
            val responseAccount = if (response.status.value == 200) {
                response.receive<UmAccount>()
            } else {
                null
            }
            ResponseWithAccount(response.status.value, responseAccount)
        }
        val responseAccount = changePasswordResponse.umAccount
        if (changePasswordResponse.statusCode == 403) {
            throw UnauthorizedException("Access denied")
        }else if(responseAccount == null || !(changePasswordResponse.statusCode == 200
                        || changePasswordResponse.statusCode == 204)) {
            throw IllegalStateException("Server error - response ${changePasswordResponse.statusCode}")
        }

        responseAccount
    }



    companion object {

        const val ACCOUNTS_PREFKEY = "um.accounts"

        const val MANIFEST_URL_FALLBACK = "http://localhost/"

    }

}