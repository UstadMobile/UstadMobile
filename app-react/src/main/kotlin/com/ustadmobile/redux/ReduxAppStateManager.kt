package com.ustadmobile.redux

import com.ustadmobile.util.BrowserTabTracker
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

 */

object ReduxAppStateManager {

    private var storeState: Store<ReduxStore, RAction, WrapperAction> ? = null

    private fun <S, A, R> combineReducersInferred(reducers: Map<KProperty1<S, R>, Reducer<*, A>>): Reducer<S, A> {
        return combineReducers(reducers.mapKeys { it.key.name})
    }

    private fun reducer(state: ReduxAppState = ReduxAppState(), action: RAction): ReduxAppState {
        return when (action) {
            is ReduxThemeState -> state.copy(appTheme = action)
            is ReduxDiState -> state.copy(di = action)
            is ReduxDbState -> state.copy(db = action)
            is ReduxToolbarState -> state.copy(appToolbar = action)
            is ReduxNavStackState -> {
                BrowserTabTracker.navStackState = action
                state.copy(navStack = action)
            }
            is ReduxSnackBarState -> state.copy(appSnackBar = action)
            else -> state
        }
    }

    /**
     * Dispatch state update to the ReduxAppStore
     * @param action state action to be update (e.g ReduxAppTheme)
     */
    fun dispatch(action: RAction){
        storeState?.dispatch(action)
    }

    /**
     * Listen for the app state changes
     * @param listener listening part of he app (e.g component)
     */
    fun subscribe(listener: (ReduxStore)-> Unit){
        storeState?.subscribe {
            storeState?.getState()?.let { state ->
                listener(state)
            }
        }
    }

    /**
     * Get current app state
     */
    fun getCurrentState() =  storeState?.getState()?.appState ?: ReduxAppState()

    /**
     * Create a redux app store where the states will be managed
     */
    fun createStore(diState: ReduxDiState, theme: ReduxThemeState) : Store<ReduxStore, RAction, WrapperAction> {
        storeState = createStore(
          combineReducersInferred(
              mapOf(ReduxStore::appState to ReduxAppStateManager::reducer)
          ),
          ReduxStore(),
          rEnhancer())
        dispatch(diState)
        dispatch(theme)
        return storeState.unsafeCast<Store<ReduxStore, RAction, WrapperAction>>()
    }
}