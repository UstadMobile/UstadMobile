package com.ustadmobile.util

import com.ccfraser.muirwik.components.styles.Theme
import com.ustadmobile.mocks.db.ReactDatabase
import com.ustadmobile.core.account.ClientId
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mocks.container.ContainerMounterJs
import com.ustadmobile.model.statemanager.*
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.kodein.di.*
import redux.*
import kotlin.reflect.KProperty1

/**
 * State management which uses Redux to manage state of an app. With Redux app states can be
 * accessed on all app components.
 *
 * To listen for state change, call subscribe() and pass your functional listener, this listener
 * will be invoked when state changes.
 * To change state, call dispatch() and pass your state to be changed
 *
 * In here we have a GlobalState which holds all state that can be changed by any component within
 * the app. Like FAB visibility, Title, FAB label, FAB icon and etc.
 *
 * We have also, different state action as per element to be changed, check under
 * com.ustadmobile.model.statemanager for all state actions.
 */
object StateManager{

    private data class UmDi(var di: DI): RAction

    //App theme state action
    data class UmTheme(var theme: Theme): RAction

    private lateinit var stateStore: Store<GlobalStateSlice, RAction, WrapperAction>

    // Credit https://github.com/JetBrains/kotlin-wrappers/blob/master/kotlin-redux/README.md
    private fun <S, A, R> combineReducersInferred(reducers: Map<KProperty1<S, R>, Reducer<*, A>>):
            Reducer<S, A> { return combineReducers(reducers.mapKeys { it.key.name}) }

    //Construct dependency injection object
    private val diModule = DI.Module("UstadApp-React"){

        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), this, di)
        }

        bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(EndpointScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)
            ReactDatabase.getInstance(this, dbName)
        }

        bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            instance(tag = UmAppDatabase.TAG_DB)
        }

        constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with false

        bind<ClientId>(tag = UstadMobileSystemCommon.TAG_CLIENT_ID) with scoped(EndpointScope.Default).singleton {
            ClientId(9090)
        }

        bind<UmTheme>() with singleton{
            UmTheme(getCurrentState().theme!!)
        }

        bind<ContainerMounter>() with singleton {
            ContainerMounterJs()
        }

        bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
            XmlPullParserFactory.newInstance().also {
                it.setNamespaceAware(true)
            }
        }

        bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
            XmlPullParserFactory.newInstance()
        }

        bind<XmlSerializer>() with provider {
            instance<XmlPullParserFactory>().newSerializer()
        }
        bind<CoroutineDispatcher>(tag = UstadMobileSystemCommon.TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }

        bind<HttpClient>() with singleton {
            HttpClient(Js) {
                install(JsonFeature)
                install(HttpTimeout)
            }
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
    }

    /**
     * Function which determines state changes from actions and act accordingly
     */
    private fun reducer(state: GlobalState = GlobalState(), action: RAction): GlobalState {
        state.type = action
        return when (action) {
            is FabState -> state.copy(showFab = action.visible, onFabClicked = action.onClick,
                fabLabel = action.label, fabIcon = action.icon)
            is UmDi -> state.copy(di = action.di)
            is ToolbarTitle -> state.copy(title = action.title)
            is UmTheme -> state.copy(theme = action.theme)
            is DummyStore -> state.copy(dummyStore = action)
            is SnackBarState -> state.copy(snackBarActionLabel = action.actionLabel,
                snackBarMessage = action.message, onFabClicked = action.onClick)
            is ToolbarTabs -> state.copy(tabLabels = if(action.labels.isEmpty()) state.tabLabels else action.labels,
                tabKeys = if(action.keys.isEmpty()) state.tabKeys else action.keys,
                onTabChanged = if(action.labels.isEmpty()) state.onTabChanged else action.onTabChange,
                selectedTab = if(action.selected == null) state.selectedTab else action.selected)
            else -> state
        }
    }

    /**
     * Update a particular state
     * @param action state action to be changed
     */
    fun dispatch(action: RAction){
        stateStore.dispatch(action)
    }

    /**
     * Listen for state changes event
     * @param mListener functional listen which will be invoked when state changes
     */
    fun subscribe(mListener: (GlobalStateSlice)-> Unit){
        stateStore.subscribe { mListener(stateStore.getState()) }
    }

    /**
     * Get current state of an app
     */
    fun getCurrentState() = stateStore.getState().state

    /**
     * Create redux app state store which store and manage all states.
     */
    fun createStore() : Store<GlobalStateSlice,RAction, WrapperAction>{
        stateStore = createStore(combineReducersInferred(
            mapOf(GlobalStateSlice::state to StateManager::reducer)),
            GlobalStateSlice(),rEnhancer())
        dispatch(UmDi(di = DI.lazy { import(diModule)}))
        return stateStore
    }
}
