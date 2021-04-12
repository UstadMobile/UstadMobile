package com.ustadmobile.util

import com.ccfraser.muirwik.components.styles.Theme
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.model.statemanager.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.kodein.di.*
import redux.*
import kotlin.reflect.KProperty1


object StateManager{

    private data class UmDi(var di: DI): RAction

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

        constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with true

        bind<CoroutineDispatcher>(tag = UstadMobileSystemCommon.TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }
    }

    private fun reducer(state: GlobalState = GlobalState(), action: RAction): GlobalState {
        return when (action) {
            is AppBarState -> state.copy(title = action.title)
            is FabState -> state.copy(showFab = action.visible, onClick = action.onClick,
                fabLabel = action.label, fabIcon = action.icon)
            is UmDi -> state.copy(di = action.di)
            is HashState -> state.copy(view = action.view)
            is UmTheme -> state.copy(theme = action.theme)
            else -> state
        }
    }

    fun dispatch(action: RAction){
        stateStore.dispatch(action)
    }

    fun subscribe(mListener: (GlobalStateSlice)-> Unit){
        stateStore.subscribe { mListener(stateStore.getState()) }
    }

    fun getCurrentState() = stateStore.getState().state

    fun createStore() : Store<GlobalStateSlice,RAction, WrapperAction>{
        stateStore = createStore(combineReducersInferred(
            mapOf(GlobalStateSlice::state to StateManager::reducer)),
            GlobalStateSlice(),rEnhancer())
        dispatch(UmDi(di = DI.lazy { import(diModule)}))
        return stateStore
    }
}
