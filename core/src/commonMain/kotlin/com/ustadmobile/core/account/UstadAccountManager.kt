package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.util.ext.encryptWithPbkdf2
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.util.ext.withEndpoint
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_GUESTPERSON
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_PERSONGROUP
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class UstadAccountManager(
    private val systemImpl: UstadMobileSystemImpl,
    private val appContext: Any,
    val di: DI
)  {

    /**
     * The account that is currently the selected account on screen
     */
    private val _currentUserSession : MutableStateFlow<UserSessionWithPersonAndEndpoint>

    /**
     * Endpoint urls that have active sessions that are on the device. This is stored in preferences
     * so that we know what databases we need to collect from
     */
    private val _endpointsWithActiveSessions: MutableStateFlow<List<Endpoint>>

    /**
     * Flow that can be accessed to access all active accounts (e.g. the current user session AND
     * all accounts that are stored on this device that the user could switch to).
     */
    private val _allActiveAccounts: MutableStateFlow<List<UserSessionWithPersonAndEndpoint>>

    fun interface EndpointFilter {

       fun filterEndpoint(endpointUrl: String): Boolean

    }

    data class ResponseWithAccount(val statusCode: Int, val umAccount: UmAccount?)


    val activeUserSessionsLive: Flow<List<UserSessionWithPersonAndEndpoint>>
        get() = _allActiveAccounts.asStateFlow()

    val activeUserSessionLive: Flow<UserSessionWithPersonAndEndpoint?>
        get() = _currentUserSession.asStateFlow()

    private val httpClient: HttpClient by di.instance()

    //private val endpointsWithActiveSessions = concurrentSafeListOf<Endpoint>()

    private val json: Json by di.instance()

    private val apiUrlConfig: ApiUrlConfig by di.instance()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        val currentEndpointStr = systemImpl.getAppPref(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY)
            ?: apiUrlConfig.presetApiUrl ?: MANIFEST_URL_FALLBACK
        val currentDb: UmAppDatabase = di.direct.instance(tag = DoorTag.TAG_DB)

        val initUserSession: UserSessionWithPersonAndEndpoint = systemImpl.getAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY)?.let {
            json.decodeFromString(it)
        } ?: makeNewTempGuestSession(currentEndpointStr,  currentDb)
        _currentUserSession = MutableStateFlow(initUserSession)
        val initEndpoints: List<String> = systemImpl.getAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION)?.let {
            json.decodeFromString(ListSerializer(String.serializer()), it)
        } ?: listOf(currentEndpointStr)
        _endpointsWithActiveSessions = MutableStateFlow(initEndpoints.map { Endpoint(it) })

        _allActiveAccounts = MutableStateFlow(listOf(initUserSession))

        //Note: might need to collect all active endpoints and use that to watch for any session
        //invalidations

        /**
         * Provide a flow (as/when required) of all active sessions. This requires using flows
         * from multiple different databases. The database flows will only be collected if/when the
         * all active accounts flow is being collected.
         */
        scope.launch {
            _allActiveAccounts.whenSubscribed {
                _endpointsWithActiveSessions.collectLatest { endpointsWithSessions ->
                    endpointsWithSessions.forEach { endpoint ->
                        scope.launch {
                            val endpointDb: UmAppDatabase = di.on(endpoint).direct
                                .instance(tag = DoorTag.TAG_DB)
                            endpointDb.userSessionDao.findAllLocalSessionsLive().collect { endpointSessions ->
                                _allActiveAccounts.update { prev ->
                                    prev.filter {
                                        it.endpoint != endpoint
                                    } +  endpointSessions.map {
                                        UserSessionWithPersonAndEndpoint(
                                            userSession = it.userSession ?: UserSession(),
                                            person = it.person ?: Person(),
                                            endpoint = endpoint
                                        )
                                    }.sortedBy { it.displayName }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Note: Guest sessions can be:
     *
     * Temporary: e.g. auto-created when there is no other account. This will not be sent to the server,
     * will not be displayed on the account list, etc.
     *
     * Selected: when a user clicks login as guest on a site that allows guest login. The guest
     * session is then converted from temporary to selected. It will then be sent to the server
     * (e.g. to be included in statistics on total number of users etc).
     */
    private fun makeNewTempGuestSession(
        endpointUrl: String,
        currentDb: UmAppDatabase
    ): UserSessionWithPersonAndEndpoint {
        return UserSessionWithPersonAndEndpoint(
            userSession = UserSession().apply {
                usUid = currentDb.doorPrimaryKeyManager.nextId(UserSession.TABLE_ID)
                usClientNodeId = currentDb.doorWrapperNodeId
                usStartTime = systemTimeInMillis()
            },
            person = GUEST_PERSON,
            endpoint = Endpoint(endpointUrl),
        ).also {
            scope.launch {
                currentDb.userSessionDao.insertSession(it.userSession)
            }
        }
    }

    /**
     * Get a list of all accounts that are on the system across all endpoints
     */
    suspend fun activeSessionsList(
        endpointFilter: EndpointFilter = EndpointFilter { true }
    ): List<UserSessionWithPersonAndEndpoint> {
        return _endpointsWithActiveSessions.value.filter { endpointFilter.filterEndpoint(it.url) }.flatMap { endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            db.userSessionDao.findAllLocalSessionsAsync().map { userSession ->
                userSession.withEndpoint(endpoint)
            }
        }
    }

    suspend fun activeSessionCount(
        maxDateOfBirth: Long = 0,
        endpointFilter: EndpointFilter = EndpointFilter { true }
    ): Int {
        return _endpointsWithActiveSessions.value.filter { endpointFilter.filterEndpoint(it.url) }.fold(0) { total, endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            total + db.userSessionDao.countAllLocalSessionsAsync(maxDateOfBirth)
        }
    }

    //This is the older way of doing things now.
    val activeAccount: UmAccount
        get() = _currentUserSession.value.toUmAccount()

    var activeSession: UserSessionWithPersonAndEndpoint
        get() = _currentUserSession.value
        set(value) {
            _currentUserSession.value = value

            val activeAccountJson = json.encodeToString(value)
            systemImpl.setAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, activeAccountJson)
        }

    val activeEndpoint: Endpoint
        get() = _currentUserSession.value.endpoint

    val activeAccountLive: Flow<UmAccount>
        get() = _currentUserSession.map { it.toUmAccount() }

    suspend fun register(
        person: PersonWithAccount,
        endpointUrl: String,
        accountRegisterOptions: AccountRegisterOptions = AccountRegisterOptions()
    ): PersonWithAccount = withContext(Dispatchers.Default){
        val parentVal = accountRegisterOptions.parentJoin
        val httpStmt = httpClient.preparePost {
            url("${endpointUrl.removeSuffix("/")}/auth/register")
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(person, parentVal, endpointUrl))
        }

        val (registeredPerson: Person?, status: Int) = httpStmt.execute { response ->
            if(response.status.value == 200) {
                Pair(response.body<PersonWithAccount>(), 200)
            }else {
                Pair(null, response.status.value)
            }
        }

        val newPassword = person.newPassword
        if(status == 200 && registeredPerson != null && newPassword != null) {
            if(accountRegisterOptions.makeAccountActive){
                val session = addSession(registeredPerson, endpointUrl, newPassword)
                activeSession = session
            }

            registeredPerson
        }else if(status == 409){
            throw IllegalStateException("Conflict: username already taken")
        }else {
            throw Exception("register request: non-OK status code: $status")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun addSession(
        person: Person,
        endpointUrl: String,
        password: String?
    ) : UserSessionWithPersonAndEndpoint{
        val endpoint = Endpoint(endpointUrl)
        val endpointRepo: UmAppDatabase = di.on(endpoint).direct
            .instance(tag = DoorTag.TAG_REPO)

        if(endpoint !in _endpointsWithActiveSessions.value) {
            addActiveEndpoint(endpoint, commit = false)
            commitActiveEndpointsToPref()
        }

        val pbkdf2Params: Pbkdf2Params = di.direct.instance()

        val userSession = endpointRepo.withDoorTransactionAsync {
            val authSalt = endpointRepo.onRepoWithFallbackToDb(2000) {
                it.siteDao.getSiteAsync()?.authSalt
            } ?: throw IllegalStateException("addSession: No auth salt!")

            val nodeId = di.on(endpoint).direct.instance<NodeIdAndAuth>().nodeId
            UserSession().apply {
                usClientNodeId = nodeId
                usPersonUid = person.personUid
                usStartTime = systemTimeInMillis()
                usSessionType = UserSession.TYPE_STANDARD
                usStatus = UserSession.STATUS_ACTIVE
                usAuth = password?.encryptWithPbkdf2(
                    authSalt, pbkdf2Params, endpoint, httpClient
                )?.toHexString()
                usUid = endpointRepo.userSessionDao.insertSession(this)
            }
        }

        return UserSessionWithPersonAndEndpoint(userSession, person, endpoint)
    }

    @Suppress("RedundantSuspendModifier") // Reserved for use if required as per previous api
    private suspend fun addActiveEndpoint(endpoint: Endpoint, commit: Boolean = true) {
        _endpointsWithActiveSessions.update { prev ->
            prev + listOf(endpoint)
        }

        if(commit)
            commitActiveEndpointsToPref()
    }

    private suspend fun removeActiveEndpoint(endpoint: Endpoint, commit: Boolean = true) {
        _endpointsWithActiveSessions.update { prev ->
            prev.filter { it != endpoint }
        }
        if(commit)
            commitActiveEndpointsToPref()
    }

    private fun commitActiveEndpointsToPref() {
        val json = Json.encodeToString(ListSerializer(String.serializer()),
            _endpointsWithActiveSessions.value.toSet().map { it.url }.toList())
        systemImpl.setAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION, json)
    }

    //When sync data comes in, check to see if a change has been actioned that has ended our active
    // session
//    override suspend fun onIncomingReplicationProcessed(
//        incomingReplicationEvent: IncomingReplicationEvent
//    ) {
//        if(incomingReplicationEvent.tableId != UserSession.TABLE_ID)
//            return
//
//        val activeSessionUid = activeSession?.userSession?.usUid ?: return
//
//        val activeSessionUpdate = incomingReplicationEvent.incomingReplicationData.firstOrNull {
//            it.jsonObject["usUid"]?.jsonPrimitive?.longOrNull == activeSessionUid
//        } ?: return
//
//        val activeSessionStatus = activeSessionUpdate.jsonObject["usStatus"]?.jsonPrimitive?.intOrNull
//        if(activeSessionStatus != UserSession.STATUS_ACTIVE) {
//            activeSession = null
//        }
//    }

    suspend fun endSession(
        session: UserSessionWithPersonAndEndpoint,
        endStatus: Int = UserSession.STATUS_LOGGED_OUT,
        endReason: Int = UserSession.REASON_LOGGED_OUT
    ) {
        val endpointRepo: UmAppDatabase = di.on(session.endpoint)
            .direct.instance(tag = DoorTag.TAG_REPO)
        endpointRepo.userSessionDao.endSession(
            session.userSession.usUid, endStatus, endReason)

        //check if the active session has been ended.
        if(activeSession.userSession.usUid == session.userSession.usUid
            && activeSession.endpoint == session.endpoint) {
            activeSession = makeNewTempGuestSession(session.endpoint.url, endpointRepo)
        }


        if(activeSessionsList { it == session.endpoint.url }.isEmpty()) {
            removeActiveEndpoint(session.endpoint)
        }
    }



    suspend fun login(username: String, password: String, endpointUrl: String,
        maxDateOfBirth: Long = 0L): UmAccount = withContext(Dispatchers.Default){
        val repo: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = DoorTag.TAG_REPO)
        val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = DoorTag.TAG_DB)

        val nodeId = (repo as? DoorDatabaseRepository)?.config?.nodeId
            ?: throw IllegalStateException("Could not open repo for endpoint $endpointUrl")

        val loginResponse = httpClient.post {
            url("${endpointUrl.removeSuffix("/")}/auth/login")
            parameter("username", username)
            parameter("password", password)
            parameter("maxDateOfBirth", maxDateOfBirth)
            header("X-nid", nodeId)
            expectSuccess = false
        }

        if(loginResponse.status.value == 403) {
            throw UnauthorizedException("Access denied")
        }else if(loginResponse.status == HttpStatusCode.FailedDependency) {
            //Used to indicate where parental consent is required, but not granted
            throw ConsentNotGrantedException("Parental consent required but not granted")
        }else if(loginResponse.status == HttpStatusCode.Conflict) {
            throw AdultAccountRequiredException("Adult account required, credentials for child account")
        }else if(loginResponse.status.value != 200){
            throw IllegalStateException("Server error - response ${loginResponse.status.value}")
        }

        val responseAccount = loginResponse.body<UmAccount>()
        responseAccount.endpointUrl = endpointUrl

        var personInDb = db.personDao.findByUidAsync(responseAccount.personUid)

        if(personInDb == null){
            val personOnServerResponse = httpClient.get {
                url("${endpointUrl.removeSuffix("/")}/auth/person")
                parameter("personUid", responseAccount.personUid)
            }
            if(personOnServerResponse.status.value == 200) {
                val personObj = personOnServerResponse.body<Person>()
                repo.personDao.insertAsync(personObj)
                personInDb = personObj
            }else {
                throw IllegalStateException("Internal error: could not get person object")
            }
        }

        getSiteFromDbOrLoadFromHttp(endpointUrl, repo)

        val newSession = addSession(personInDb, endpointUrl, password)

        //activeEndpoint = Endpoint(endpointUrl)
        activeSession = newSession

        //This should not be needed - as responseAccount can be smartcast, but will not otherwise compile
        responseAccount
    }

    private suspend fun getSiteFromDbOrLoadFromHttp(
        endpointUrl: String,
        repo: UmAppDatabase
    ) {
        val db = (repo as DoorDatabaseRepository).db as UmAppDatabase
        val siteInDb = db.siteDao.getSiteAsync()
        if(siteInDb == null) {
            val siteResponse = httpClient.get {
                doorNodeAndVersionHeaders(repo as DoorDatabaseRepository)
                url("${endpointUrl.removeSuffix("/")}/UmAppDatabase/SiteDao/getSiteAsync")
            }
            if(siteResponse.status.value == 200) {
                val siteObj = siteResponse.body<Site>()
                repo.siteDao.replaceAsync(siteObj)
            }else {
                throw IllegalStateException("Internal error: no Site in database and could not fetch it from server")
            }
        }
    }

    suspend fun startGuestSession(endpointUrl: String) {
        val repo: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = DoorTag.TAG_REPO)
        val guestPerson = repo.insertPersonAndGroup(Person().apply {
            username = null
            firstNames = "Guest"
            lastName = "User"
        }, groupFlag = PERSONGROUP_FLAG_PERSONGROUP or PERSONGROUP_FLAG_GUESTPERSON)

        getSiteFromDbOrLoadFromHttp(endpointUrl, repo)

        activeSession = addSession(guestPerson, endpointUrl, null)
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

        /**
         * Prefix for preference keys related to External Access Permission
         */
        const val KEY_PREFIX_EAPUID = "eap_"

        /**
         * The AccountType (if used)
         */
        const val ACCOUNT_TYPE = "com.ustadmobile"

        /**
         * Intent action indicating that the caller wants to get an authentication token
         */
        const val ACTION_GET_AUTH_TOKEN = "com.ustadmobile.AUTH_GET_TOKEN"

    }

}