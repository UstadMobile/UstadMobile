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
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_GUESTPERSON
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_PERSONGROUP
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class UstadAccountManager(
    private val systemImpl: UstadMobileSystemImpl,
    private val appContext: Any,
    val di: DI
) : IncomingReplicationListener {

    data class ResponseWithAccount(val statusCode: Int, val umAccount: UmAccount?)

    private inner class UserSessionMediator: DoorMediatorLiveData<List<UserSessionWithPersonAndEndpoint>>() {

        private val endpointSessionsListMap = mutableMapOf<Endpoint, List<UserSessionWithPersonAndEndpoint>>()

        private val endpointSessionsLiveDataMap = mutableMapOf<Endpoint, LiveData<List<UserSessionAndPerson>>>()

        fun addEndpoint(endpoint: Endpoint) {
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)
            val liveData = db.userSessionDao.findAllLocalSessionsLive()
            endpointSessionsLiveDataMap[endpoint] = liveData

            addSource(liveData) { endpointSessionList ->
                endpointSessionsListMap[endpoint] = endpointSessionList.map { it.withEndpoint(endpoint) }
                setValue(endpointSessionsListMap.values.flatten())
            }
        }

        fun removeEndpoint(endpoint: Endpoint) {
            val liveData = endpointSessionsLiveDataMap[endpoint] ?: return
            removeSource(liveData)
        }
    }

    private val userSessionLiveDataMediator = UserSessionMediator()

    val activeUserSessionsLive: LiveData<List<UserSessionWithPersonAndEndpoint>>
        get() = userSessionLiveDataMediator

    private val _activeUserSession: AtomicRef<UserSessionWithPersonAndEndpoint?>

    private val _activeUserSessionLive = MutableLiveData<UserSessionWithPersonAndEndpoint?>()

    val activeUserSessionLive: LiveData<UserSessionWithPersonAndEndpoint?>
        get() = _activeUserSessionLive

    private val _activeEndpoint: AtomicRef<Endpoint>

    private val _activeAccountLive = MutableLiveData<UmAccount>()

    private val httpClient: HttpClient by di.instance()

    private val endpointsWithActiveSessions = concurrentSafeListOf<Endpoint>()

    init {
        val activeUserSessionFromJson = systemImpl.getAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, appContext)?.let {
            safeParse(di, UserSessionWithPersonAndEndpoint.serializer(), it)
        }

        _activeUserSession = atomic(activeUserSessionFromJson)
        _activeUserSessionLive.postValue(activeUserSessionFromJson)

        systemImpl.getAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION, appContext)?.also { endpointJson ->
            val endpointStrs = safeParseList(di, ListSerializer(String.serializer()), String::class,
                endpointJson)
            val allEndpoints = endpointStrs.map { Endpoint(it) }
            endpointsWithActiveSessions.addAll(allEndpoints)

            GlobalScope.launch(doorMainDispatcher()) {
                allEndpoints.forEach {
                    addActiveEndpoint(it)
                }
            }
        }


        val activeEndpointStr = systemImpl.getAppPref(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY, appContext)
            ?: systemImpl.getAppConfigString(AppConfig.KEY_API_URL, MANIFEST_URL_FALLBACK, appContext)
            ?: MANIFEST_URL_FALLBACK

        _activeEndpoint = atomic(Endpoint(activeEndpointStr))

        _activeAccountLive.postValue(_activeUserSession.value?.toUmAccount()
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

    suspend fun activeSessionCount(maxDateOfBirth: Long = 0, endpointFilter: (String) -> Boolean = {true}): Int {
        return endpointsWithActiveSessions.filter{ endpointFilter(it.url) }.fold(0) { total, endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            total + db.userSessionDao.countAllLocalSessionsAsync(maxDateOfBirth)
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
            _activeUserSessionLive.postValue(value)

            val activeAccountJson = value?.let {
                safeStringify(di, UserSessionWithPersonAndEndpoint.serializer(), value)
            }

            systemImpl.setAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, activeAccountJson, appContext)

        }

    var activeEndpoint: Endpoint
        get() = _activeEndpoint.value
        set(value){
            _activeEndpoint.value = value
            systemImpl.setAppPref(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY, value.url, appContext)
        }

    val activeAccountLive: LiveData<UmAccount>
        get() = _activeAccountLive

    val storedAccountsLive: LiveData<List<UmAccount>> = DoorMediatorLiveData<List<UmAccount>>().apply {
        addSource(userSessionLiveDataMediator) { userSessionList ->
            setValue(userSessionList.map { it.toUmAccount() })
        }
    }


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

    suspend fun addSession(person: Person, endpointUrl: String, password: String?) : UserSessionWithPersonAndEndpoint{
        val endpoint = Endpoint(endpointUrl)
        val endpointRepo: UmAppDatabase = di.on(endpoint).direct
            .instance(tag = DoorTag.TAG_REPO)

        if(endpoint !in endpointsWithActiveSessions) {
            addActiveEndpoint(endpoint, commit = false)
            commitActiveEndpointsToPref()
        }

        val pbkdf2Params: Pbkdf2Params = di.direct.instance()

        val authSalt = endpointRepo.onRepoWithFallbackToDb(2000) {
            it.siteDao.getSiteAsync()?.authSalt
        } ?: throw IllegalStateException("addSession: No auth salt!")


        val userSession = UserSession().apply {
            usClientNodeId = (endpointRepo as DoorDatabaseRepository).config.nodeId.toLong()
            usPersonUid = person.personUid
            usStartTime = systemTimeInMillis()
            usSessionType = UserSession.TYPE_STANDARD
            usStatus = UserSession.STATUS_ACTIVE
            usAuth = password?.encryptWithPbkdf2(authSalt, pbkdf2Params)?.toHexString()
            usUid = endpointRepo.userSessionDao.insertSession(this)
        }

        return UserSessionWithPersonAndEndpoint(userSession, person, endpoint)
    }

    private suspend fun addActiveEndpoint(endpoint: Endpoint, commit: Boolean = true) {
        endpointsWithActiveSessions += endpoint
        if(commit)
            commitActiveEndpointsToPref()

        withContext(doorMainDispatcher()) {
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            db.addIncomingReplicationListener(this@UstadAccountManager)

            userSessionLiveDataMediator.addEndpoint(endpoint)
        }
    }

    private suspend fun removeActiveEndpoint(endpoint: Endpoint, commit: Boolean = true) {
        endpointsWithActiveSessions -= endpoint
        if(commit)
            commitActiveEndpointsToPref()

        withContext(doorMainDispatcher()) {
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            db.removeIncomingReplicationListener(this@UstadAccountManager)

            userSessionLiveDataMediator.removeEndpoint(endpoint)
        }
    }

    private fun commitActiveEndpointsToPref() {
        val json = Json.encodeToString(ListSerializer(String.serializer()),
            endpointsWithActiveSessions.toSet().map { it.url }.toList())
        systemImpl.setAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION, json, appContext)
    }

    //When sync data comes in, check to see if a change has been actioned that has ended our active
    // session
    override suspend fun onIncomingReplicationProcessed(
        incomingReplicationEvent: IncomingReplicationEvent
    ) {
        if(incomingReplicationEvent.tableId != UserSession.TABLE_ID)
            return

        val activeSessionUid = activeSession?.userSession?.usUid ?: return

        val activeSessionUpdate = incomingReplicationEvent.incomingReplicationData.firstOrNull {
            it.jsonObject["usUid"]?.jsonPrimitive?.longOrNull == activeSessionUid
        } ?: return

        val activeSessionStatus = activeSessionUpdate.jsonObject["usStatus"]?.jsonPrimitive?.intOrNull
        if(activeSessionStatus != UserSession.STATUS_ACTIVE) {
            activeSession = null
        }
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

        activeEndpoint = Endpoint(endpointUrl)
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

    }

}