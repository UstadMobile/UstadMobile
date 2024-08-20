package com.ustadmobile.core.account

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.ustadmobile.core.account.UstadAccountManager.EndpointFilter
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.account.CreateNewLocalAccountUseCase
import com.ustadmobile.core.domain.passkey.PassKeySignInData
import com.ustadmobile.core.domain.passkey.PasskeyResult
import com.ustadmobile.core.domain.passkey.PasskeyVerifyResult
import com.ustadmobile.core.domain.passkey.SavePersonPasskeyUseCase
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.util.ext.withLearningSpace
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.doorWrapperNodeId
import com.ustadmobile.door.ext.setBodyJson
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.message.DoorMessage
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_GUESTPERSON
import com.ustadmobile.lib.db.entities.PersonGroup.Companion.PERSONGROUP_FLAG_PERSONGROUP
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.url
import io.github.aakira.napier.Napier
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * The app AccountManager. Users can have multiple accounts with active sessions at any given time.
 * There is one "current" account at a time- this is the account which is currently selected which
 * is active on screen.
 *
 * If the user has not logged in, then the a temporary guest account will be set as the current
 * account.
 */
class UstadAccountManager(
    private val settings: Settings,
    val di: DI
)  {


    private val _currentUserSession : MutableStateFlow<UserSessionWithPersonAndLearningSpace>

    /**
     * The current user session is the one for the currently selected account
     */
    var currentUserSession: UserSessionWithPersonAndLearningSpace
        get() = _currentUserSession.value
        set(value) {
            _currentUserSession.value = value

            val activeAccountJson = json.encodeToString(value)
            settings[ACCOUNTS_ACTIVE_SESSION_PREFKEY] = activeAccountJson
            settings[ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY] = value.learningSpace.url
        }

    /**
     * The account that is currently the selected account on screen
     */
    val currentUserSessionFlow: Flow<UserSessionWithPersonAndLearningSpace>
        get() = _currentUserSession.asStateFlow()

    //This is the older way of doing things now.
    val currentAccount: UmAccount
        get() = _currentUserSession.value.toUmAccount()


    /**
     * Endpoint urls that have active sessions that are on the device. This is stored in preferences
     * so that we know what databases we need to collect from
     */
    private val _learningSpacesWithActiveSessions: MutableStateFlow<List<LearningSpace>>

    /**
     * Flow that can be accessed to access all active accounts (e.g. the current user session AND
     * all accounts that are stored on this device that the user could switch to).
     */
    private val _activeUserSessions: MutableStateFlow<List<UserSessionWithPersonAndLearningSpace>>


    val activeUserSessionsFlow: Flow<List<UserSessionWithPersonAndLearningSpace>>
        get() = _activeUserSessions.asStateFlow()

    /**
     * Flow that is use to show prompt to compose ui , to tell user that option to create passkey for
     * easy login
     */

    private val _passKeyPromptFlow = MutableSharedFlow<PassKeyPromptData>()

    val passKeyPromptFlow: Flow<PassKeyPromptData>
        get() = _passKeyPromptFlow


    val activeLearningSpace: LearningSpace
        get() = _currentUserSession.value.learningSpace

    val activeLearningSpaces: List<LearningSpace>
        get() = _learningSpacesWithActiveSessions.value


    private val createNewLocalAccountUseCase: CreateNewLocalAccountUseCase by di.instance()

    fun interface EndpointFilter {

       fun filterEndpoint(endpointUrl: String): Boolean

    }

    private val httpClient: HttpClient by di.instance()

    private val json: Json by di.instance()

    private val apiUrlConfig: ApiUrlConfig by di.instance()

    private val closed = atomic(false)

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        val currentEndpointStr = settings.getStringOrNull(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY)
            ?: apiUrlConfig.presetApiUrl ?: MANIFEST_URL_FALLBACK
        val currentDb: UmAppDatabase = di.direct.on(LearningSpace(currentEndpointStr))
            .instance(tag = DoorTag.TAG_DB)

        val initUserSession: UserSessionWithPersonAndLearningSpace = settings.getStringOrNull(ACCOUNTS_ACTIVE_SESSION_PREFKEY)?.let {
            json.decodeFromString(it)
        } ?: makeNewTempGuestSession(currentEndpointStr,  currentDb)
        _currentUserSession = MutableStateFlow(initUserSession)
        val initEndpoints: List<String> = settings.getStringOrNull(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION)?.let {
            json.decodeFromString(ListSerializer(String.serializer()), it)
        } ?: listOf(currentEndpointStr)
        _learningSpacesWithActiveSessions = MutableStateFlow(initEndpoints.map { LearningSpace(it) })

        _activeUserSessions = MutableStateFlow(listOf(initUserSession))

        //Note: might need to collect all active endpoints and use that to watch for any session
        //invalidations

        /**
         * Provide a flow (as/when required) of all active sessions. This requires using flows
         * from multiple different databases. The database flows will only be collected if/when the
         * all active accounts flow is being collected.
         */
        scope.launch {
//            //Previous versions might not save this properly in settings
//            //Added 7/Jul/24. Should be removed aug-sept.
//            if(initUserSession.person.let { it.firstNames == null || it.lastName == null }) {
//                val initSessionPerson = currentDb.personDao().findByUidAsync(
//                    initUserSession.person.personUid)
//                if(initSessionPerson?.firstNames != null && initSessionPerson.lastName != null) {
//                    _currentUserSession.update { prev ->
//                        prev.copy(person = initSessionPerson)
//                    }
//                }
//            }

            _activeUserSessions.whenSubscribed {
                _learningSpacesWithActiveSessions.collectLatest { learningSpacesWithSessions ->
                    learningSpacesWithSessions.forEach { learningSpace ->
                        scope.launch {
                            val endpointDb: UmAppDatabase = di.on(learningSpace).direct
                                .instance(tag = DoorTag.TAG_DB)
                            endpointDb.userSessionDao().findAllLocalSessionsLive().collect { learningSpaceSessions ->
                                _activeUserSessions.update { prev ->
                                    prev.filter {
                                        it.learningSpace != learningSpace
                                    } +  learningSpaceSessions.map {
                                        UserSessionWithPersonAndLearningSpace(
                                            userSession = it.userSession ?: UserSession(),
                                            person = it.person ?: Person(),
                                            learningSpace = learningSpace,
                                            personPicture = it.personPicture
                                        )
                                    }.sortedBy { it.displayName }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * When the current user session flow is being collected, then collect a flow from the
         * database of the picture and name. If these change, then update the flow.
         *
         * We can't just do this direct from the database because this flow is based on data stored
         * in settings.
         */
        scope.launch {
            _currentUserSession.whenSubscribed {
                _currentUserSession.collectLatest { session ->
                    val learningSpaceDb: UmAppDatabase = di.on(session.learningSpace).direct
                        .instance(tag = DoorTag.TAG_DB)
                    learningSpaceDb.personDao().findByUidWithPictureAsFlow(
                        session.userSession.usPersonUid
                    ).collect { personAndPictureFromDb ->
                        val nameChanged = personAndPictureFromDb?.person?.fullName() != session.person.fullName()
                        val pictureUriChanged = personAndPictureFromDb?.picture?.personPictureThumbnailUri !=
                                session.personPicture?.personPictureThumbnailUri
                        if(nameChanged || pictureUriChanged) {
                            currentUserSession = session.copy(
                                person = if(nameChanged) {
                                    session.person.shallowCopy {
                                        firstNames = personAndPictureFromDb?.person?.firstNames
                                        lastName = personAndPictureFromDb?.person?.lastName
                                    }
                                }else {
                                    session.person
                                },
                                personPicture = if(pictureUriChanged) {
                                    personAndPictureFromDb?.picture
                                }else {
                                    session.personPicture
                                }
                            )
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
        learningSpaceUrl: String,
        currentDb: UmAppDatabase
    ): UserSessionWithPersonAndLearningSpace {
        return UserSessionWithPersonAndLearningSpace(
            userSession = UserSession().apply {
                usUid = currentDb.doorPrimaryKeyManager.nextId(UserSession.TABLE_ID)
                usClientNodeId = currentDb.doorWrapperNodeId
                usStartTime = systemTimeInMillis()
                usSessionType = (UserSession.TYPE_TEMP_LOCAL or UserSession.TYPE_GUEST)
                usStatus = UserSession.STATUS_ACTIVE
            },
            person = GUEST_PERSON,
            learningSpace = LearningSpace(learningSpaceUrl),
        ).also {
            scope.launch {
                currentDb.userSessionDao().insertSession(it.userSession)
            }
        }
    }

    private fun assertNotClosed() {
        if(closed.value)
            throw IllegalStateException("UstadAccountManager is closed")
    }




    /**
     * Get a list of all accounts that are on the system across all endpoints
     */
    suspend fun activeSessionsList(
        endpointFilter: EndpointFilter = EndpointFilter { true }
    ): List<UserSessionWithPersonAndLearningSpace> {
        return _learningSpacesWithActiveSessions.value.filter { endpointFilter.filterEndpoint(it.url) }.flatMap { endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            db.userSessionDao().findAllLocalSessionsAsync().map { userSession ->
                userSession.withLearningSpace(endpoint)
            }
        }
    }

    /**
     *
     */
    suspend fun activeSessionCount(
        maxDateOfBirth: Long = 0,
        endpointFilter: EndpointFilter = EndpointFilter { true }
    ): Int {
        return _learningSpacesWithActiveSessions.value.filter { endpointFilter.filterEndpoint(it.url) }.fold(0) { total, endpoint ->
            val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
            total + db.userSessionDao().countAllLocalSessionsAsync(maxDateOfBirth)
        }
    }

   suspend fun createPassKeyPrompt(
       username: String,
       personUid: Long,
       doorNodeId: String,
       usStartTime: Long,
       serverUrl: String
   ) {
        val promptData = PassKeyPromptData(
            username = username,
            personUid = personUid,
            doorNodeId=doorNodeId,
            usStartTime=usStartTime,
            serverUrl=serverUrl
        )

       _passKeyPromptFlow.emit(promptData)

   }
    suspend fun registerWithPasskey(
        learningSpaceUrl: String,
        passkeyResult: PasskeyResult,
        person: Person,
        personPicture: PersonPicture?,
    ) {
        val learningSpace = LearningSpace(learningSpaceUrl)

        val savePassKeyUseCase: SavePersonPasskeyUseCase = di.on(learningSpace).direct.instance()
        savePassKeyUseCase.invoke(passkeyResult)

        //Must ensure that the site object is loaded to get auth salt.
        val repo: UmAppDatabase = di.on(learningSpace).direct.instance<UmAppDataLayer>()
            .repositoryOrLocalDb
        getSiteFromDbOrLoadFromHttp(repo)
        if(repo.personDao().findByUidAsync(person.personUid) == null) {
            repo.personDao().insertAsync(person)
        }
        val session = addSession(person, learningSpaceUrl, null)

        currentUserSession = session


    }

    suspend fun register(
        person: Person,
        password: String,
        learningSpaceUrl: String,
        accountRegisterOptions: AccountRegisterOptions = AccountRegisterOptions()
    ): Person = withContext(Dispatchers.Default){
        assertNotClosed()
        val learningSpace = LearningSpace(learningSpaceUrl)
        val parentVal = accountRegisterOptions.parentJoin
        val httpStmt = httpClient.preparePost {
            url("${learningSpaceUrl.removeSuffix("/")}/auth/register")
            contentType(ContentType.Application.Json)
            setBodyJson(
                json = json,
                serializer = RegisterRequest.serializer(),
                value = RegisterRequest(
                    person = person,
                    newPassword = password,
                    parent = parentVal,
                    learningSpaceUrl = learningSpaceUrl
                )
            )
        }

        val (registeredPerson: Person?, status: Int) = httpStmt.execute { response ->
            if(response.status.value == 200) {
                Pair(json.decodeFromString<Person>(response.bodyAsText()), 200)
            }else {
                Pair(null, response.status.value)
            }
        }

        if(status == 200 && registeredPerson != null) {
            //Must ensure that the site object is loaded to get auth salt.
            val repo: UmAppDatabase = di.on(learningSpace).direct.instance<UmAppDataLayer>()
                .requireRepository()
            getSiteFromDbOrLoadFromHttp(repo)

            val session = addSession(registeredPerson, learningSpaceUrl, password)

            //If the person is not loaded into the database (probably not), then put in the db.
            val db: UmAppDatabase = di.on(learningSpace).direct.instance(tag = DoorTag.TAG_DB)
            db.withDoorTransactionAsync {
                if(db.personDao().findByUidAsync(registeredPerson.personUid) == null) {
                    db.personDao().insertAsync(registeredPerson)
                }
            }

            if(accountRegisterOptions.makeAccountActive){
                currentUserSession = session
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
        learningSpaceUrl: String,
        password: String?
    ) : UserSessionWithPersonAndLearningSpace{
        assertNotClosed()
        val learningSpace = LearningSpace(learningSpaceUrl)
        val dataLayer: UmAppDataLayer = di.on(learningSpace).direct.instance()

        if(learningSpace !in _learningSpacesWithActiveSessions.value) {
            addActiveLearningSpace(learningSpace, commit = false)
            commitActiveEndpointsToPref()
        }

        val authManager: AuthManager = di.on(learningSpace).direct.instance()

        val (userSession, personPicture) = dataLayer.repositoryOrLocalDb.withDoorTransactionAsync {
            val nodeId = di.on(learningSpace).direct.instance<NodeIdAndAuth>().nodeId
            val userSession = UserSession().apply {
                usClientNodeId = nodeId
                usPersonUid = person.personUid
                usStartTime = systemTimeInMillis()
                usSessionType = UserSession.TYPE_STANDARD
                usStatus = UserSession.STATUS_ACTIVE
                usAuth = password?.let { authManager.encryptPbkdf2(it).toHexString() }
                usUid = dataLayer.repositoryOrLocalDb.userSessionDao().insertSession(this)
            }
            val personPicture = dataLayer.localDb.personPictureDao().findByPersonUidAsync(
                person.personUid)
            userSession to personPicture
        }

        return UserSessionWithPersonAndLearningSpace(userSession, person, learningSpace, personPicture)
    }

    @Suppress("RedundantSuspendModifier") // Reserved for use if required as per previous api
    private suspend fun addActiveLearningSpace(learningSpace: LearningSpace, commit: Boolean = true) {
        _learningSpacesWithActiveSessions.update { prev ->
            prev + listOf(learningSpace)
        }

        if(commit)
            commitActiveEndpointsToPref()
    }

    private fun removeActiveLearningSpace(learningSpace: LearningSpace, commit: Boolean = true) {
        _learningSpacesWithActiveSessions.update { prev ->
            prev.filter { it != learningSpace }
        }
        if(commit)
            commitActiveEndpointsToPref()
    }

    private fun commitActiveEndpointsToPref() {
        val json = Json.encodeToString(ListSerializer(String.serializer()),
            _learningSpacesWithActiveSessions.value.toSet().map { it.url }.toList())
        settings[ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION] = json
    }

    //When sync data comes in, check to see if a change has been actioned that has ended our active
    // session
    suspend fun onIncomingMessageReceived(
        message: DoorMessage
    ) {
        val deactivatedCurrentSession = message.replications.firstOrNull {
            it.tableId == UserSession.TABLE_ID &&
                    it.entity["usUid"]?.jsonPrimitive?.longOrNull == currentUserSession.userSession.usUid &&
                    it.entity["usStatus"]?.jsonPrimitive?.intOrNull != UserSession.STATUS_ACTIVE
        }

        if(deactivatedCurrentSession != null) {
            //current session was deactivated. The session itself will be updated on the underlying database
            startGuestSession(currentUserSession.learningSpace.url)
        }
    }

    suspend fun endSession(
        session: UserSessionWithPersonAndLearningSpace,
        endStatus: Int = UserSession.STATUS_LOGGED_OUT,
        endReason: Int = UserSession.REASON_LOGGED_OUT
    ) {
        val dataLayer: UmAppDataLayer = di.on(session.learningSpace).direct.instance()

        dataLayer.repositoryOrLocalDb.userSessionDao().endSession(
            sessionUid = session.userSession.usUid,
            newStatus = endStatus,
            reason = endReason,
            endTime = systemTimeInMillis()
        )

        //check if the active session has been ended.
        if(currentUserSession.userSession.usUid == session.userSession.usUid
            && currentUserSession.learningSpace == session.learningSpace) {
            currentUserSession = makeNewTempGuestSession(session.learningSpace.url, dataLayer.repositoryOrLocalDb)
        }


        if(activeSessionsList { it == session.learningSpace.url }.isEmpty()) {
            removeActiveLearningSpace(session.learningSpace)
        }
    }
    suspend fun loginWithPasskey(
        passKeySignInData: PassKeySignInData,
        currentServerUrl:String
    ) = withContext(Dispatchers.Default){
        assertNotClosed()
        val userHandle = passKeySignInData.userHandle.base64StringToByteArray()
        val endpointUrl= userHandle.decodeToString().substringAfter("@")
        val loginResponse = httpClient.post {
            url("${endpointUrl.removeSuffix("/")}/api/passkey/verifypasskey")
            parameter("id", passKeySignInData.credentialId)
            parameter("userHandle", passKeySignInData.userHandle)
            parameter("authenticatorData", passKeySignInData.authenticatorData)
            parameter("clientDataJSON", passKeySignInData.clientDataJSON)
            parameter("signature", passKeySignInData.signature)
            parameter("origin", passKeySignInData.origin)
            parameter("rpId", passKeySignInData.rpId)
            parameter("challenge", passKeySignInData.challenge)
        }.bodyAsText()
        Napier.d { "passkeyres"+loginResponse.toString() }
       val passkeyVerifyResult= Json.decodeFromString<PasskeyVerifyResult>(loginResponse)

        if(!passkeyVerifyResult.isVerified) {
            throw UnauthorizedException("Account not found")
        }
        val repo: UmAppDatabase by di.on(LearningSpace(endpointUrl)).instance(tag = DoorTag.TAG_REPO)

        //Make sure that we fetch the person and personpicture into the database.
        val personAndPicture = repo.personDao().findByUidWithPicture(
            passkeyVerifyResult.personUid) ?: throw IllegalStateException("Cannot find person in repo/db")
        val personInDb = personAndPicture.person!! //Cannot be null based on query


        val repoWithCurrentUrl: UmAppDatabase by di.on(LearningSpace(currentServerUrl)).instance(tag = DoorTag.TAG_REPO)

        getSiteFromDbOrLoadFromHttp(repoWithCurrentUrl)

        val newSession = addSession(personInDb, currentServerUrl, "")
            currentUserSession = newSession


    }



    suspend fun login(
        username: String,
        password: String,
        endpointUrl: String,
        maxDateOfBirth: Long = 0L,
        dontSetCurrentSession: Boolean = false
    ): UmAccount = withContext(Dispatchers.Default){
        assertNotClosed()

        val dataLayer: UmAppDataLayer = di.direct.on(LearningSpace(endpointUrl)).instance()

        val nodeId = (dataLayer.repository as? DoorDatabaseRepository)?.config?.nodeId
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

        val responseAccount: UmAccount = json.decodeFromString(loginResponse.bodyAsText())
        responseAccount.endpointUrl = endpointUrl

        //Make sure that we fetch the person and personpicture into the database.
        val personAndPicture = dataLayer.repositoryOrLocalDb.personDao().findByUidWithPicture(
            responseAccount.personUid) ?: throw IllegalStateException("Cannot find person in repo/db")
        val personInDb = personAndPicture.person!! //Cannot be null based on query

        getSiteFromDbOrLoadFromHttp(dataLayer.repositoryOrLocalDb)

        val newSession = addSession(personInDb, endpointUrl, password)
        if(!dontSetCurrentSession) {
            currentUserSession = newSession
        }

        //This should not be needed - as responseAccount can be smartcast, but will not otherwise compile
        responseAccount
    }

    private suspend fun getSiteFromDbOrLoadFromHttp(
        repo: UmAppDatabase
    ) {
        val db = (repo as DoorDatabaseRepository).db as UmAppDatabase
        val siteInDb = db.siteDao().getSiteAsync()
        if(siteInDb == null) {
            repo.siteDao().getSiteAsync() ?: throw IllegalStateException("Internal error: no Site in database and could not fetch it from server")
        }
    }

    suspend fun createLocalAccount(): UserSessionWithPersonAndLearningSpace {
        val localAccountResult = createNewLocalAccountUseCase(Person())
        return addSession(
            person = localAccountResult.person,
            learningSpaceUrl = localAccountResult.learningSpace.url,
            password = null
        ).also {
            currentUserSession = it
        }
    }

    suspend fun startGuestSession(learningSpaceUrl: String): UserSessionWithPersonAndLearningSpace {
        val dataLayer: UmAppDataLayer = di.on(LearningSpace(learningSpaceUrl)).direct.instance()
        val guestPerson = dataLayer.repositoryOrLocalDb.insertPersonAndGroup(Person().apply {
            username = null
            firstNames = "Guest"
            lastName = "User"
            personType = Person.TYPE_GUEST
        }, groupFlag = PERSONGROUP_FLAG_PERSONGROUP or PERSONGROUP_FLAG_GUESTPERSON)

        getSiteFromDbOrLoadFromHttp(dataLayer.repositoryOrLocalDb)

        val guestSession = addSession(guestPerson, learningSpaceUrl, null)
        currentUserSession = guestSession
        return guestSession
    }

    fun close() {
        if(!closed.getAndSet(true)) {
            scope.cancel()
        }
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
         * The AccountType (if used)
         */
        const val ACCOUNT_TYPE = "com.ustadmobile"

        /**
         * Intent action indicating that the caller wants to get an authentication token
         */
        @Suppress("unused")
        const val ACTION_GET_AUTH_TOKEN = "com.ustadmobile.AUTH_GET_TOKEN"

    }

}