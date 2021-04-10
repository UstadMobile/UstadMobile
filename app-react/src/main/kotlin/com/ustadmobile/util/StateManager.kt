package com.ustadmobile.util

import com.ustadmobile.model.statemanager.UmAppBar
import com.ustadmobile.model.statemanager.UmAppState
import com.ustadmobile.model.statemanager.UmFab
import com.ustadmobile.model.statemanager.UmState
import redux.*
import kotlin.reflect.KProperty1


object StateManager{

    private val umReduxStore = createStore(combineReducersInferred(
        mapOf(UmAppState::state to StateManager::reducer)), UmAppState(),rEnhancer())

    // Credit https://github.com/JetBrains/kotlin-wrappers/blob/master/kotlin-redux/README.md
    private fun <S, A, R> combineReducersInferred(reducers: Map<KProperty1<S, R>, Reducer<*, A>>): Reducer<S, A> {
        return combineReducers(reducers.mapKeys { it.key.name})
    }

    private fun reducer(state: UmState = UmState(), action: RAction): UmState {
        return when (action) {
            is UmAppBar -> state.copy(title = action.title)
            is UmFab -> state.copy(showFab = action.showFab, isDetailScreen = action.isDetailScreen)
            else -> state
        }
    }

    fun dispatch(action: RAction){
        umReduxStore.dispatch(action)
    }

    fun subscribe(mListener: (UmAppState)-> Unit){
        umReduxStore.subscribe { mListener(umReduxStore.getState()) }
    }

    fun getCurrentState() = umReduxStore.getState()

    fun getStore() = umReduxStore
}
