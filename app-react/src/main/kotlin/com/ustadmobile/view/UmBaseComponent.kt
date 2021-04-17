package com.ustadmobile.view

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.model.statemanager.AppBarState
import com.ustadmobile.model.statemanager.SnackBarState
import com.ustadmobile.util.SearchManager
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.StateManager.getCurrentState
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Runnable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import react.*

open class UmBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props),
    UstadView, DIAware, DoorLifecycleOwner {

    private val lifecycleObservers: MutableList<DoorLifecycleObserver> = concurrentSafeListOf()

    val systemImpl : UstadMobileSystemImpl by instance()

    val umTheme : StateManager.UmTheme by instance()

    private val lifecycleStatus = atomic(0)

    protected var searchManager: SearchManager? = null

    override fun componentDidMount() {
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
        searchManager = SearchManager("um-search")
    }

    override fun RBuilder.render() {
        lifecycleStatus.value = DoorLifecycleObserver.CREATED
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            StateManager.dispatch(AppBarState(loading = value))
            setState { field = value }
        }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        StateManager.dispatch(SnackBarState(message,
            systemImpl.getString(actionMessageId, this)) { action() })
    }

    override fun runOnUiThread(r: Runnable?) {
        r?.run()
    }

    override val di: DI
        get() = getCurrentState().di

    override val currentState: Int
        get() = lifecycleStatus.value

    override fun addObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.add(observer)
    }

    override fun removeObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    override fun componentWillUnmount() {
        for(observer in lifecycleObservers){
            observer.onStop(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STOPPED
        searchManager?.onDestroy()
    }
}