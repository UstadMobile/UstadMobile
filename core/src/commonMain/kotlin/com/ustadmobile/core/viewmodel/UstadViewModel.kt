package com.ustadmobile.core.viewmodel

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.openlink.OnClickLinkUseCase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.nav.*
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.isDateOfBirthAnAdult
import com.ustadmobile.core.util.ext.isGuestUser
import com.ustadmobile.core.util.ext.localFirstThenRepoIfFalse
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.util.ext.putFromSavedStateIfPresent
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_KEY
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_VIEWNAME
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.errors.ErrorViewModel
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.kodein.di.*
import moe.tlaster.precompose.viewmodel.ViewModel as PreComposeViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope as preComposeViewModelScope

/**
 * @param di the KodeIn DI
 * @param savedStateHandle the SavedStateHandle
 * @param destinationName The name of this destination as per the navigation view stack, normally as
 * per the related VIEW_NAME. This might NOT be the VIEW_NAME that relates to this screen e.g. when
 * this ViewModel is being used within a tab or other component that is not directly part of the
 * navigation.
 */
abstract class UstadViewModel(
    override val di: DI,
    protected val savedStateHandle: UstadSavedStateHandle,
    protected val destinationName: String,
): PreComposeViewModel(), DIAware {

    protected val navController = CommandFlowUstadNavController()

    val navCommandFlow = navController.commandFlow

    @Suppress("PropertyName")
    protected val _appUiState = MutableStateFlow(AppUiState())

    val appUiState: Flow<AppUiState> = _appUiState.asStateFlow()

    protected val accountManager: UstadAccountManager by instance()

    val viewModelScope: CoroutineScope
        get() = preComposeViewModelScope

    /**
     * Shorthand to get the person uid of the active user (if any).
     */
    protected val activeUserPersonUid: Long
        get() = accountManager.currentUserSession.person.personUid

    internal val activeDb: UmAppDatabase by on(accountManager.activeEndpoint)
        .instance(tag = DoorTag.TAG_DB)

    internal val activeRepo: UmAppDatabase by on(accountManager.activeEndpoint)
        .instance(tag = DoorTag.TAG_REPO)

    protected val navResultReturner: NavResultReturner by instance()

    internal val json: Json by instance()

    protected val snackDispatcher: SnackBarDispatcher by instance()

    protected val resultReturner: NavResultReturner by instance()

    internal val systemImpl: UstadMobileSystemImpl by instance()

    protected val onClickLinkUseCase: OnClickLinkUseCase by lazy {
        OnClickLinkUseCase(
            navController = navController,
            accountManager = accountManager,
            openExternalLinkUseCase = di.direct.instance(),
            apiUrlConfig = di.direct.instance(),
        )
    }

    private var lastNavResultTimestampCollected: Long = savedStateHandle[KEY_LAST_COLLECTED_TS]?.toLong() ?: 0L
        set(value) {
            field = value
            savedStateHandle[KEY_LAST_COLLECTED_TS] = value.toString()
        }

    /**
     * If navigation for a result is in progress, this will be non-null
     */
    protected val expectedResultDest: NavResultDest?
        get()  {
            val popUpToViewName = savedStateHandle[ARG_RESULT_DEST_VIEWNAME]
            val saveToKey = savedStateHandle[ARG_RESULT_DEST_KEY]
            return if(popUpToViewName != null && saveToKey != null) {
                NavResultDest(popUpToViewName, saveToKey)
            }else {
                null
            }
        }

    init {
        if(lastNavResultTimestampCollected == 0L)
            lastNavResultTimestampCollected = systemTimeInMillis()
    }
    /**
     * Shorthand to make it easier to update the loading state
     */
    protected var loadingState: LoadingUiState
        get() = _appUiState.value.loadingState
        set(value) {
            _appUiState.update {
                it.copy(loadingState = value)
            }
        }

    /**
     * Shorthand to set the title
     */
    protected var title: String?
        get() = _appUiState.value.title
        set(value) {
            _appUiState.update {
                it.copy(title = value)
            }
        }

    /**
     * Shorthand to observe results. Avoids two edge cases:
     *
     * 1. "Replay" - when the ViewModel is recreated, if no other result has been returned in the
     *    meantime, the last result would be collected again. The flow of NavResultReturner always
     *    replays the most recent result returned (required to allow a collector which starts after
     *    the result was sent to collect it).
     *
     *    This is avoided by tracking the timestamp of the last item collected.
     *
     * 2. Replay from previous viwemodel: when the user goes from screen A to screen B, then C,
     *    returns a result to screen A, and then navigates forward to screen B again with new arguments.
     *    The new instance of screen B does not remember receiving any results, so the result from
     *    the old instance of screen C looks new.
     *
     *    This is avoided by setting the alstNavResultTimestampCollected to the first start time
     *    on init.
     *
     */
    fun NavResultReturner.filteredResultFlowForKey(
        key: String,
    ) : Flow<NavResult> {
        return resultFlowForKey(key).filter {
            val isNew = it.timestamp > lastNavResultTimestampCollected
            if(isNew)
                lastNavResultTimestampCollected = it.timestamp

            isNew
        }
    }

    protected suspend fun <T> UstadSavedStateHandle.getJson(
        key: String,
        deserializer: DeserializationStrategy<T>
    ): T? {
        val jsonStr = get(key)
        return if(jsonStr != null) {
            withContext(Dispatchers.Default) {
                json.decodeFromString(deserializer, jsonStr)
            }
        }else {
            null
        }
    }

    protected suspend fun <T> UstadSavedStateHandle.setJson(
        key: String,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val jsonStr = withContext(Dispatchers.Default){
            json.encodeToString(serializer, value)
        }
        set(key, jsonStr)
    }

    /**
     * Parse the query parameters (if any). this is a placeholder, will be removed when the authenticator
     * branch is merged.
     */
    protected fun UstadNavController.navigateToViewUri(
        viewUri: String,
        goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions.Default,
    ) {
        val viewName = viewUri.substringBefore("?")
        val args = if(viewUri.contains("?")) {
            UMFileUtil.parseURLQueryString(viewUri)
        }else {
            mapOf()
        }

        navigate(viewName, args, goOptions)

    }

    /**
     * Return a result to the screen that is expecting it, if any. See CODING-STYLE.md README for an
     * overview of how this works.
     *
     * @param result: the result that is being provided (e.g. selected Person etc)
     */
    protected fun finishWithResult(result: Any?) {
        val resultDest = expectedResultDest
        if(resultDest != null) {
            navResultReturner.sendResult(NavResult(resultDest.key, systemTimeInMillis(), result))
            navController.popBackStack(resultDest.viewName, false)
        }else {
            navController.popBackStack(UstadView.CURRENT_DEST, true)
        }
    }


    fun <T> navigateForResult(
        nextViewName: String,
        key: String,
        currentValue: T?,
        serializer: SerializationStrategy<T>,
        args: Map<String, String> = emptyMap(),
        goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions.Default,
        overwriteDestination: Boolean = (this is UstadEditViewModel),
    ) {
        val navArgs = args.toMutableMap()

        navArgs.takeIf { !overwriteDestination }?.putFromSavedStateIfPresent(ARG_RESULT_DEST_KEY)

        if(!navArgs.containsKey(ARG_RESULT_DEST_KEY) || overwriteDestination)
            navArgs[ARG_RESULT_DEST_KEY] = key

        navArgs.takeIf { !overwriteDestination }?.putFromSavedStateIfPresent(ARG_RESULT_DEST_VIEWNAME)

        if(!navArgs.containsKey(ARG_RESULT_DEST_VIEWNAME) || overwriteDestination)
            navArgs[ARG_RESULT_DEST_VIEWNAME] = destinationName

        if(currentValue != null) {
            navArgs[UstadEditView.ARG_ENTITY_JSON] = json.encodeToString(serializer, currentValue)
        }

        navController.navigate(nextViewName, navArgs.toMap(), goOptions)
    }

    fun navigateToEditHtml(
        currentValue: String?,
        resultKey: String,
        title: String? = null,
        extraArgs: Map<String, String> = emptyMap(),
    ) {
        navController.navigate(
            viewName = HtmlEditViewModel.DEST_NAME,
            args = buildMap {
                put(HtmlEditViewModel.ARG_HTML, (currentValue ?: ""))
                put(ARG_RESULT_DEST_KEY, resultKey)
                put(ARG_RESULT_DEST_VIEWNAME, destinationName)
                title?.also { put(HtmlEditViewModel.ARG_TITLE, it ) }
                putAll(extraArgs)
            }
        )
    }

    /**
     * Load an entity for editing:
     *
     * 1. Try to load from JSON in saved state. This normally looks at KEY_ENTITY_STATE first,
     *    then ARG_ENTITY_JSON for a value that has been passed as an argument.
     * 2. If nothing found in loadFromStateKeys, then try to load from the database and then the
     *    repository. If loading from the repository fails, use the value from the database.
     * 3. If nothing is found in the database/repository, then make a default value
     *
     * @param loadFromStateKeys a list of keys to look for a saved value (in order to check).
     * @param savedStateKey the key that should be used to save the current value (e.g. as the user
     *        edits).
     * @param onLoadFromDb a function that will load the given entity from the database and/or repo
     * @param makeDefault a function that will create a default value if nothing is found in the
     *        database or repo.
     * @param uiUpdate the functin that will update the UI to display the given entity. When loading
     *        from the database/repository, the value that is loaded from the local database will
     *        be displayed first (so the user sees this whilst the value is loading from the repo).
     * @param T the entity type
     */
    protected suspend fun <T> loadEntity(
        serializer: KSerializer<T>,
        loadFromStateKeys: List<String> = listOf(KEY_ENTITY_STATE, UstadEditView.ARG_ENTITY_JSON),
        savedStateKey: String = loadFromStateKeys.first(),
        onLoadFromDb: (suspend (UmAppDatabase) -> T?)?,
        makeDefault: suspend () -> T?,
        uiUpdate: (T?) -> Unit,
    ) : T? {

        loadFromStateKeys.forEach { key ->
            val savedVal: T? = savedStateHandle.getJson(key, serializer)
            if(savedVal != null) {
                uiUpdate(savedVal)
                return savedVal
            }
        }

        val dbVal = onLoadFromDb?.invoke(activeDb)
        if(dbVal != null) {
            uiUpdate(dbVal)
        }

        return try {
            val repoVal = onLoadFromDb?.invoke(activeRepo) ?: makeDefault()
            if(repoVal != null)
                savedStateHandle.setJson(savedStateKey, serializer, repoVal)
            uiUpdate(repoVal)
            repoVal
        }catch(e: Exception) {
            //could happen when connectivity is not so good
            if(dbVal != null)
                savedStateHandle.setJson(savedStateKey, serializer, dbVal)

            dbVal ?: makeDefault().also(uiUpdate)
        }
    }

    /**
     * Launch a given codeblock if the permission check passes. Otherwise navigate to an error screen
     */
    protected fun launchIfHasPermission(
        permissionCheck: suspend (UmAppDatabase) -> Boolean,
        setLoadingState: Boolean = false,
        onSetFieldsEnabled: ((Boolean) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        if(setLoadingState) {
            _appUiState.update { prev -> prev.copy(loadingState = LoadingUiState.INDETERMINATE) }
        }
        onSetFieldsEnabled?.invoke(false)

        viewModelScope.launch {
            try {
                if(!activeRepo.localFirstThenRepoIfFalse(permissionCheck)) {
                    navController.navigate(
                        ErrorViewModel.DEST_NAME,
                        emptyMap(),
                        goOptions = UstadMobileSystemCommon.UstadGoOptions(
                            popUpToViewName = destinationName, popUpToInclusive = true
                        )
                    )
                }else {
                    block()
                }
            }finally {
                _appUiState.update { prev -> prev.copy(loadingState = LoadingUiState.NOT_LOADING) }
                onSetFieldsEnabled?.invoke(true)
            }
        }
    }


    /**
     * If the given key is present in the savedStateHandle for this ViewModel, then put it into
     * the Receiver MutableMap. This can be convenient for forwarding arguments when navigating
     */
    fun MutableMap<String, String>.putFromSavedStateIfPresent(key: String) {
        putFromSavedStateIfPresent(savedStateHandle, key)
    }

    fun MutableMap<String, String>.putFromSavedStateIfPresent(keys: List<String>) {
        keys.forEach {
            putFromSavedStateIfPresent(it)
        }
    }


    /**
     * Run an asynchronous block of code e.g. when saving an item. Whilst the item is being saved,
     * show progress indicator on the app bar and disable the app bar button (to avoid any possibility
     * of accidental/anger clicks)
     *
     * @param onSetFieldsEnabled
     */
    fun launchWithLoadingIndicator(
        onSetFieldsEnabled: (Boolean) -> Unit,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSetFieldsEnabled(false)
                _appUiState.update { prev ->
                    prev.copy(
                        loadingState = LoadingUiState.INDETERMINATE,
                        actionBarButtonState = prev.actionBarButtonState.copy(
                            enabled = false,
                        )
                    )
                }
                block()
            }finally {
                onSetFieldsEnabled(true)
                _appUiState.update { prev ->
                    prev.copy(
                        loadingState = LoadingUiState.NOT_LOADING,
                        actionBarButtonState = prev.actionBarButtonState.copy(
                            enabled = true,
                        )
                    )
                }
            }
        }
    }

    /**
     * If the user is logged in with an acceptable account, then run the given block. Else, take
     * them to the login screen with the next destination parameters set (e.g. so they can come back
     * once they have logged in).
     */
    protected fun ifLoggedInElseNavigateToLoginWithNextDestSet(
        requireAdultAccount: Boolean = false,
        args: Map<String, String>,
        block: () -> Unit
    ) {
        val apiUrlConfig: ApiUrlConfig by instance()

        if(
            accountManager.currentUserSession.person.let {
                it.isGuestUser() ||
                (requireAdultAccount && !Instant.fromEpochMilliseconds(it.dateOfBirth).isDateOfBirthAnAdult())
            }
        ) {
            navController.navigateToLink(
                link = "$destinationName?${args.toQueryString()}",
                accountManager = accountManager,
                openExternalLinkUseCase = { _, _ -> Unit },
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = destinationName, popUpToInclusive = true
                ),
                userCanSelectServer = apiUrlConfig.canSelectServer,
            )
        }else {
            block()
        }
    }

    /**
     * Create a new Xapi session based on the accountmanager and argument values for this ViewModel
     */
    protected fun createXapiSession(
        contentEntryUid: Long = 0,
        clazzUid: Long = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0,
        cbUid: Long = savedStateHandle[ARG_COURSE_BLOCK_UID]?.toLong() ?: 0,
    ): XapiSession {
        return XapiSession(
            endpoint = accountManager.activeEndpoint,
            accountPersonUid = activeUserPersonUid,
            accountUsername = accountManager.currentUserSession.person.username ?: "anonymous",
            clazzUid = clazzUid,
            cbUid = cbUid,
            contentEntryUid = contentEntryUid,
            rootActivityId = "${accountManager.activeEndpoint.url}ns/xapi/contentEntry/$contentEntryUid",
            registrationUuid = uuid4().toString(),
        )
    }

    companion object {
        /**
         * Saved state key for the current value of the entity itself. This is different to
         * ARG_ENTITY_JSON which provides a starting value
         */
        const val KEY_ENTITY_STATE = "entityState"

        const val KEY_LAST_COLLECTED_TS = "collectedTs"

        const val KEY_INIT_STATE = "initState"

        const val ARG_TIME_ZONE = "timeZone"

        const val ARG_PARENT_UID = "parentUid"

        const val ARG_COURSE_BLOCK_UID = "courseBlockUid"

        const val ARG_ENTITY_UID = "entityUid"

        /**
         * Used by Login and SiteEnterLink
         */
        const val ARG_SERVER_URL = "serverUrl"

        /**
         * Commonly used to pass the next view uri to go to after completing an action e.g. after
         * login, account selection, registration, etc.
         */
        const val ARG_NEXT = "next"

        /**
         * Used to 'enforce' a minimum age policy for certain screens (e.g. parent - child consent
         * management). This can be used on AccountList to show only adult accounts (e.g.
         * date of birth < MAX DATE OF BIRTH), or LoginPresenter where an adult account is required.
         */
        const val ARG_MAX_DATE_OF_BIRTH = "maxDob"

        val ROOT_DESTINATIONS = listOf(
            ClazzListViewModel.DEST_NAME_HOME,
            ContentEntryListViewModel.DEST_NAME_HOME,
            ConversationListViewModel.DEST_NAME_HOME,
            PersonListViewModel.DEST_NAME_HOME
        )

        /**
         * Can be used with any Android intent to provide a deep link to open within the app.
         * The link can be in the form of:
         *
         * https://endpoint.server/umapp/#/ViewName?arg=value
         * ViewName?arg=value
         */
        const val ARG_OPEN_LINK = "openLink"


        /**
         * Used together with the AccountManager
         */
        const val ARG_ACCOUNT_NAME = "account"

        /**
         * Tasks that involve multiple destinations (e.g. Login - AcceptTerms - PersonEditRegister )
         * might need to pop off multiple destinations from the stack when they are done.
         *
         * The final destination in the stack might be reachable via different routes (e.g. it might
         * have started from the login screen, or the account list screen) . The POPUPTO_ON_FINISH
         * arg is intended to be used in such situations. It can be supplied by the initiator and
         * passed through until it is used by the final destination.
         */
        const val ARG_POPUPTO_ON_FINISH = "popUpToOnFinish"


        const val ARG_INVITE_CODE = "inviteCode"

        /**
         * The ClazzUid for screens where the entity is not the clazz itself. This is generally
         * passed even when viewing related entities because it makes permission checks (which are
         * based on the Clazz) easier.
         */
        const val ARG_CLAZZUID = "clazzUid"

        /**
         * The ContentEntryUid for screens where the entity is not the content entry itself. This
         * will be passed when viewing content (e.g. to the VideoContent, EpubContent, PdfContent, etc)
         */
        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val ARG_PERSON_UID = "personUid"

        const val ARG_TITLE = "t"

        /**
         * Screens that handle selecting an account (e.g. AccountList, Login, Register) in some rare
         * scenarios should not change the active session e.g. when the user is granting or denying
         * a request for external app permission (in a separate activity) we don't want the session
         * for the main activity to be changed.
         */
        const val ARG_DONT_SET_CURRENT_SESSION = "noSessionChange"

        /**
         * Where the current user session is not changed, the next view needs to know which account
         * was selected (e.g. by UstadAccountManager, Login, Register, etc).
         */
        const val ARG_SELECTED_ACCOUNT_PERSON_UID = "selectedAccountPersonUid"

        /**
         * Where the current user session is not changed, the next view needs to know which account
         * was selected (e.g. by UstadAccountManager, Login, Register, etc).
         */
        const val ARG_SELECTED_ACCOUNT_ENDPOINT_URL = "selectedAccountEndpointUrl"

    }

}