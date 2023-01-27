package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.*
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

abstract class UstadViewModel(
    override val di: DI,
    protected val savedStateHandle: UstadSavedStateHandle,
): ViewModel(savedStateHandle), DIAware {


    protected val navController = CommandFlowUstadNavController()

    val navCommandFlow = navController.commandFlow

    protected val _appUiState = MutableStateFlow(AppUiState())

    val appUiState: Flow<AppUiState> = _appUiState.asStateFlow()

    protected val accountManager: UstadAccountManager by instance()

    protected val activeDb: UmAppDatabase by on(accountManager.activeEndpoint)
        .instance(tag = DoorTag.TAG_DB)

    protected val activeRepo: UmAppDatabase by on(accountManager.activeEndpoint)
        .instance(tag = DoorTag.TAG_REPO)

    protected val navResultReturner: NavResultReturner by instance()

    protected val json: Json by instance()

    protected val snackDispatcher: SnackBarDispatcher by instance()

    private val navResultTimestampsCollected: MutableSet<Long> by lazy {
        savedStateHandle[KEY_COLLECTED_TIMESTAMPS]?.split(",")
            ?.map { it.trim().toLong() }?.toMutableSet() ?: mutableSetOf()
    }


    init {

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
     * When using
     */
    suspend fun NavResultReturner.collectReturnedResults(
        key: String,
        collector: FlowCollector<NavResult>
    ) {
        return resultFlowForKey(key).filter {
            it.timestamp !in navResultTimestampsCollected
        }.collect {
            collector.emit(it)
            navResultTimestampsCollected += it.timestamp
            savedStateHandle[KEY_COLLECTED_TIMESTAMPS] = navResultTimestampsCollected
                .joinToString(separator = ",")
        }
    }

    protected inline fun <reified T> UstadSavedStateHandle.getJson(key: String): T? {
        return get(key)?.let { json.decodeFromString<T>(it) }
    }

    protected inline fun <reified T> UstadSavedStateHandle.setJson(
        key: String,
        value: T
    ) {
        set(key, json.encodeToString(value))
    }

    protected inline fun <reified T> UstadSavedStateHandle.getOrPutJson(
        key: String,
        makeBlock: () -> T,
    ): T {
        return getJson(key) ?: makeBlock().also {
            setJson(key, it)
        }
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
        val args = if(viewName.contains("?")) {
            UMFileUtil.parseURLQueryString(viewUri)
        }else {
            mapOf()
        }

        navigate(viewName, args, goOptions)

    }

    companion object {
        /**
         * Saved state key for the current value of the entity itself. This is different to
         * ARG_ENTITY_JSON which provides a starting value
         */
        const val KEY_ENTITY_STATE = "entityState"

        const val KEY_COLLECTED_TIMESTAMPS = "collectedTs"
    }

}