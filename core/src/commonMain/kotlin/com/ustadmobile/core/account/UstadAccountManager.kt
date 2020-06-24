package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

@Serializable
data class UstadAccounts(var currentAccount: String, val storedAccounts: List<UmAccount>)

class UstadAccountManager(val systemImpl: UstadMobileSystemImpl, val appContext: Any,
                          val dbOpener: DbOpener = DefaultDbOpener(),
                          val httpClient: HttpClient = defaultHttpClient(),
                          val attachmentsDir: String? = null) {

    data class DbPair(val db: UmAppDatabase, val repo: UmAppDatabase)

    data class LoginResponse(val statusCode: Int, val umAccount: UmAccount?)

    interface DbOpener {

        fun openDb(context: Any, name: String) : UmAppDatabase

    }

    private class DefaultDbOpener: DbOpener {
        override fun openDb(context: Any, name: String) = UmAppDatabase.getInstance(context, name)
    }

    private fun UmAppDatabase.toDbAndRepoPair(endpointUrl: String) =
            DbPair(this, this.asRepository(appContext, endpointUrl, "", httpClient,
                attachmentsDir))

    private val _activeAccount: AtomicRef<UmAccount>

    private val _storedAccounts: MutableList<UmAccount>

    private val _storedAccountsLive: DoorMutableLiveData<List<UmAccount>>

    private val _activeAccountLive: DoorMutableLiveData<UmAccount>

    private val dbs: MutableMap<String, DbPair> = mutableMapOf()

    init {
        val accounts: UstadAccounts = systemImpl.getAppPref(ACCOUNTS_PREFKEY, appContext)?.let {
            Json.parse(UstadAccounts.serializer(), it)
        } ?: (systemImpl.getManifestPreference(MANIFEST_DEFAULT_SERVER, appContext) ?: MANIFEST_URL_FALLBACK).let { apiUrl ->
            UstadAccounts("guest@$apiUrl",
                    listOf(UmAccount(0L, "guest", "", apiUrl)))
        }

        _storedAccounts = copyOnWriteListOf(*accounts.storedAccounts.toTypedArray())
        _storedAccountsLive = DoorMutableLiveData(_storedAccounts)
        val activeAccountVal = accounts.storedAccounts.first { it.userAtServer == accounts.currentAccount }
        _activeAccount = atomic(activeAccountVal)
        _activeAccountLive = DoorMutableLiveData(activeAccountVal)

        _storedAccounts.mapNotNull { it.endpointUrl }.forEach {endpointUrl ->
            val dbName = sanitizeDbNameFromUrl(endpointUrl)
            val db = dbOpener.openDb(appContext, dbName)
            dbs[dbName] = db.toDbAndRepoPair(endpointUrl)
        }
    }

    val activeAccount: UmAccount
        get() = _activeAccount.value

    val activeAccountLive: DoorLiveData<UmAccount>
        get() = _activeAccountLive

    val storedAccounts: List<UmAccount>
        get() = _storedAccounts.toList()



    //Provides an immutable map that prevents any possibility of other code changing the internal copy
    val storedDatabases: Map<String, DbPair>
        get() = dbs.entries.map { it.key to DbPair(it.value.db, it.value.repo) }.toMap()

    fun getActiveDatabase(context: Any) {

    }

    fun getActiveRepository(context: Any) {

    }

    suspend fun register() {

    }

    @Synchronized
    private fun addAccount(account: UmAccount) {
        _storedAccounts += account

        val endpointUrl = account.endpointUrl ?: throw IllegalArgumentException("addAccount account must have endpointurl")
        val dbName = sanitizeDbNameFromUrl(endpointUrl)
        if(!dbs.containsKey(dbName)) {
            dbs[dbName] = dbOpener.openDb(appContext, dbName).toDbAndRepoPair(endpointUrl)
        }

        _storedAccountsLive.sendValue(_storedAccounts.toList())
    }

    @Synchronized
    private fun removeAccount(account: UmAccount) {
        _storedAccounts.removeAll { it.userAtServer == account.userAtServer }
        _storedAccountsLive.sendValue(_storedAccounts.toList())
    }

    suspend fun login(username: String, password: String, endpointUrl: String, replaceActiveAccount: Boolean = false): UmAccount {
        val httpStmt = httpClient.post<HttpStatement> {
            url("${endpointUrl.removeSuffix("/")}/auth/login")
            parameter("username", username)
            parameter("password", password)
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
            removeAccount(activeAccount)
        }

        _activeAccount.value = responseAccount
        _activeAccountLive.sendValue(responseAccount)

        return responseAccount
    }

    suspend fun logout(account: UmAccount) {

    }



    companion object {

        const val ACCOUNTS_PREFKEY = "um.accounts"

        const val MANIFEST_DEFAULT_SERVER = "defaultApiUrl"

        const val MANIFEST_URL_FALLBACK = "http://localhost/"

        @Volatile
        private lateinit var accountManager: UstadAccountManager

        @Synchronized
        fun getInstance(systemImpl: UstadMobileSystemImpl, appContext: Any) : UstadAccountManager {
            if(!Companion::accountManager.isInitialized) {
                accountManager = UstadAccountManager(systemImpl, appContext)
            }

            return accountManager
        }

    }

}