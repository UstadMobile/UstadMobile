package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.encryptWithPbkdf2
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.core.util.ext.withEndpoint
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_GUESTPERSON
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_PERSONGROUP
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class UstadAccountManager(private val systemImpl: UstadMobileSystemImpl,
                          private val appContext: Any,
                          val di: DI) {

    data class ResponseWithAccount(val statusCode: Int, val umAccount: UmAccount?)

    private inner class UserSessionMediator: DoorMediatorLiveData<List<UserSessionWithPersonAndEndpoint>>() {

        private val endpointSessionsListMap = mutableMapOf<Endpoint, List<UserSessionWithPersonAndEndpoint>>()

        private val endpointSessionsLiveDataMap = mutableMapOf<Endpoint, DoorLiveData<List<UserSessionAndPerson>>>()

        fun addEndpoint(endpoint: Endpoint) {
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)
            val liveData = db.userSessionDao.findAllLocalSessionsLive()
            endpointSessionsLiveDataMap[endpoint] = liveData

            addSource(liveData) { endpointSessionList ->
                endpointSessionsListMap[endpoint] = endpointSessionList.map { it.withEndpoint(endpoint) }
                setVal(endpointSessionsListMap.values.flatten())
            }
        }

        fun removeEndpoint(endpoint: Endpoint) {
            val liveData = endpointSessionsLiveDataMap[endpoint] ?: return
            removeSource(liveData)
        }
    }

    private val userSessionLiveDataMediator = UserSessionMediator()

    val activeUserSessionsLive: DoorLiveData<List<UserSessionWithPersonAndEndpoint>>
        get() = userSessionLiveDataMediator

    private val _activeUserSession: AtomicRef<UserSessionWithPersonAndEndpoint?>

    private val _activeUserSessionLive = DoorMutableLiveData<UserSessionWithPersonAndEndpoint?>()

    val activeUserSessionLive: DoorLiveData<UserSessionWithPersonAndEndpoint?>
        get() = _activeUserSessionLive

    private val _activeEndpoint: AtomicRef<Endpoint>

    private val _activeAccountLive = DoorMutableLiveData<UmAccount>()

    private val httpClient: HttpClient by di.instance()

    private val endpointsWithActiveSessions = concurrentSafeListOf<Endpoint>()

    init {
        systemImpl.getAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION, appContext)?.also { endpointJson ->
            val endpointStrs = safeParseList(di, ListSerializer(String.serializer()), String::class,
                endpointJson)
            val allEndpoints = endpointStrs.map { Endpoint(it) }
            endpointsWithActiveSessions.addAll(allEndpoints)

            GlobalScope.launch(doorMainDispatcher()) {
                allEndpoints.forEach {
                    userSessionLiveDataMediator.addEndpoint(it)
                }
            }
        }

        val activeUserSessionFromJson = systemImpl.getAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, appContext)?.let {
            safeParse(di, UserSessionWithPersonAndEndpoint.serializer(), it)
        }

        _activeUserSession = atomic(activeUserSessionFromJson)
        _activeUserSessionLive.sendValue(activeUserSessionFromJson)


        val activeEndpointStr = systemImpl.getAppPref(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY, appContext)
            ?: systemImpl.getAppConfigString(AppConfig.KEY_API_URL, MANIFEST_URL_FALLBACK, appContext)
            ?: MANIFEST_URL_FALLBACK

        _activeEndpoint = atomic(Endpoint(activeEndpointStr))

        _activeAccountLive.sendValue(_activeUserSession.value?.toUmAccount()
            ?: GUEST_PERSON.toUmAccount(activeEndpointStr))
    }

    /**
     * Get a list of all accounts that are on the system across all endpoints
     */
    suspend fun activeSessionsList(endpointFilter: (String) -> Boolean = { true }): List<UserSessionWithPersonAndEndpoint> {
        return endpointsWithActiveSessions.filter { endpointFilter(it.url) }.flatMap { endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            db.userSessionDao.findAllLocalSessionsAsync().map { userSession ->
                userSession.withEndpoint(endpoint)
            }
        }
    }

    suspend fun activeSessionCount(endpointFilter: (String) -> Boolean = {true}): Int {
        return endpointsWithActiveSessions.filter{ endpointFilter(it.url) }.fold(0) { total, endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            total + db.userSessionDao.countAllLocalSessionsAsync()
        }
    }

    //This is the older way of doing things now.
    val activeAccount: UmAccount
        get() = _activeUserSession.value?.toUmAccount()
            ?: GUEST_PERSON.toUmAccount(_activeEndpoint.value.url)

    var activeSession: UserSessionWithPersonAndEndpoint?
        get() = _activeUserSession.value
        set(value) {
            _activeUserSession.value = value
            _activeUserSessionLive.sendValue(value)

            val activeAccountJson = value?.let {
                safeStringify(di, UserSessionWithPersonAndEndpoint.serializer(), value)
            }

            systemImpl.setAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, activeAccountJson, appContext)

            if(value != null)
                activeEndpoint = value.endpoint
        }

    var activeEndpoint: Endpoint
        get() = _activeEndpoint.value
        set(value){
            _activeEndpoint.value = value
            systemImpl.setAppPref(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY, value.url, appContext)
        }

    val activeAccountLive: DoorLiveData<UmAccount>
        get() = _activeAccountLive

    val storedAccountsLive: DoorLiveData<List<UmAccount>> = DoorMediatorLiveData<List<UmAccount>>().apply {
        addSource(userSessionLiveDataMediator) { userSessionList ->
            setVal(userSessionList.map { it.toUmAccount() })
        }
    }


    suspend fun register(person: PersonWithAccount, endpointUrl: String,
                         accountRegisterOptions: AccountRegisterOptions = AccountRegisterOptions()): UmAccount = withContext(Dispatchers.Default){
        val parentVal = accountRegisterOptions.parentJoin
        val httpStmt = httpClient.post<HttpStatement>() {
            url("${endpointUrl.removeSuffix("/")}/auth/register")
            contentType(ContentType.Application.Json)
            body = RegisterRequest(person, parentVal, endpointUrl)
        }

        val (account: UmAccount?, status: Int) = httpStmt.execute { response ->
            if(response.status.value == 200) {
                Pair(response.receive<UmAccount>(), 200)
            }else {
                Pair(null, response.status.value)
            }
        }

        val newPassword = person.newPassword
        if(status == 200 && account != null && newPassword != null) {
            account.endpointUrl = endpointUrl
            val session = addSession(person, endpointUrl, newPassword)
            if(accountRegisterOptions.makeAccountActive){
                activeSession = session
            }

            account
        }else if(status == 409){
            throw IllegalStateException("Conflict: username already taken")
        }else {
            throw Exception("register request: non-OK status code: $status")
        }
    }

    suspend fun addSession(person: Person, endpointUrl: String, password: String?) : UserSessionWithPersonAndEndpoint{
        val endpoint = Endpoint(endpointUrl)
        val endpointRepo: UmAppDatabase = di.on(endpoint).direct
            .instance(tag = DoorTag.TAG_REPO)

        if(endpoint !in endpointsWithActiveSessions) {
            endpointsWithActiveSessions += endpoint
            commitActiveEndpointsToPref()
            withContext(doorMainDispatcher()) {
                userSessionLiveDataMediator.addEndpoint(endpoint)
            }
        }

        val pbkdf2Params: Pbkdf2Params = di.direct.instance()

        val authSalt = endpointRepo.siteDao.getSiteAsync()?.authSalt
            ?: throw IllegalStateException("addSession: No auth salt!")

        val userSession = UserSession().apply {
            usClientNodeId = (endpointRepo as DoorDatabaseSyncRepository).clientId
            usPersonUid = person.personUid
            usStartTime = systemTimeInMillis()
            usSessionType = UserSession.TYPE_STANDARD
            usStatus = UserSession.STATUS_ACTIVE
            usAuth = password?.encryptWithPbkdf2(authSalt, pbkdf2Params)
            usUid = endpointRepo.userSessionDao.insertSession(this)
        }

        return UserSessionWithPersonAndEndpoint(userSession, person, endpoint)
    }

    suspend fun endSession(session: UserSessionWithPersonAndEndpoint,
                           endStatus: Int = UserSession.STATUS_LOGGED_OUT,
                           endReason: Int = UserSession.REASON_LOGGED_OUT
    ) {
        val endpointRepo: UmAppDatabase = di.on(session.endpoint)
            .direct.instance(tag = DoorTag.TAG_REPO)
        endpointRepo.userSessionDao.endSession(
            session.userSession.usUid, endStatus, endReason)

        //check if the active session has been ended.
        if(activeSession?.userSession?.usUid == session.userSession.usUid
            && activeSession?.endpoint == session.endpoint) {
            activeSession = null
        }


        if(activeSessionsList { it == session.endpoint.url }.isEmpty()) {
            endpointsWithActiveSessions -= session.endpoint
            commitActiveEndpointsToPref()
            withContext(doorMainDispatcher()) {
                userSessionLiveDataMediator.removeEndpoint(session.endpoint)
            }
        }
    }

    private fun commitActiveEndpointsToPref() {
        val json = Json.encodeToString(ListSerializer(String.serializer()),
            endpointsWithActiveSessions.map { it.url })
        systemImpl.setAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION, json, appContext)
    }

    suspend fun login(username: String, password: String, endpointUrl: String): UmAccount = withContext(Dispatchers.Default){
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
        val person = repo.personDao.findByUid(responseAccount.personUid)
            ?: throw IllegalStateException("Internal error: could not get person object")
        val newSession = addSession(person, endpointUrl, password)

        activeEndpoint = Endpoint(endpointUrl)
        activeSession = newSession

        //This should not be needed - as responseAccount can be smartcast, but will not otherwise compile
        responseAccount
    }

    suspend fun startGuestSession(endpointUrl: String) {
        val repo: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = UmAppDatabase.TAG_REPO)
        val guestPerson = repo.insertPersonAndGroup(Person().apply {
            username = null
            firstNames = "Guest"
            lastName = "User"
        }, groupFlag = PERSONGROUP_FLAG_PERSONGROUP or PERSONGROUP_FLAG_GUESTPERSON)

        activeSession = addSession(guestPerson, endpointUrl, null)
    }

    suspend fun changePassword(username: String, currentPassword: String?, newPassword: String, endpointUrl: String): UmAccount = withContext(Dispatchers.Default){
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

        val GUEST_PERSON = Person().apply {
            personUid = 0
            firstNames = "Guest"
            lastName = "User"
        }

        const val ACCOUNTS_ACTIVE_SESSION_PREFKEY = "accountmgr.activesession"

        const val ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY = "accountmgr.activeendpoint"

        const val ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION = "accountmgr.endpointswithsessions"

        const val MANIFEST_URL_FALLBACK = "http://localhost/"

    }

}